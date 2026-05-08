package com.mindshift.nexre.ui.share

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.SourcePlatform
import com.mindshift.nexre.domain.usecase.SaveImageUseCase
import com.mindshift.nexre.domain.usecase.SummarizeLinkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ShareUiState {
    data object Loading : ShareUiState
    data class Summary(val link: Link) : ShareUiState
    data class ImagePreview(val sourceUri: Uri, val title: String) : ShareUiState
    data object NoApiKey : ShareUiState
    data object NoInternet : ShareUiState
    data object Error : ShareUiState
    data object Saved : ShareUiState
}

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val summarizeLinkUseCase: SummarizeLinkUseCase,
    private val saveImageUseCase: SaveImageUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ShareUiState>(ShareUiState.Loading)
    val state = _state.asStateFlow()

    private var pendingLink: Link? = null

    fun startSummarize(url: String) = viewModelScope.launch {
        _state.value = ShareUiState.Loading
        val result = summarizeLinkUseCase(url)
        pendingLink = (result as? SummarizeLinkUseCase.Result.Success)?.link
        _state.value = when (result) {
            is SummarizeLinkUseCase.Result.Success -> ShareUiState.Summary(result.link)
            is SummarizeLinkUseCase.Result.NoApiKey -> ShareUiState.NoApiKey
            is SummarizeLinkUseCase.Result.NoInternet -> ShareUiState.NoInternet
            is SummarizeLinkUseCase.Result.GeminiError -> ShareUiState.Error
        }
    }

    fun startSummarizeText(text: String) = viewModelScope.launch {
        _state.value = ShareUiState.Loading
        val result = summarizeLinkUseCase.invokeText(text)
        pendingLink = (result as? SummarizeLinkUseCase.Result.Success)?.link
        _state.value = when (result) {
            is SummarizeLinkUseCase.Result.Success -> ShareUiState.Summary(result.link)
            is SummarizeLinkUseCase.Result.NoApiKey -> ShareUiState.NoApiKey
            is SummarizeLinkUseCase.Result.NoInternet -> ShareUiState.NoInternet
            is SummarizeLinkUseCase.Result.GeminiError -> ShareUiState.Error
        }
    }

    fun startSummarizeImage(uri: Uri) = viewModelScope.launch {
        _state.value = ShareUiState.Loading
        val result = saveImageUseCase(uri)
        _state.value = when (result) {
            is SaveImageUseCase.Result.Success -> {
                pendingLink = result.link
                ShareUiState.ImagePreview(sourceUri = uri, title = result.link.title)
            }
            is SaveImageUseCase.Result.StorageError -> ShareUiState.Error
        }
    }

    fun updateImageTitle(title: String) {
        pendingLink = pendingLink?.copy(title = title)
        val s = _state.value
        if (s is ShareUiState.ImagePreview) _state.value = s.copy(title = title)
    }

    fun confirmSave() = viewModelScope.launch {
        pendingLink?.let {
            if (it.sourcePlatform == SourcePlatform.IMAGE)
                saveImageUseCase.savePendingLink(it)
            else
                summarizeLinkUseCase.savePendingLink(it)
        }
        _state.value = ShareUiState.Saved
    }
}
