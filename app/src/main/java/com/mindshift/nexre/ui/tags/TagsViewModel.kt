package com.mindshift.nexre.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(private val tagRepository: TagRepository) : ViewModel() {

    val tags = tagRepository.getTagsWithCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun renameTag(id: Int, newName: String) = viewModelScope.launch {
        tagRepository.renameTag(id, newName)
    }

    fun deleteTag(id: Int) = viewModelScope.launch {
        tagRepository.deleteTag(id)
    }

    fun mergeTags(fromId: Int, toId: Int) = viewModelScope.launch {
        tagRepository.mergeTags(fromId, toId)
    }
}
