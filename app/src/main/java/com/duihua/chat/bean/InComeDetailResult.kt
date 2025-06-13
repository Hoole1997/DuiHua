package com.duihua.chat.bean

import android.graphics.Path

data class InComeDetailResult(
    val content: List<InComeDetail>
)

data class InComeDetail(
    val id: Long,
    val userID: Long,
    val currency: Double,
    val operate: String,
    val operateStatus: String,
    val createDate: String
) {
    fun operate() : Operate{
        return if (operate == Operate.INCOME.name) {
            return Operate.INCOME
        } else {
            return Operate.WITHDRAW
        }
    }

    fun operateStatus() : OperateStatus{
        return if (operateStatus == OperateStatus.SUBMIT.name) {
            return OperateStatus.SUBMIT
        } else if (operateStatus == OperateStatus.REJECT.name){
            return OperateStatus.REJECT
        } else {
            return OperateStatus.FINISH
        }
    }
}

enum class Operate {
    INCOME,
    WITHDRAW
}

enum class OperateStatus {
    SUBMIT,
    REJECT,
    FINISH
}