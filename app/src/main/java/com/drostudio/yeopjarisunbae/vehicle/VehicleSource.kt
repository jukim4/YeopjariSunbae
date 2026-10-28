package com.drostudio.yeopjarisunbae.vehicle

import com.drostudio.yeopjarisunbae.core.model.VehicleState
import kotlinx.coroutines.flow.StateFlow

interface VehicleSource {
    val state: StateFlow<VehicleState>
    val lastError: StateFlow<String?>
    fun start()
    fun stop()
}