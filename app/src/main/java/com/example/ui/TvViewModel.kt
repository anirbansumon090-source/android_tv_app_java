package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ChannelEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class SplashState {
    object Idle : SplashState()
    object Loading : SplashState()
    object Success : SplashState()
}

data class UserAccount(
    val username: String,
    val name: String,
    val type: String, // "Premium", "Free"
    val expiryDate: String
)

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // Splash
    private val _splashState = MutableStateFlow<SplashState>(SplashState.Idle)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    private val _bootInPlayer = MutableStateFlow(false)
    val bootInPlayer: StateFlow<Boolean> = _bootInPlayer.asStateFlow()

    // Channels & Categories
    private val _channels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val channels: StateFlow<List<ChannelEntity>> = _channels.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentPlayingChannel = MutableStateFlow<ChannelEntity?>(null)
    val currentPlayingChannel: StateFlow<ChannelEntity?> = _currentPlayingChannel.asStateFlow()

    // Remote Typed Channel Number Logic
    private val _typedNumber = MutableStateFlow("")
    val typedNumber: StateFlow<String> = _typedNumber.asStateFlow()

    private val _typedChannelFound = MutableStateFlow<ChannelEntity?>(null)
    val typedChannelFound: StateFlow<ChannelEntity?> = _typedChannelFound.asStateFlow()

    // Overlay display flag
    private val _showChannelOverlay = MutableStateFlow(false)
    val showChannelOverlay: StateFlow<Boolean> = _showChannelOverlay.asStateFlow()

    // Active User State
    private val _loggedInUser = MutableStateFlow<UserAccount?>(null)
    val loggedInUser: StateFlow<UserAccount?> = _loggedInUser.asStateFlow()

    // API Config State
    private val _apiUrl = MutableStateFlow("")
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _hmacKey = MutableStateFlow("")
    val hmacKey: StateFlow<String> = _hmacKey.asStateFlow()

    private val _encryptionKey = MutableStateFlow("")
    val encryptionKey: StateFlow<String> = _encryptionKey.asStateFlow()

    private var numberInputJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Load data from db and pre-populate
        viewModelScope.launch {
            repository.prepopulateData()
            
            // Observe boot in player
            repository.observeSetting("boot_in_player").collectLatest { value ->
                _bootInPlayer.value = value == "true"
            }
        }

        viewModelScope.launch {
            // Observe channels
            repository.allChannels.collectLatest { channelList ->
                _channels.value = channelList
                val distinctCategories = channelList.map { it.category }.distinct()
                _categories.value = distinctCategories
                if (distinctCategories.isNotEmpty() && _selectedCategory.value.isEmpty()) {
                    _selectedCategory.value = distinctCategories.first()
                }
            }
        }

        viewModelScope.launch {
            // Load API config info from DB
            _apiUrl.value = repository.getSetting("api_url") ?: "https://api.smarttvplay.live/v2"
            _apiKey.value = repository.getSetting("api_key") ?: "stvl_live_2026_hzptqm_secured"
            _hmacKey.value = repository.getSetting("hmac_key") ?: "hmac_sha256_tv_key_smart"
            _encryptionKey.value = repository.getSetting("encryption_key") ?: "aes_256_cbc_smart_tv_enc"

            // Load saved user login if any
            val savedUser = repository.getSetting("user_username")
            if (savedUser != null) {
                _loggedInUser.value = UserAccount(
                    username = savedUser,
                    name = repository.getSetting("user_name") ?: "Anirban Sumon",
                    type = repository.getSetting("user_type") ?: "Premium TV Client",
                    expiryDate = repository.getSetting("user_expiry") ?: "২০২৭-১২-৩১"
                )
            }
        }
    }

    fun startSplashScreen(onFinished: (bootInPlayer: Boolean) -> Unit) {
        viewModelScope.launch {
            _splashState.value = SplashState.Loading
            // Simulate reading configuration and API loading (Firebase, etc.)
            delay(2200)
            _splashState.value = SplashState.Success
            onFinished(_bootInPlayer.value)
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectChannel(channel: ChannelEntity) {
        _currentPlayingChannel.value = channel
    }

    fun nextChannel() {
        val list = _channels.value
        if (list.isEmpty()) return
        val current = _currentPlayingChannel.value ?: list.first()
        val index = list.indexOfFirst { it.id == current.id }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            _currentPlayingChannel.value = list[nextIndex]
        } else {
            _currentPlayingChannel.value = list.first()
        }
    }

    fun prevChannel() {
        val list = _channels.value
        if (list.isEmpty()) return
        val current = _currentPlayingChannel.value ?: list.first()
        val index = list.indexOfFirst { it.id == current.id }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            _currentPlayingChannel.value = list[prevIndex]
        } else {
            _currentPlayingChannel.value = list.last()
        }
    }

    fun appendChannelNumberDigit(digit: Char) {
        numberInputJob?.cancel()
        val currentText = _typedNumber.value + digit
        _typedNumber.value = currentText

        // Check if we found a match
        val matchedChannel = _channels.value.find { it.id.toString() == currentText }
        _typedChannelFound.value = matchedChannel

        // Start countdown to switch channel
        numberInputJob = viewModelScope.launch {
            if (currentText.length >= 3) {
                // Instantly change if 3 digits matched or exceeded
                delay(400)
                executeChannelSwitch()
            } else {
                delay(2000) // Wait 2 seconds for further digits
                executeChannelSwitch()
            }
        }
    }

    private fun executeChannelSwitch() {
        val targetNumber = _typedNumber.value
        val matched = _channels.value.find { it.id.toString() == targetNumber }
        if (matched != null) {
            _currentPlayingChannel.value = matched
        }
        _typedNumber.value = ""
        _typedChannelFound.value = null
    }

    fun toggleBootInPlayer(enabled: Boolean) {
        viewModelScope.launch {
            _bootInPlayer.value = enabled
            repository.saveSetting("boot_in_player", enabled.toString())
        }
    }

    fun setApiUrl(url: String) {
        viewModelScope.launch {
            _apiUrl.value = url
            repository.saveSetting("api_url", url)
        }
    }

    fun loginUser(username: String, name: String) {
        viewModelScope.launch {
            val user = UserAccount(
                username = username,
                name = name,
                type = "Premium",
                expiryDate = "২০২৭-১২-৩১"
            )
            _loggedInUser.value = user
            repository.saveSetting("user_username", username)
            repository.saveSetting("user_name", name)
            repository.saveSetting("user_type", "Premium")
            repository.saveSetting("user_expiry", "২০২৭-১২-৩১")
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            _loggedInUser.value = null
            repository.saveSetting("user_username", "")
            repository.saveSetting("user_name", "")
            repository.saveSetting("user_type", "")
            repository.saveSetting("user_expiry", "")
        }
    }

    fun showOverlayForChannel() {
        viewModelScope.launch {
            _showChannelOverlay.value = true
            delay(4000) // Auto hide overlay after 4 seconds
            _showChannelOverlay.value = false
        }
    }

    fun toggleChannelOverlay(visible: Boolean) {
        _showChannelOverlay.value = visible
    }
}
