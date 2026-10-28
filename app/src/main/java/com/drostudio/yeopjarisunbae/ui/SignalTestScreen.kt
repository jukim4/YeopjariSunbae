package com.drostudio.yeopjarisunbae.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drostudio.yeopjarisunbae.navi.NaviSource
import com.drostudio.yeopjarisunbae.vehicle.VehicleSource

/** API 연결 확인용 화면. Virtual Console·내비 조작에 따라 값이 바뀌면 성공. */
@Composable
fun SignalTestScreen(vehicle: VehicleSource, navi: NaviSource) {
    val v by vehicle.state.collectAsState()
    val n by navi.state.collectAsState()
    val vehicleError by vehicle.lastError.collectAsState()
    val naviError by navi.lastError.collectAsState()

    Row(
        modifier = Modifier.padding(40.dp),
        horizontalArrangement = Arrangement.spacedBy(80.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("차량", fontSize = 28.sp)
            Text("브레이크: ${if (v.brakePressed) "밟음" else "안 밟음"}", fontSize = 22.sp)
            Text("방향지시등: ${v.turnSignal}", fontSize = 22.sp)
            Text("기어: ${v.gear}", fontSize = 22.sp)
            Text("속도: ${"%.0f".format(v.speedKmh)} km/h", fontSize = 22.sp)
            vehicleError?.let { Text(it, fontSize = 16.sp, color = Color.Red) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("내비", fontSize = 28.sp)
            Text("연결: ${if (n.connected) "됨" else "안 됨"} / 상태: ${n.status}", fontSize = 22.sp)
            Text(
                "다음 회전: ${n.nextTurn?.let { "${it.typeName} ${"%.0f".format(it.distanceM)}m" } ?: "-"}",
                fontSize = 22.sp,
            )
            Text("방면: ${n.nextTurn?.description?.ifBlank { "-" } ?: "-"}", fontSize = 22.sp)
            Text("회전 목록 개수: ${n.tbtCount}", fontSize = 22.sp)
            Text("목적지: ${n.destinationName ?: "-"} (${n.destinationRemain ?: "-"})", fontSize = 22.sp)
            naviError?.let { Text(it, fontSize = 16.sp, color = Color.Red) }
        }
    }
}