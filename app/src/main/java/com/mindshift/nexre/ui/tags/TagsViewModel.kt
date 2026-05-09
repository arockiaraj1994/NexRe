package com.mindshift.nexre.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindshift.nexre.domain.model.Tag
import com.mindshift.nexre.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(private val tagRepository: TagRepository) : ViewModel() {

    val tags = tagRepository.getTagsWithCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _renameDialogTag = MutableStateFlow<Tag?>(null)
    val renameDialogTag: StateFlow<Tag?> = _renameDialogTag.asStateFlow()

    private val _mergeSourceTag = MutableStateFlow<Tag?>(null)
    val mergeSourceTag: StateFlow<Tag?> = _mergeSourceTag.asStateFlow()

    fun openRenameDialog(tag: Tag) { _renameDialogTag.value = tag }
    fun dismissRenameDialog() { _renameDialogTag.value = null }

    fun confirmRename(newName: String) = viewModelScope.launch {
        _renameDialogTag.value?.let { tagRepository.renameTag(it.id, newName.trim()) }
        _renameDialogTag.value = null
    }

    fun openMergePicker(tag: Tag) { _mergeSourceTag.value = tag }
    fun dismissMergePicker() { _mergeSourceTag.value = null }

    fun confirmMerge(intoTag: Tag) = viewModelScope.launch {
        _mergeSourceTag.value?.let { tagRepository.mergeTags(it.id, intoTag.id) }
        _mergeSourceTag.value = null
    }

    fun deleteTag(id: Int) = viewModelScope.launch {
        tagRepository.deleteTag(id)
    }
}
