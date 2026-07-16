package com.homeflix.tv.presentation.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.homeflix.tv.HomeFlixTVApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val container get() = (getApplication<HomeFlixTVApplication>()).container
    val settings get() = container.settingsRepository
}
