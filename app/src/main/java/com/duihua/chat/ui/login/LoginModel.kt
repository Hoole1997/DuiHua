package com.duihua.chat.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Post
import com.duihua.chat.bean.LoginResult
import com.duihua.chat.net.NetApi
import com.duihua.chat.net.UserManager
import java.util.concurrent.atomic.AtomicBoolean

class LoginModel : ViewModel() {


    private val codeFlag = AtomicBoolean(false)
    private val loginFlag = AtomicBoolean(false)

    val loginEvent = MutableLiveData<Boolean>()

    fun sendCode(phone: String) {
        if (codeFlag.get())return
        codeFlag.set(true)
        scopeNetLife {
            Post<Any?>(NetApi.API_VERIFICATION_CODE) {
                json("phone" to phone)
            }.await()
        }.finally({
            codeFlag.set(false)
        })
    }

    fun login(phone: String,code : String) {
        if (loginFlag.get())return
        loginFlag.set(true)
        scopeNetLife {
            Post<LoginResult>(NetApi.API_LOGIN) {
                json(
                    "phone" to phone,
                    "verificationCode" to code
                )
            }.await().let {
                UserManager.setToken(it.sessionID)
                loginEvent.setValue(true)
            }
        }.finally({
            loginFlag.set(false)
        })
    }

}