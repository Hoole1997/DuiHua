package com.duihua.chat.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Post
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.net.NetApi
import com.duihua.chat.net.UserManager
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

class SettingModel: ViewModel() {
    
    private val updateFlag = AtomicBoolean(false)
    val userInfoUpdateEvent = MutableLiveData<UserInfo?>()
    
    /**
     * 更新用户手机号搜索设置
     */
    fun updateSearchByPhone(canSearchByPhone: Boolean) {
        if (updateFlag.get()) return
        updateFlag.set(true)
        
        scopeNetLife {
            Post<Any?>(NetApi.API_USER_BASIC_EDIT) {
                json("canSearchByPhone" to canSearchByPhone)
            }.await()
            
            // 更新本地用户信息
            val userInfo = UserManager.userInfo()
            if (userInfo != null) {
                userInfo.canSearchByPhone = canSearchByPhone
                UserManager.updateUserInfo(userInfo)
                userInfoUpdateEvent.setValue(userInfo)
            }
        }.finally({
            updateFlag.set(false)
        })
    }
    
    /**
     * 更新粉丝数据可见性设置
     */
    fun updateFansNumberVisibility(visibility: String) {
        if (updateFlag.get()) return
        updateFlag.set(true)
        
        scopeNetLife {
            Post<Any?>(NetApi.API_USER_BASIC_EDIT) {
                json("canSeeFansNumber" to visibility)
            }.await()
            
            // 更新本地用户信息
            val userInfo = UserManager.userInfo()
            if (userInfo != null) {
                userInfo.canSeeFansNumber = visibility
                UserManager.updateUserInfo(userInfo)
                userInfoUpdateEvent.setValue(userInfo)
            }
        }.finally({
            updateFlag.set(false)
        })
    }
    
    /**
     * 更新粉丝列表可见性设置
     */
    fun updateFansListVisibility(visibility: String) {
        if (updateFlag.get()) return
        updateFlag.set(true)
        
        scopeNetLife {
            Post<Any?>(NetApi.API_USER_BASIC_EDIT) {
                json("canSeeFansList" to visibility)
            }.await()
            
            // 更新本地用户信息
            val userInfo = UserManager.userInfo()
            if (userInfo != null) {
                userInfo.canSeeFansList = visibility
                UserManager.updateUserInfo(userInfo)
                userInfoUpdateEvent.setValue(userInfo)
            }
        }.finally({
            updateFlag.set(false)
        })
    }
    
    /**
     * 一次性更新多个设置
     */
    fun updateSettings(jsonObject: JSONObject) {
        if (updateFlag.get()) return
        updateFlag.set(true)
        
        scopeNetLife {
            Post<Any?>(NetApi.API_USER_BASIC_EDIT) {
                json(jsonObject)
            }.await()
            
            // 更新本地用户信息
            refreshUserInfo()
        }.finally({
            updateFlag.set(false)
        })
    }
    
    /**
     * 刷新用户信息
     */
    fun refreshUserInfo() {
        val userInfo = UserManager.userInfo()
        if (userInfo != null) {
            userInfoUpdateEvent.setValue(userInfo)
        }
    }
}