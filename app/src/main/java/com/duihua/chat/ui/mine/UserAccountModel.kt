package com.duihua.chat.ui.mine

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Post
import com.duihua.chat.net.NetApi
import com.duihua.chat.net.UserManager
import java.util.concurrent.atomic.AtomicBoolean

class UserAccountModel : ViewModel() {

    private val codeFlag = AtomicBoolean(false)
    private val logoutFlag = AtomicBoolean(false)
    private val changePhoneFlag = AtomicBoolean(false)

    val logoutEvent = MutableLiveData<Boolean>()
    val changePhoneEvent = MutableLiveData<Boolean>()

    /**
     * 发送验证码
     */
    fun sendCode(phone: String) {
        if (codeFlag.get()) return
        codeFlag.set(true)
        scopeNetLife {
            Post<Any?>(NetApi.API_VERIFICATION_CODE) {
                json("phone" to phone)
            }.await()
        }.finally({
            codeFlag.set(false)
        })
    }

    /**
     * 注销账号
     */
    fun logoutAccount(phone: String, code: String) {
        if (logoutFlag.get()) return
        logoutFlag.set(true)
        scopeNetLife {
            // 这里应该调用注销账号的API，这里假设接口为"/user/logout"
            Post<Any?>(NetApi.API_HOST + "/user/logout") {
                json(
                    "phone" to phone,
                    "verificationCode" to code
                )
            }.await()
            
            // 注销成功后清除用户信息
            UserManager.clearUser()
            logoutEvent.setValue(true)
        }.finally({
            logoutFlag.set(false)
        })
    }

    /**
     * 更改手机号
     */
    fun changePhone(phone: String, code: String) {
        if (changePhoneFlag.get()) return
        changePhoneFlag.set(true)
        scopeNetLife {
            // 这里应该调用更改手机号的API，这里假设接口为"/user/change-phone"
            Post<Any?>(NetApi.API_CHANGE_PHONE) {
                json(
                    "phone" to phone,
                    "verificationCode" to code
                )
            }.await()
            
            // 更新用户信息中的手机号
            val userInfo = UserManager.userInfo()
            if (userInfo != null) {
                userInfo.phone = phone
                UserManager.updateUserInfo(userInfo)
            }
            
            changePhoneEvent.setValue(true)
        }.finally({
            changePhoneFlag.set(false)
        })
    }
} 