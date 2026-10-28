package com.drostudio.yeopjarisunbae.core.model

enum class TurnSignal { OFF, LEFT, RIGHT }
enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE, UNKNOWN }

/** 차량 신호의 현재 값*/
data class VehicleState (
    val brakePressed: Boolean = false,
    val turnSignal: TurnSignal = TurnSignal.OFF,
    val gear: Gear = Gear.UNKNOWN,
    val speedKmh: Float = 0f,
)