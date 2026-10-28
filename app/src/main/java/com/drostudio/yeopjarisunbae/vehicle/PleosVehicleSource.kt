package com.drostudio.yeopjarisunbae.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.listener.BrakePedalStatusListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import com.drostudio.yeopjarisunbae.core.model.Gear
import com.drostudio.yeopjarisunbae.core.model.TurnSignal
import com.drostudio.yeopjarisunbae.core.model.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 차량 신호 입력 (실제 차량).
 * - 브레이크·방향지시등: Pleos Vehicle SDK
 * - 속도·기어: AAOS CarPropertyManager (Vehicle SDK에 속도가 없고, 기어는 SDK가 AAOS 사용을 권장)
 */
class PleosVehicleSource(context: Context) : VehicleSource {

    private val appContext = context.applicationContext
    private val vehicle = Vehicle(appContext)
    private var car: Car? = null
    private var propertyManager: CarPropertyManager? = null

    private val _state = MutableStateFlow(VehicleState())
    override val state: StateFlow<VehicleState> = _state
    private val _lastError = MutableStateFlow<String?>(null)
    override val lastError: StateFlow<String?> = _lastError

    // ---------- Pleos Vehicle SDK ----------

    private val brakeListener = object : BrakePedalStatusListener {
        override fun onBrakePedalStatusUpdated(status: Boolean?) {
            _state.update { it.copy(brakePressed = status == true) }
        }
        override fun onFailed(e: Exception) = fail("brake", e)
    }

    private val turnListener = object : TurnSignalStateListener {
        override fun onTurnSignalStateUpdated(state: TurnSignalValue?) {
            _state.update { it.copy(turnSignal = state.toTurnSignal()) }
        }
        override fun onFailed(e: Exception) = fail("turn", e)
    }

    // ---------- AAOS CarPropertyManager ----------

    private val speedCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            // PERF_VEHICLE_SPEED 단위: m/s → km/h
            (value.value as? Float)?.let { v ->
                _state.update { it.copy(speedKmh = kotlin.math.abs(v) * 3.6f) }
            }
        }
        override fun onErrorEvent(propId: Int, zone: Int) = fail("speed", Exception("error prop=$propId"))
    }

    private val gearCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            (value.value as? Int)?.let { g -> _state.update { it.copy(gear = g.toGear()) } }
        }
        override fun onErrorEvent(propId: Int, zone: Int) = fail("gear", Exception("error prop=$propId"))
    }

    override fun start() {
        startPleos()
        startCarProperties()
    }

    override fun stop() {
        safe("stop") { vehicle.getBrake().unregisterBrakePedalStatus(brakeListener) }
        safe("stop") { vehicle.getTurnSignal().unregisterTurnSignalState(turnListener) }
        safe("stop") { vehicle.release() }
        safe("stop") {
            propertyManager?.unregisterCallback(speedCallback)
            propertyManager?.unregisterCallback(gearCallback)
            car?.disconnect()
        }
    }

    private fun startPleos() {
        val ready = runCatching { vehicle.initialize() }.onFailure { fail("init", it) }.isSuccess
        if (!ready) return

        safe("brake") {
            vehicle.getBrake().getCurrentBrakePedalStatus(
                { s -> _state.update { it.copy(brakePressed = s == true) } },
                { fail("brake", it) },
            )
            vehicle.getBrake().registerBrakePedalStatus(brakeListener)
        }
        safe("turn") {
            vehicle.getTurnSignal().getCurrentTurnSignalState(
                { s -> _state.update { it.copy(turnSignal = s.toTurnSignal()) } },
                { fail("turn", it) },
            )
            vehicle.getTurnSignal().registerTurnSignalState(turnListener)
        }
    }

    /**
     * registerCallback은 Android 15(API 35)에서 deprecated 되었지만,
     * Pleos Connect Emulator는 API 34라 새 API(subscribePropertyEvents)가 없다.
     * API 34에서 동작하는 기존 API를 의도적으로 사용한다.
     */
    @Suppress("DEPRECATION")
    private fun startCarProperties() {
        safe("car") {
            car = Car.createCar(appContext)
            propertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            propertyManager?.registerCallback(
                speedCallback, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL,
            )
            propertyManager?.registerCallback(
                gearCallback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE,
            )
        }
    }

    private fun safe(tag: String, block: () -> Unit) {
        runCatching(block).onFailure { fail(tag, it) }
    }

    private fun fail(tag: String, e: Throwable) {
        Log.w(TAG, "[vehicle/$tag] ${e.message}", e)
        _lastError.value = "[vehicle/$tag] ${e.message}"
    }

    companion object {
        private const val TAG = "YeopjariSunbae"

        private fun TurnSignalValue?.toTurnSignal() = when (this) {
            TurnSignalValue.LEFT -> TurnSignal.LEFT
            TurnSignalValue.RIGHT -> TurnSignal.RIGHT
            else -> TurnSignal.OFF
        }

        private fun Int.toGear() = when (this) {
            VehicleGear.GEAR_PARK -> Gear.PARK
            VehicleGear.GEAR_REVERSE -> Gear.REVERSE
            VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
            VehicleGear.GEAR_UNKNOWN -> Gear.UNKNOWN
            else -> Gear.DRIVE // D 및 수동 단수는 모두 주행 중으로 본다
        }
    }
}