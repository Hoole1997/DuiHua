package com.duihua.chat.bean

data class RechargeItem(
    val id: Long,
    val name: String,
    val price:Int,
    val rechargeType: String,
    val rechargeAmount: Int,
    val productId : String,
    val showOrder: Int,
    val isDelete: Boolean,
    val createDate: String,
    val lastModifiedDate: String
)
