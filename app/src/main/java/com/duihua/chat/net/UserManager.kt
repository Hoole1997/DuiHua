package com.duihua.chat.net

import android.text.TextUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.duihua.chat.bean.UserInfo

object UserManager {

    private var userInfo: UserInfo? = null

    fun updateUserInfo(userInfo: UserInfo) {
        this.userInfo = userInfo
        SPUtils.getInstance().put(NetApi.SP_USER_INFO, GsonUtils.toJson(UserManager.userInfo))
    }

    fun userInfo(): UserInfo? {
        if (userInfo == null) {
            val spUser = SPUtils.getInstance().getString(NetApi.SP_USER_INFO,"")
            if (!spUser.isNullOrBlank()) {
                userInfo = GsonUtils.fromJson<UserInfo>(spUser, UserInfo::class.java)
            }
        }
        return userInfo
    }

    fun clearUser() {
        userInfo = null
        SPUtils.getInstance().remove(NetApi.SP_USER_INFO)
        SPUtils.getInstance().remove(NetApi.SP_USER_TOKEN)
    }

    fun token(): String {
        return SPUtils.getInstance().getString(NetApi.SP_USER_TOKEN,"")
    }

    fun setToken(token: String) {
        SPUtils.getInstance().put(NetApi.SP_USER_TOKEN,token)
    }

}