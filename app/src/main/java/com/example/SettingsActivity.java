package com.example;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.database.AppDao;
import com.example.database.AppDatabase;
import com.example.model.Channel;
import com.example.model.Setting;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private View itemBootSetting;
    private View itemResetSetting;
    private CheckBox checkBootSetting;
    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        itemBootSetting = findViewById(R.id.item_boot_setting);
        itemResetSetting = findViewById(R.id.item_reset_setting);
        checkBootSetting = findViewById(R.id.check_boot_setting);
        btnBack = findViewById(R.id.btn_settings_back);

        btnBack.setOnClickListener(v -> finish());

        // Set up click listeners for settings
        itemBootSetting.setOnClickListener(v -> toggleBootSetting());
        itemResetSetting.setOnClickListener(v -> resetDatabase());

        loadSettings();
    }

    private void loadSettings() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();
            Setting setting = dao.getSetting("boot_in_player");
            boolean isChecked = setting != null && "true".equalsIgnoreCase(setting.getValue());

            runOnUiThread(() -> checkBootSetting.setChecked(isChecked));
        }).start();
    }

    private void toggleBootSetting() {
        boolean nextState = !checkBootSetting.isChecked();
        checkBootSetting.setChecked(nextState);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();
            dao.insertSetting(new Setting("boot_in_player", String.valueOf(nextState)));
            runOnUiThread(() -> Toast.makeText(SettingsActivity.this, 
                    nextState ? "অটো-বুট প্লেয়ার সক্রিয় করা হয়েছে" : "অটো-বুট প্লেয়ার নিষ্ক্রিয় করা হয়েছে", 
                    Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void resetDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();

            // Delete all and seed default channels
            dao.deleteAllChannels();

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

            runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "ডাটাবেজ ডিফল্ট অবস্থায় রিসেট করা হয়েছে", Toast.LENGTH_SHORT).show());
        }).start();
    }
}
