package com.mindshift.nexre.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val linkRepository: LinkRepository) : ViewModel() {

    val links = linkRepository.getHomeLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markRead(link: Link) = viewModelScope.launch {
        linkRepository.updateStatus(link.id, LinkStatus.READ)
    }

    fun markUnread(link: Link) = viewModelScope.launch {
        linkRepository.updateStatus(link.id, LinkStatus.UNREAD)
    }

    fun archive(link: Link) = viewModelScope.launch {
        linkRepository.updateStatus(link.id, LinkStatus.ARCHIVED)
    }

    fun unarchive(link: Link) = viewModelScope.launch {
        linkRepository.updateStatus(link.id, LinkStatus.UNREAD)
    }
}
