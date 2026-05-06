package com.mindshift.nexre.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.usecase.SummarizeLinkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ShareUiState {
    data object Loading : ShareUiState
    data class Summary(val link: Link) : ShareUiState
    data object NoApiKey : ShareUiState
    data object NoInternet : ShareUiState
    data object Error : ShareUiState
    data object Saved : ShareUiState
}

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val summarizeLinkUseCase: SummarizeLinkUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ShareUiState>(ShareUiState.Loading)
    val state = _state.asStateFlow()

    fun startSummarize(url: String) = viewModelScope.launch {
        _state.value = ShareUiState.Loading
        _state.value = when (val result = summarizeLinkUseCase(url)) {
            is SummarizeLinkUseCase.Result.Success -> ShareUiState.Summary(result.link)
            is SummarizeLinkUseCase.Result.NoApiKey -> ShareUiState.NoApiKey
            is SummarizeLinkUseCase.Result.NoInternet -> ShareUiState.NoInternet
            is SummarizeLinkUseCase.Result.GeminiError -> ShareUiState.Error
        }
    }

    fun startSummarizeText(text: String) = viewModelScope.launch {
        _state.value = ShareUiState.Loading
        _state.value = when (val result = summarizeLinkUseCase.invokeText(text)) {
            is SummarizeLinkUseCase.Result.Success -> ShareUiState.Summary(result.link)
            is SummarizeLinkUseCase.Result.NoApiKey -> ShareUiState.NoApiKey
            is SummarizeLinkUseCase.Result.NoInternet -> ShareUiState.NoInternet
            is SummarizeLinkUseCase.Result.GeminiError -> ShareUiState.Error
        }
    }

    fun confirmSave() { _state.value = ShareUiState.Saved }
}
