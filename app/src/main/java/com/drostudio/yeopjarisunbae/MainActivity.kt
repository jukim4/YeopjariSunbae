package com.drostudio.yeopjarisunbae

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.drostudio.yeopjarisunbae.demo.DemoNaviSource
import com.drostudio.yeopjarisunbae.demo.DemoPlayer
import com.drostudio.yeopjarisunbae.demo.DemoScenario
import com.drostudio.yeopjarisunbae.demo.DemoVehicleSource
import com.drostudio.yeopjarisunbae.navi.NaviSource
import com.drostudio.yeopjarisunbae.navi.PleosNaviSource
import com.drostudio.yeopjarisunbae.ui.SignalTestScreen
import com.drostudio.yeopjarisunbae.vehicle.PleosVehicleSource
import com.drostudio.yeopjarisunbae.vehicle.VehicleSource

class MainActivity : ComponentActivity() {

    /** true: 데모 시나리오 재생 / false: 실제 차량(Pleos SDK) */
    private val useDemo = false

    private lateinit var vehicle: VehicleSource
    private lateinit var navi: NaviSource

    // AndroidManifest.xml에 선언된 커스텀 권한 전부 요청 (dangerous 권한이면 반드시 필요,
    // normal/signature면 결과와 무관하게 무시됨 — 안전하게 전부 요청)
    private val runtimePermissions = arrayOf(
        "android.car.permission.CAR_SPEED",
        "pleos.car.permission.CAR_DRIVE",
        "pleos.car.permission.CAR_LIGHTS",
        "pleos.car.permission.CAR_SPEED",
        "pleos.car.permission.NAVI_ROUTE",
    )

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            startSources()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (useDemo) {
            val player = DemoPlayer(DemoScenario.rightTurn())
            vehicle = DemoVehicleSource(player)
            navi = DemoNaviSource(player)
        } else {
            vehicle = PleosVehicleSource(this)
            navi = PleosNaviSource(this)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SignalTestScreen(vehicle, navi)
                }
            }
        }

        if (useDemo) {
            startSources()
        } else {
            val missing = runtimePermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isEmpty()) startSources() else permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startSources() {
        vehicle.start()
        navi.start()
    }

    override fun onDestroy() {
        vehicle.stop()
        navi.stop()
        super.onDestroy()
    }
}