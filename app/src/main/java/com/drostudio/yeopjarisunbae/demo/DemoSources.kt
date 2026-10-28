package com.drostudio.yeopjarisunbae.demo

import com.drostudio.yeopjarisunbae.core.model.NaviState
import com.drostudio.yeopjarisunbae.core.model.VehicleState
import com.drostudio.yeopjarisunbae.navi.NaviSource
import com.drostudio.yeopjarisunbae.vehicle.VehicleSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 데모 재생값을 차량 입력으로 내보낸다. 앱은 실제 차량과 구분 없이 같은 방식으로 받는다. */
class DemoVehicleSource(private val player: DemoPlayer) : VehicleSource {
    override val state: StateFlow<VehicleState> = player.vehicle
    override val lastError: StateFlow<String?> = MutableStateFlow(null)
    override fun start() = player.play()
    override fun stop() = player.stop()
}

class DemoNaviSource(private val player: DemoPlayer) : NaviSource {
    override val state: StateFlow<NaviState> = player.navi
    override val lastError: StateFlow<String?> = MutableStateFlow(null)
    override fun start() = player.play()
    override fun stop() = player.stop()
}