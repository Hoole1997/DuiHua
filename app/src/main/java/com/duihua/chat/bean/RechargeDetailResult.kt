package com.duihua.chat.bean

data class RechargeDetailResult(
    val content: List<RechargeDetail>
)

data class RechargeDetail(
    val totalDiamond:Int,
    val changeOrderID: String?,
    val createDate: String
)
