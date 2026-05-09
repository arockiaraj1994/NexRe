package com.mindshift.nexre.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Link
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryFilter { ALL, UNREAD, READ, ARCHIVED, FAVOURITES }

enum class LibrarySort(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    RECENTLY_OPENED("Recently opened"),
    MOST_OPENED("Most opened"),
}

@HiltViewModel
class LibraryViewModel @Inject constructor(private val repo: LinkRepository) : ViewModel() {

    val filter = MutableStateFlow(LibraryFilter.ALL)
    val sort = MutableStateFlow(LibrarySort.NEWEST)

    val links = combine(filter, sort) { f, s -> Pair(f, s) }
        .flatMapLatest { (f, s) ->
            val base: Flow<List<Link>> = when (s) {
                LibrarySort.NEWEST -> repo.getAllLinks()
                LibrarySort.OLDEST -> repo.getAllLinksOldestFirst()
                LibrarySort.RECENTLY_OPENED -> repo.getLinksSortedByOpened()
                LibrarySort.MOST_OPENED -> repo.getAllLinksMostOpened()
            }
            base.map { list -> applyFilter(list, f) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun applyFilter(list: List<Link>, f: LibraryFilter): List<Link> = when (f) {
        LibraryFilter.ALL -> list.filter { it.status != LinkStatus.ARCHIVED }
        LibraryFilter.UNREAD -> list.filter { it.status == LinkStatus.UNREAD }
        LibraryFilter.READ -> list.filter { it.status == LinkStatus.READ }
        LibraryFilter.ARCHIVED -> list.filter { it.status == LinkStatus.ARCHIVED }
        LibraryFilter.FAVOURITES -> list.filter { it.isFavourite }
    }

    fun setFilter(f: LibraryFilter) { filter.value = f }
    fun setSort(s: LibrarySort) { sort.value = s }
    fun markRead(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.READ) }
    fun markUnread(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.UNREAD) }
    fun archive(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.ARCHIVED) }
    fun unarchive(link: Link) = viewModelScope.launch { repo.updateStatus(link.id, LinkStatus.UNREAD) }
}
