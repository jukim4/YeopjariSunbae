package com.drostudio.yeopjarisunbae.navi

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.data.CautionPoint
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.util.Log
import com.drostudio.yeopjarisunbae.core.model.NaviState
import com.drostudio.yeopjarisunbae.core.model.NextTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** 내비 입력 */
class PleosNaviSource(context: Context) : NaviSource {

    private val navi = NaviHelper(context.applicationContext)

    private val _state = MutableStateFlow(NaviState())
    override val state: StateFlow<NaviState> = _state
    private val _lastError = MutableStateFlow<String?>(null)
    override val lastError: StateFlow<String?> = _lastError

    private val listener = object : NaviHelperEventListener {

        override fun onTBTInfo(tbtInfoList: List<TBTInfo>) {
            // 첫 항목만 다음 회전으로 사용
            val head = tbtInfoList.firstOrNull()
            _state.update {
                it.copy(
                    tbtCount = tbtInfoList.size,
                    nextTurn = head?.let { t ->
                        NextTurn(t.type.name, t.distance.toDouble(), t.description ?: "")
                    },
                )
            }
            Log.i(TAG, "onTBTInfo n=${tbtInfoList.size} " +
                tbtInfoList.take(3).joinToString(" | ") { "${it.type.name} ${it.distance}m '${it.description}'" })
        }

        override fun onDestinationInfo(destinationInfo: DestinationInfo) {
            _state.update {
                it.copy(
                    destinationName = destinationInfo.name,
                    destinationRemain = "${destinationInfo.remainDistance}",
                )
            }
        }

        override fun onRoutingCompleted() {
            runCatching { navi.getTBTInfo(); navi.getDestinationInfo() }
        }

        override fun onNavigationStatus(status: NavigationStatus) {
            _state.update { it.copy(status = status.name) }
        }

        override fun onRouteStarted(routeStartInfo: RouteStartInfo) {
            Log.i(TAG, "onRouteStarted $routeStartInfo")
        }

        override fun onCautionPointInfo(cautionPointList: List<CautionPoint>) {
            Log.i(TAG, "onCautionPointInfo n=${cautionPointList.size}")
        }

        override fun onError(errorCode: NaviErrorCode) {
            fail("event", Exception(errorCode.name))
        }
    }



    override fun start() {
        runCatching {
            navi.initialize(
                {
                    _state.update { it.copy(connected = true) }
                    navi.getTBTInfo()
                    navi.getDestinationInfo()
                },
                { e -> fail("init", e) },
            )
            navi.addListener(listener)
        }.onFailure { fail("init", it) }
    }

    override fun stop() {
        runCatching {
            navi.removeListener(listener)
            navi.release()
        }
    }

    private fun fail(tag: String, e: Throwable) {
        Log.w(TAG, "[navi/$tag] ${e.message}", e)
        _lastError.value = "[navi/$tag] ${e.message}"
    }

    companion object {
        private const val TAG = "YeopjariSunbae"
    }
}