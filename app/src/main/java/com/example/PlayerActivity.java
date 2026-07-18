package com.example;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.database.AppDao;
import com.example.database.AppDatabase;
import com.example.model.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private DefaultHttpDataSource.Factory httpDataSourceFactory;

    private View drawerChannels;
    private RecyclerView recyclerDrawerChannels;
    private View overlayInfoPanel;
    private View overlayNumberInput;
    private TextView txtTypedNumbers;

    private ImageView imgPlayerLogo;
    private TextView txtPlayerChannelNumber;
    private TextView txtPlayerChannelName;
    private TextView txtPlayerChannelDesc;

    private List<Channel> channels = new ArrayList<>();
    private Channel currentChannel;
    private DrawerAdapter drawerAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder typedBuffer = new StringBuilder();

    // Runnables for hiding overlays
    private final Runnable hideInfoPanelRunnable = () -> overlayInfoPanel.setVisibility(View.GONE);
    private final Runnable hideNumberInputRunnable = () -> {
        overlayNumberInput.setVisibility(View.GONE);
        tuneToTypedNumber();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.player_view);
        drawerChannels = findViewById(R.id.drawer_channels);
        recyclerDrawerChannels = findViewById(R.id.recycler_drawer_channels);
        overlayInfoPanel = findViewById(R.id.overlay_info_panel);
        overlayNumberInput = findViewById(R.id.overlay_number_input);
        txtTypedNumbers = findViewById(R.id.txt_typed_numbers);

        imgPlayerLogo = findViewById(R.id.img_player_logo);
        txtPlayerChannelNumber = findViewById(R.id.txt_player_channel_number);
        txtPlayerChannelName = findViewById(R.id.txt_player_channel_name);
        txtPlayerChannelDesc = findViewById(R.id.txt_player_channel_desc);

        recyclerDrawerChannels.setLayoutManager(new LinearLayoutManager(this));
        drawerAdapter = new DrawerAdapter();
        recyclerDrawerChannels.setAdapter(drawerAdapter);

        // Initialize ExoPlayer with an explicit MediaSourceFactory.
        // allowCrossProtocolRedirects(true) matters a lot for IPTV: many free
        // stream sources redirect http -> https (or the reverse) via their CDN,
        // and ExoPlayer's default HTTP data source refuses that redirect unless
        // this is turned on, causing an otherwise-valid stream to fail to load.
        httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("SmartTVLive/1.0 (Linux;Android) ExoPlayerLib/media3")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000);
        DefaultDataSource.Factory dataSourceFactory =
                new DefaultDataSource.Factory(this, httpDataSourceFactory);
        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();
        playerView.setPlayer(player);

        int channelId = getIntent().getIntExtra("channel_id", -1);

        loadChannelsAndStart(channelId);
    }

    private void loadChannelsAndStart(int channelId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();
            channels = dao.getAllChannels();

            if (!channels.isEmpty()) {
                currentChannel = channels.get(0);
                if (channelId != -1) {
                    for (Channel c : channels) {
                        if (c.getId() == channelId) {
                            currentChannel = c;
                            break;
                        }
                    }
                }
                runOnUiThread(() -> {
                    drawerAdapter.notifyDataSetChanged();
                    playChannel(currentChannel);
                });
            } else {
                runOnUiThread(() -> Toast.makeText(PlayerActivity.this, "কোন চ্যানেল পাওয়া যায়নি", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void playChannel(Channel channel) {
        if (channel == null || player == null) return;
        currentChannel = channel;

        // Apply this channel's custom HTTP headers (Referer/User-Agent/Cookie/token/etc.)
        // before creating the media source. Many protected or anti-hotlink stream sources
        // reject playback (403/blocked) unless the exact expected headers are sent.
        // setDefaultRequestProperties only affects HttpDataSource instances created
        // AFTER this call, so it must run right before player.prepare() for each channel.
        Map<String, String> channelHeaders = channel.getHeaders();
        httpDataSourceFactory.setDefaultRequestProperties(channelHeaders);

        // Start video streaming using Media3 ExoPlayer.
        // The MIME type is detected explicitly so that HLS (.m3u8) and DASH (.mpd)
        // streams still resolve correctly even when the URL has query params/tokens
        // after the extension (very common with IPTV links).
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(channel.getUrl())
                .setMimeType(detectMimeType(channel.getUrl()))
                .build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        // Populate bottom info overlay
        txtPlayerChannelName.setText(channel.getName());
        txtPlayerChannelNumber.setText("CH " + channel.getNumber());
        txtPlayerChannelDesc.setText(channel.getDescription());

        // Load channel logo in bottom panel!
        Glide.with(this)
                .load(channel.getLogoUrl())
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv)
                .into(imgPlayerLogo);

        // Show info panel and hide it after 4 seconds
        showInfoPanel();
    }

    /**
     * Detects the correct Media3 MIME type for a stream URL so ExoPlayer picks the
     * right MediaSource (HLS, DASH, or progressive) instead of relying purely on the
     * raw file extension, which can be hidden behind query strings/tokens.
     */
    private String detectMimeType(String url) {
        if (url == null) return MimeTypes.APPLICATION_M3U8;
        String lower = url.toLowerCase();
        // Strip off any query string / fragment before checking the extension.
        int cut = lower.length();
        int q = lower.indexOf('?');
        if (q != -1) cut = Math.min(cut, q);
        int h = lower.indexOf('#');
        if (h != -1) cut = Math.min(cut, h);
        String path = lower.substring(0, cut);

        if (path.endsWith(".m3u8") || lower.contains("m3u8")) {
            return MimeTypes.APPLICATION_M3U8;
        } else if (path.endsWith(".mpd") || lower.contains("mpd")) {
            return MimeTypes.APPLICATION_MPD;
        } else if (path.endsWith(".ism") || path.contains(".ism/manifest")) {
            return MimeTypes.APPLICATION_SS;
        } else if (path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".webm")
                || path.endsWith(".mp3") || path.endsWith(".aac")) {
            return MimeTypes.VIDEO_MP4;
        }
        // Default to HLS since most live IPTV channel links are HLS playlists,
        // even without a clean .m3u8 extension.
        return MimeTypes.APPLICATION_M3U8;
    }

    private void showInfoPanel() {
        overlayInfoPanel.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideInfoPanelRunnable);
        handler.postDelayed(hideInfoPanelRunnable, 4000);
    }

    // Capture standard D-pad keyboard keys and TV Remote codes
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 1. Direct Numeric channel entry (0 to 9 keys)
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            int pressedNumber = keyCode - KeyEvent.KEYCODE_0;
            appendTypedNumber(pressedNumber);
            return true;
        }

        // 2. D-pad Up arrow -> Previous channel
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (drawerChannels.getVisibility() == View.GONE) {
                switchChannelOffset(-1);
                return true;
            }
        }

        // 3. D-pad Down arrow -> Next channel
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (drawerChannels.getVisibility() == View.GONE) {
                switchChannelOffset(1);
                return true;
            }
        }

        // 4. D-pad Left arrow -> Open quick drawer channel selection
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (drawerChannels.getVisibility() == View.GONE) {
                drawerChannels.setVisibility(View.VISIBLE);
                recyclerDrawerChannels.requestFocus();
                return true;
            }
        }

        // 5. D-pad Right arrow -> Close quick drawer channel selection
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (drawerChannels.getVisibility() == View.VISIBLE) {
                drawerChannels.setVisibility(View.GONE);
                return true;
            }
        }

        // 6. Center key -> Show overlay panel
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (drawerChannels.getVisibility() == View.GONE) {
                showInfoPanel();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (drawerChannels.getVisibility() == View.VISIBLE) {
            drawerChannels.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void switchChannelOffset(int offset) {
        if (channels.isEmpty() || currentChannel == null) return;
        int currentIndex = channels.indexOf(currentChannel);
        if (currentIndex != -1) {
            int targetIndex = currentIndex + offset;
            if (targetIndex < 0) targetIndex = channels.size() - 1;
            if (targetIndex >= channels.size()) targetIndex = 0;
            playChannel(channels.get(targetIndex));
        }
    }

    private void appendTypedNumber(int number) {
        typedBuffer.append(number);
        txtTypedNumbers.setText(typedBuffer.toString());
        overlayNumberInput.setVisibility(View.VISIBLE);

        handler.removeCallbacks(hideNumberInputRunnable);
        handler.postDelayed(hideNumberInputRunnable, 2000); // Wait 2 seconds of silence before tuning
    }

    private void tuneToTypedNumber() {
        if (typedBuffer.length() == 0) return;
        try {
            int number = Integer.parseInt(typedBuffer.toString());
            typedBuffer.setLength(0); // Reset buffer

            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                AppDao dao = db.appDao();
                Channel targetChannel = dao.getChannelByNumber(number);

                runOnUiThread(() -> {
                    if (targetChannel != null) {
                        playChannel(targetChannel);
                    } else {
                        Toast.makeText(PlayerActivity.this, "চ্যানেল " + number + " খুঁজে পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            typedBuffer.setLength(0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    // Left Drawer Quick Menu List adapter
    private class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {

        @NonNull
        @Override
        public DrawerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new DrawerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DrawerViewHolder holder, int position) {
            Channel channel = channels.get(position);
            holder.txtTitle.setText(channel.getNumber() + " - " + channel.getName());

            boolean isCurrent = currentChannel != null && channel.getId() == currentChannel.getId();

            holder.txtTitle.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    holder.txtTitle.setTextColor(Color.parseColor("#381E72")); // Dark text on focused
                } else {
                    if (isCurrent) {
                        holder.txtTitle.setTextColor(Color.parseColor("#D0BCFF")); // Highlighted purple
                    } else {
                        holder.txtTitle.setTextColor(Color.parseColor("#CAC4D0")); // Normal soft gray
                    }
                }
            });

            if (isCurrent) {
                holder.txtTitle.setTextColor(Color.parseColor("#D0BCFF"));
            } else {
                holder.txtTitle.setTextColor(Color.parseColor("#CAC4D0"));
            }

            holder.txtTitle.setOnClickListener(v -> {
                playChannel(channel);
                drawerChannels.setVisibility(View.GONE);
            });
        }

        @Override
        public int getItemCount() {
            return channels.size();
        }

        class DrawerViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle;

            public DrawerViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txt_category_name);
            }
        }
    }
}
