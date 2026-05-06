package com.mindshift.nexre.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mindshift.nexre.domain.usecase.ExportJsonUseCase
import com.mindshift.nexre.domain.usecase.SummarizeLinkUseCase
import com.mindshift.nexre.domain.usecase.ValidateGeminiKeyUseCase
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data class ExportDone(val fileName: String) : SettingsEvent
    data object ExportError : SettingsEvent
    data class KeyValidated(val valid: Boolean) : SettingsEvent
    data object DataCleared : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val validateGeminiKeyUseCase: ValidateGeminiKeyUseCase,
    private val exportJsonUseCase: ExportJsonUseCase,
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _event = MutableStateFlow<SettingsEvent?>(null)
    val event = _event.asStateFlow()

    fun getStoredApiKey(): String {
        return try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val prefs = EncryptedSharedPreferences.create(
                context, "nexre_secure_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            prefs.getString("gemini_api_key", "") ?: ""
        } catch (e: Exception) { "" }
    }

    fun saveApiKey(key: String) {
        try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val prefs = EncryptedSharedPreferences.create(
                context, "nexre_secure_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            prefs.edit().putString("gemini_api_key", key.trim()).apply()
        } catch (e: Exception) { }
    }

    fun testApiKey(key: String) = viewModelScope.launch {
        val valid = validateGeminiKeyUseCase(key.trim())
        _event.value = SettingsEvent.KeyValidated(valid)
    }

    fun exportData() = viewModelScope.launch {
        try {
            val fileName = exportJsonUseCase()
            _event.value = SettingsEvent.ExportDone(fileName)
        } catch (e: Exception) {
            _event.value = SettingsEvent.ExportError
        }
    }

    fun clearArchived() = viewModelScope.launch { linkRepository.deleteArchivedLinks() }

    fun clearAllData() = viewModelScope.launch {
        linkRepository.deleteAllLinks()
        _event.value = SettingsEvent.DataCleared
    }

    fun getStoredModelId(): String {
        return context.getSharedPreferences("nexre_prefs", Context.MODE_PRIVATE)
            .getString("gemini_model_id", SummarizeLinkUseCase.DEFAULT_MODEL_ID)
            ?: SummarizeLinkUseCase.DEFAULT_MODEL_ID
    }

    fun saveModelId(modelId: String) {
        context.getSharedPreferences("nexre_prefs", Context.MODE_PRIVATE)
            .edit().putString("gemini_model_id", modelId.trim()).apply()
    }

    fun consumeEvent() { _event.value = null }
}
