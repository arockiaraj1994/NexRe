package com.mindshift.nexre

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexre_prefs")
private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val showOnboarding = context.dataStore.data
        .map { prefs -> !(prefs[ONBOARDING_DONE] ?: false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun completeOnboarding() = viewModelScope.launch {
        context.dataStore.edit { it[ONBOARDING_DONE] = true }
    }
}
