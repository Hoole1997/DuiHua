package com.duihua.chat.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.drake.net.Post
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.net.NetApi

class AuthModel : ViewModel() {

    val userInfoEvent = MutableLiveData<UserInfo>()

    fun refreshUserInfo() {
        scopeNetLife {
            Get<UserInfo>(NetApi.API_USER_BASIC) {

            }.await().let {
                userInfoEvent.setValue(it)
            }
        }
    }

    fun updateIdentity(name: String,identityNumber: String,onResult:() -> Unit) {
        scopeNetLife {
            Post<Any?>(NetApi.API_IDENTITY) {
                json(
                    "name" to name,
                    "identityNumber" to identityNumber
                )
            }.await()
            onResult.invoke()
        }
    }

}