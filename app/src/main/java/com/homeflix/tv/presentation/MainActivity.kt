package com.homeflix.tv.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.homeflix.tv.HomeFlixTVApplication
import com.homeflix.tv.presentation.navigation.AppNavigation
import com.homeflix.tv.presentation.theme.HomeFlixTheme
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    /** Set by AppNavigation to handle long-press BACK → Home. */
    var onNavigateHome: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as HomeFlixTVApplication).container

        setContent {
            HomeFlixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0)
                ) {
                    val isConfigured by container.settingsRepository.isConfigured.collectAsState(initial = null)
                    AppNavigation(
                        container = container,
                        isConfigured = isConfigured
                    )
                }
            }
        }
    }
}
