package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AppRepository(private val appDao: AppDao) {

    val allChannels: Flow<List<ChannelEntity>> = appDao.getAllChannels()

    fun observeSetting(key: String): Flow<String?> {
        return appDao.observeSetting(key).map { it?.value }
    }

    suspend fun getSetting(key: String): String? {
        return appDao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        appDao.saveSetting(SettingEntity(key, value))
    }

    suspend fun prepopulateData() {
        val channels = allChannels.map { it }.firstOrNull() ?: emptyList()
        if (channels.isEmpty()) {
            val defaultChannels = listOf(
                // Bangladeshi Channels
                ChannelEntity(
                    id = 101,
                    name = "বিটিভি (BTV)",
                    category = "বাংলাদেশি",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    description = "গণপ্রজাতন্ত্রী বাংলাদেশ সরকারের রাষ্ট্রীয় টেলিভিশন সংস্থা।"
                ),
                ChannelEntity(
                    id = 102,
                    name = "সময় টিভি (Somoy TV)",
                    category = "বাংলাদেশি",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    description = "বাংলাদেশের ২৪ ঘণ্টার একটি শীর্ষস্থানীয় সংবাদভিত্তিক স্যাটেলাইট টেলিভিশন চ্যানেল।"
                ),
                ChannelEntity(
                    id = 103,
                    name = "যমুনা টিভি (Jamuna TV)",
                    category = "বাংলাদেশি",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    description = "বস্তুনিষ্ঠ সংবাদ নিয়ে সার্বক্ষণিক সত্যের সন্ধানে যমুনা টেলিভিশন।"
                ),
                ChannelEntity(
                    id = 104,
                    name = "দীপ্ত টিভি (Deepto TV)",
                    category = "বাংলাদেশি",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    description = "দীপ্ত টিভি একটি জনপ্রিয় বাংলাদেশী বিনোদনমূলক স্যাটেলাইট টিভি চ্যানেল।"
                ),
                
                // News
                ChannelEntity(
                    id = 201,
                    name = "বিবিসি বাংলা (BBC Bangla)",
                    category = "খবর",
                    url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    description = "বিবিসি ওয়ার্ল্ড সার্ভিসের বাংলা বিভাগ থেকে আন্তর্জাতিক ও আঞ্চলিক খবর।"
                ),
                ChannelEntity(
                    id = 202,
                    name = "সিএনএন (CNN)",
                    category = "খবর",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    description = "বিশ্বের অন্যতম প্রাচীন এবং সর্বাধিক পরিচিত চব্বিশ ঘণ্টার সংবাদভিত্তিক চ্যানেল।"
                ),
                ChannelEntity(
                    id = 203,
                    name = "আল জাজিরা (Al Jazeera)",
                    category = "খবর",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                    description = "কাতার ভিত্তিক বিশ্বের শীর্ষস্থানীয় সংবাদ নেটওয়ার্ক আল জাজিরা।"
                ),

                // Sports
                ChannelEntity(
                    id = 301,
                    name = "টি স্পোর্টস (T Sports)",
                    category = "খেলাধুলা",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    description = "বাংলাদেশের প্রথম ও একমাত্র ডেডিকেটেড স্পোর্টস চ্যানেল।"
                ),
                ChannelEntity(
                    id = 302,
                    name = "জিটিভি স্পোর্টস (GTV Sports)",
                    category = "খেলাধুলা",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                    description = "গাজী টেলিভিশন বাংলাদেশের একটি জনপ্রিয় স্যাটেলাইট ক্রীড়া ও সাধারণ চ্যানেল।"
                ),

                // Entertainment
                ChannelEntity(
                    id = 401,
                    name = "জি বাংলা (Zee Bangla)",
                    category = "বিনোদন",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
                    description = "বাঙালি সংস্কৃতির শ্রেষ্ঠ ড্রামা সিরিজ ও রিয়েলিটি শো-র বিনোদন চ্যানেল।"
                ),
                ChannelEntity(
                    id = 402,
                    name = "স্টার জলসা (Star Jalsha)",
                    category = "বিনোদন",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    description = "স্টার নেটওয়ার্কের সেরা বাংলা ফ্যামিলি ড্রামা ও রোমাঞ্চকর শোগুলোর সম্প্রচারক।"
                ),

                // Movies
                ChannelEntity(
                    id = 501,
                    name = "সনি পিক্স (Sony Pix)",
                    category = "চলচ্চিত্র",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    description = "হলিউড অ্যাকশন ও ব্লকবাস্টার সিনেমার অন্যতম বিনোদন মাধ্যম।"
                ),
                ChannelEntity(
                    id = 502,
                    name = "এইচবিও (HBO)",
                    category = "চলচ্চিত্র",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    description = "বিশ্বের সবচেয়ে জনপ্রিয় প্রিমিয়াম সিনেমা ও সিরিজ সম্প্রচারকারী টিভি চ্যানেল।"
                )
            )
            appDao.insertChannels(defaultChannels)
        }

        // Check and write default settings
        if (getSetting("boot_in_player") == null) {
            saveSetting("boot_in_player", "false")
        }
        if (getSetting("api_url") == null) {
            saveSetting("api_url", "https://api.smarttvplay.live/v2")
        }
        if (getSetting("api_key") == null) {
            saveSetting("api_key", "stvl_live_2026_hzptqm_secured")
        }
        if (getSetting("hmac_key") == null) {
            saveSetting("hmac_key", "hmac_sha256_tv_key_smart")
        }
        if (getSetting("encryption_key") == null) {
            saveSetting("encryption_key", "aes_256_cbc_smart_tv_enc")
        }
    }
}
