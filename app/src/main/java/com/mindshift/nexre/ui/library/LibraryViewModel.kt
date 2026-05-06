package com.mindshift.nexre.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryFilter { ALL, UNREAD, READ, ARCHIVED, FAVOURITES }
enum class LibrarySort { DATE_SAVED, RECENTLY_OPENED }

@HiltViewModel
class LibraryViewModel @Inject constructor(private val repo: LinkRepository) : ViewModel() {

    val filter = MutableStateFlow(LibraryFilter.ALL)
    val sort = MutableStateFlow(LibrarySort.DATE_SAVED)

    val links = combine(filter, sort, repo.getAllLinks(), repo.getLinksSortedByOpened()) { f, s, all, byOpened ->
        val base = if (s == LibrarySort.DATE_SAVED) all else byOpened
        when (f) {
            LibraryFilter.ALL -> base.filter { it.status != LinkStatus.ARCHIVED }
            LibraryFilter.UNREAD -> base.filter { it.status == LinkStatus.UNREAD }
            LibraryFilter.READ -> base.filter { it.status == LinkStatus.READ }
            LibraryFilter.ARCHIVED -> all.filter { it.status == LinkStatus.ARCHIVED }
            LibraryFilter.FAVOURITES -> base.filter { it.isFavourite }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(f: LibraryFilter) { filter.value = f }
    fun setSort(s: LibrarySort) { sort.value = s }
    fun markRead(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.READ) }
    fun markUnread(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.UNREAD) }
    fun archive(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.ARCHIVED) }
    fun unarchive(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.UNREAD) }
}
