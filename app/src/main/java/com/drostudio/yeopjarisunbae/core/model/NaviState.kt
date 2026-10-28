package com.drostudio.yeopjarisunbae.core.model

/** 다음 회전 하나. typeName은 테스트용으로 원문 이름을 그대로 둔다 */
data class NextTurn(
    val typeName: String,
    val distanceM: Double,
    val description: String,
)

data class NaviState(
    val connected: Boolean = false,
    val status: String = "-",
    val nextTurn: NextTurn? = null,
    val tbtCount: Int = 0,
    val destinationName: String? = null,
    val destinationRemain: String? = null,
)
