package com.duihua.chat.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.net.NetApi

class ChatModel : ViewModel() {

    val userinfoLiveData = MutableLiveData<Pair<OtherUserInfo,OtherUserInfo>>()
    fun refreshInfo(otherUserId: Long,localUserId: Long) {
        scopeNetLife {
            val otherUser = Get<OtherUserInfo>(NetApi.API_OTHER_USER) {
                param("otherUserID", otherUserId)
            }.await()
            val localUserInfo = Get<OtherUserInfo>(NetApi.API_OTHER_USER) {
                param("otherUserID", localUserId)
            }.await()
            userinfoLiveData.postValue(Pair<OtherUserInfo, OtherUserInfo>(otherUser,localUserInfo))
        }
    }
}