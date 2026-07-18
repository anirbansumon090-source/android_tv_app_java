package com.example;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.database.AppDao;
import com.example.database.AppDatabase;
import com.example.model.Channel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerCategories;
    private RecyclerView recyclerChannels;
    private TextView txtSectionTitle;
    private View btnSettings;

    private List<Channel> allChannels = new ArrayList<>();
    private List<Channel> filteredChannels = new ArrayList<>();
    private List<String> categories = new ArrayList<>();

    private CategoryAdapter categoryAdapter;
    private ChannelAdapter channelAdapter;
    private String selectedCategory = "সব";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerCategories = findViewById(R.id.recycler_categories);
        recyclerChannels = findViewById(R.id.recycler_channels);
        txtSectionTitle = findViewById(R.id.txt_section_title);
        btnSettings = findViewById(R.id.btn_settings);

        // Setup settings button click
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Initialize Lists
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerChannels.setLayoutManager(new GridLayoutManager(this, 3)); // 3 Column Grid for landscape TVs

        categoryAdapter = new CategoryAdapter();
        channelAdapter = new ChannelAdapter();

        recyclerCategories.setAdapter(categoryAdapter);
        recyclerChannels.setAdapter(channelAdapter);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Reload data if db was reset in settings
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();
            allChannels = dao.getAllChannels();

            // Extract unique categories
            categories.clear();
            categories.add("সব");
            for (Channel c : allChannels) {
                if (!categories.contains(c.getCategory())) {
                    categories.add(c.getCategory());
                }
            }

            runOnUiThread(() -> {
                categoryAdapter.notifyDataSetChanged();
                filterChannels(selectedCategory);
            });
        }).start();
    }

    private void filterChannels(String category) {
        selectedCategory = category;
        txtSectionTitle.setText(category + " চ্যানেল সমূহ (" + getChannelCountForCategory(category) + ")");
        filteredChannels.clear();
        if ("সব".equalsIgnoreCase(category)) {
            filteredChannels.addAll(allChannels);
        } else {
            for (Channel c : allChannels) {
                if (category.equalsIgnoreCase(c.getCategory())) {
                    filteredChannels.add(c);
                }
            }
        }
        channelAdapter.notifyDataSetChanged();
    }

    private int getChannelCountForCategory(String category) {
        if ("সব".equalsIgnoreCase(category)) return allChannels.size();
        int count = 0;
        for (Channel c : allChannels) {
            if (category.equalsIgnoreCase(c.getCategory())) count++;
        }
        return count;
    }

    // Category ViewHolder and Adapter
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            String category = categories.get(position);
            holder.txtCategory.setText(category);

            // Handle selection and focus
            boolean isSelected = category.equalsIgnoreCase(selectedCategory);
            holder.txtCategory.setSelected(isSelected);

            // Set colors dynamically based on focus and selection
            holder.txtCategory.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    holder.txtCategory.setTextColor(Color.parseColor("#381E72")); // OnPrimaryPurple
                    // Filter on focus for instant responsive TV feel!
                    filterChannels(category);
                } else {
                    if (category.equalsIgnoreCase(selectedCategory)) {
                        holder.txtCategory.setTextColor(Color.parseColor("#D0BCFF")); // Highlighted purple
                    } else {
                        holder.txtCategory.setTextColor(Color.parseColor("#CAC4D0")); // SoftGray
                    }
                }
            });

            // Set initial state
            if (isSelected) {
                holder.txtCategory.setTextColor(Color.parseColor("#D0BCFF"));
            } else {
                holder.txtCategory.setTextColor(Color.parseColor("#CAC4D0"));
            }

            holder.txtCategory.setOnClickListener(v -> filterChannels(category));
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView txtCategory;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                txtCategory = itemView.findViewById(R.id.txt_category_name);
            }
        }
    }

    // Channel ViewHolder and Adapter
    private class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

        @NonNull
        @Override
        public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
            return new ChannelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
            Channel channel = filteredChannels.get(position);
            holder.txtChannelName.setText(channel.getName());
            holder.txtChannelNumber.setText("CH " + channel.getNumber());

            // Handle live badge visibility
            holder.txtLiveBadge.setVisibility(View.VISIBLE);

            // Use Glide to load channel logo! (requirement)
            Glide.with(HomeActivity.this)
                    .load(channel.getLogoUrl())
                    .placeholder(R.drawable.ic_tv)
                    .error(R.drawable.ic_tv)
                    .into(holder.imgLogo);

            // OnFocusChangeListener to highlight text on card focus
            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    holder.txtChannelName.setTextColor(Color.parseColor("#381E72")); // OnPrimaryPurple
                    holder.txtChannelNumber.setTextColor(Color.parseColor("#381E72"));
                    holder.imgLogo.setColorFilter(Color.parseColor("#381E72"));
                } else {
                    holder.txtChannelName.setTextColor(Color.parseColor("#E6E1E5")); // TextWhite
                    holder.txtChannelNumber.setTextColor(Color.parseColor("#66FFFFFF"));
                    holder.imgLogo.clearColorFilter();
                }
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
                intent.putExtra("channel_id", channel.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return filteredChannels.size();
        }

        class ChannelViewHolder extends RecyclerView.ViewHolder {
            ImageView imgLogo;
            TextView txtChannelName;
            TextView txtChannelNumber;
            TextView txtLiveBadge;

            public ChannelViewHolder(@NonNull View itemView) {
                super(itemView);
                imgLogo = itemView.findViewById(R.id.img_channel_logo);
                txtChannelName = itemView.findViewById(R.id.txt_channel_name);
                txtChannelNumber = itemView.findViewById(R.id.txt_channel_number);
                txtLiveBadge = itemView.findViewById(R.id.txt_live_badge);
            }
        }
    }
}
