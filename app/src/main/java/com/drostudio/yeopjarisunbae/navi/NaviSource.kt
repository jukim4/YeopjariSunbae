package com.drostudio.yeopjarisunbae.navi

import com.drostudio.yeopjarisunbae.core.model.NaviState
import kotlinx.coroutines.flow.StateFlow

interface NaviSource {
    val state: StateFlow<NaviState>
    val lastError: StateFlow<String?>
    fun start()
    fun stop()
}