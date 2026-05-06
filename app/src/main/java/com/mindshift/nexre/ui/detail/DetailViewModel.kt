package com.mindshift.nexre.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: LinkRepository,
) : ViewModel() {

    private val linkId: String = checkNotNull(savedStateHandle["linkId"])

    val link = repo.getLinkById(linkId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var browserOpenTime = 0L

    fun recordOpen() = viewModelScope.launch {
        repo.recordOpen(linkId)
        browserOpenTime = System.currentTimeMillis()
    }

    fun recordReadDuration() {
        if (browserOpenTime > 0) {
            val seconds = ((System.currentTimeMillis() - browserOpenTime) / 1000).toInt()
            viewModelScope.launch { repo.addReadDuration(linkId, seconds) }
            browserOpenTime = 0
        }
    }

    fun markRead() = viewModelScope.launch { repo.updateStatus(linkId, LinkStatus.READ) }
    fun archive() = viewModelScope.launch { repo.updateStatus(linkId, LinkStatus.ARCHIVED) }
    fun delete() = viewModelScope.launch { repo.deleteLink(linkId) }
    fun toggleFavourite(current: Boolean) = viewModelScope.launch { repo.updateFavourite(linkId, !current) }
    fun updateNote(note: String) = viewModelScope.launch { repo.updateNote(linkId, note) }
}
