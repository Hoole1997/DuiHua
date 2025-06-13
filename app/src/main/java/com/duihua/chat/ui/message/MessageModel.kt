package com.duihua.chat.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.net.NetApi

class MessageModel : ViewModel() {

    fun refreshInfo(userId: Long,onResult:(OtherUserInfo) -> Unit) {
        scopeNetLife {
            val otherUser = Get<OtherUserInfo>(NetApi.API_OTHER_USER) {
                param("otherUserID", userId)
            }.await()
            onResult.invoke(otherUser)
        }
    }

}