package com.drostudio.yeopjarisunbae.demo

import com.drostudio.yeopjarisunbae.core.model.NaviState
import com.drostudio.yeopjarisunbae.core.model.VehicleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 시나리오를 시간 순서대로 재생해 차량·내비 상태를 내보낸다. */
class DemoPlayer(private val frames: List<DemoFrame>) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    private val _vehicle = MutableStateFlow(frames.first().vehicle)
    val vehicle: StateFlow<VehicleState> = _vehicle
    private val _navi = MutableStateFlow(frames.first().navi)
    val navi: StateFlow<NaviState> = _navi
    private val _elapsedSec = MutableStateFlow(0)
    val elapsedSec: StateFlow<Int> = _elapsedSec

    fun play() {
        if (job?.isActive == true) return
        job = scope.launch {
            var now = 0
            for (frame in frames) {
                delay((frame.atSec - now) * 1000L)
                now = frame.atSec
                _elapsedSec.value = now
                _vehicle.value = frame.vehicle
                _navi.value = frame.navi
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}