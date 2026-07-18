package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.database.AppDao;
import com.example.database.AppDatabase;
import com.example.model.Channel;
import com.example.model.Setting;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Run DB check and seed in a background thread
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();

            // Seed default channels if empty
            List<Channel> channels = dao.getAllChannels();
            if (channels.isEmpty()) {
                List<Channel> defaultChannels = new ArrayList<>();
                defaultChannels.add(new Channel(
                        "সময় টিভি",
                        "খবর",
                        "https://shstream.somoynews.tv/somoytv/smil:somoytv.smil/playlist.m3u8",
                        "https://images.somoynews.tv/somoytv/somoy-logo.png",
                        "সময় নিউজ ২৪ ঘণ্টা সরাসরি সম্প্রচার",
                        101
                ));
                defaultChannels.add(new Channel(
                        "চ্যানেল ২৪",
                        "খবর",
                        "https://c24.bdiptv.stream/hls/c24_360p.m3u8",
                        "https://www.channel24bd.tv/assets/images/logo.png",
                        "চ্যানেল ২৪ সংবাদ এবং লাইভ টকশো",
                        102
                ));
                defaultChannels.add(new Channel(
                        "আরটিভি",
                        "বিনোদন",
                        "https://rtv.bdiptv.stream/hls/rtv_360p.m3u8",
                        "https://www.rtvonline.com/assets/images/logo.png",
                        "আরটিভি নাটক, গান এবং এন্টারটেইনমেন্ট",
                        103
                ));
                defaultChannels.add(new Channel(
                        "যমুনা টিভি",
                        "খবর",
                        "https://jamuna.bdiptv.stream/jamuna/live.m3u8",
                        "https://www.jamuna.tv/images/logo.png",
                        "যমুনা নিউজ চব্বিশ ঘণ্টা সরাসরি সম্প্রচার",
                        104
                ));
                defaultChannels.add(new Channel(
                        "সিনটেল এইচডি",
                        "সিনেমা",
                        "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                        "ওপেন সোর্স এনিমেশন সিনেমা টেস্ট স্ট্রীম",
                        105
                ));
                defaultChannels.add(new Channel(
                        "খরগোশ টেস্ট",
                        "সিনেমা",
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                        "বিগ বাক বানি এনিমেশন টেস্ট ভিডিও ফাইল",
                        106
                ));
                defaultChannels.add(new Channel(
                        "টিয়ার্স অব স্টিল",
                        "সিনেমা",
                        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
                        "সায়েন্স ফিকশন এবং ভিজ্যুয়াল এফেক্ট টেস্ট স্ট্রীম",
                        107
                ));

                dao.insertAllChannels(defaultChannels);
            }

            // Check if user set Auto-Boot into Player setting
            Setting bootSetting = dao.getSetting("boot_in_player");
            boolean bootInPlayer = bootSetting != null && "true".equalsIgnoreCase(bootSetting.getValue());

            // Run on Main UI thread to transition
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (bootInPlayer) {
                    Intent intent = new Intent(SplashActivity.this, PlayerActivity.class);
                    // Open with default channel
                    intent.putExtra("channel_id", -1); // Auto load first channel
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
                finish();
            }, 1500); // 1.5 seconds delay for aesthetic loading feel
        }).start();
    }
}
