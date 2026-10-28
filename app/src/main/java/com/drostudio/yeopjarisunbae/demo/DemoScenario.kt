package com.drostudio.yeopjarisunbae.demo

import com.drostudio.yeopjarisunbae.core.model.Gear
import com.drostudio.yeopjarisunbae.core.model.NaviState
import com.drostudio.yeopjarisunbae.core.model.NextTurn
import com.drostudio.yeopjarisunbae.core.model.TurnSignal
import com.drostudio.yeopjarisunbae.core.model.VehicleState

/** 특정 시각(초)의 차량·내비 상태 한 장면 */
data class DemoFrame(
    val atSec: Int,
    val vehicle: VehicleState,
    val navi: NaviState,
)
object DemoScenario {
    /**
     * 우회전 시나리오 (약 50초)
     * 0초 P 정차 → 2초 D 출발 → 시속 36km(초당 10m)로 우회전 지점 400m 앞에서 접근
     * → 60m 전 오른쪽 깜빡이 → 20m 전 정지선에서 2초 일시정지 → 우회전 → 도착 후 P
     */
    fun rightTurn(): List<DemoFrame> {
        val frames = mutableListOf<DemoFrame>()
        val turnName = "TURN_RIGHT"
        val desc = "시청 방면"

        fun navi(distance: Double?) = NaviState(
            connected = true,
            status = "DEMO",
            nextTurn = distance?.let { NextTurn(turnName, it, desc) },
            tbtCount = if (distance == null) 0 else 1,
            destinationName = "데모 목적지",
        )

        // 0~1초: 주차 상태
        frames += DemoFrame(0, VehicleState(gear = Gear.PARK), navi(400.0))
        // 2초: 기어 D, 브레이크 밟은 채 출발 준비
        frames += DemoFrame(2, VehicleState(gear = Gear.DRIVE, brakePressed = true), navi(400.0))

        // 3~38초: 초당 10m로 접근 (400m → 50m), 60m부터 오른쪽 깜빡이
        for (t in 3..38) {
            val distance = 400.0 - (t - 3) * 10
            frames += DemoFrame(
                t,
                VehicleState(
                    gear = Gear.DRIVE,
                    speedKmh = 36f,
                    turnSignal = if (distance <= 60) TurnSignal.RIGHT else TurnSignal.OFF,
                ),
                navi(distance),
            )
        }

        // 39~40초: 감속하며 정지선 접근 (50m → 20m)
        frames += DemoFrame(39, VehicleState(Gear.DRIVE.let { false }, TurnSignal.RIGHT, Gear.DRIVE, 15f), navi(35.0))
        frames += DemoFrame(40, VehicleState(true, TurnSignal.RIGHT, Gear.DRIVE, 5f), navi(20.0))

        // 41~42초: 정지선에서 일시정지 (브레이크 + 속도 0)
        frames += DemoFrame(41, VehicleState(true, TurnSignal.RIGHT, Gear.DRIVE, 0f), navi(20.0))
        frames += DemoFrame(42, VehicleState(true, TurnSignal.RIGHT, Gear.DRIVE, 0f), navi(20.0))

        // 43~45초: 우회전 통과
        frames += DemoFrame(43, VehicleState(false, TurnSignal.RIGHT, Gear.DRIVE, 10f), navi(10.0))
        frames += DemoFrame(44, VehicleState(false, TurnSignal.RIGHT, Gear.DRIVE, 15f), navi(0.0))
        frames += DemoFrame(45, VehicleState(false, TurnSignal.OFF, Gear.DRIVE, 25f), navi(null))

        // 48초: 도착, 정차 후 P
        frames += DemoFrame(48, VehicleState(true, TurnSignal.OFF, Gear.DRIVE, 0f), navi(null))
        frames += DemoFrame(50, VehicleState(true, TurnSignal.OFF, Gear.PARK, 0f), navi(null))

        return frames
    }
}