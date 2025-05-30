package com.duihua.chat

import android.app.Application
import android.util.Log.VERBOSE
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.drake.net.BuildConfig
import com.drake.net.NetConfig
import com.drake.net.exception.ResponseException
import com.drake.net.interceptor.LogRecordInterceptor
import com.drake.net.interceptor.RequestInterceptor
import com.drake.net.interfaces.NetErrorHandler
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDebug
import com.drake.net.okhttp.setErrorHandler
import com.drake.net.okhttp.setRequestInterceptor
import com.drake.net.request.BaseRequest
import com.drake.statelayout.StateConfig
import com.duihua.chat.net.GsonConverter
import com.duihua.chat.net.NetApi
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.login.ActivityLogin
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import java.util.concurrent.TimeUnit

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)
        initStateLayout()
        initNetConfig()
    }

    private fun initStateLayout() {
        StateConfig.apply {
            emptyLayout = R.layout.layout_empty
            loadingLayout = R.layout.layout_loading
            errorLayout = R.layout.layout_error
        }
    }

    private fun initNetConfig() {
        NetConfig.initialize(NetApi.API_HOST,this){
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            setDebug(BuildConfig.DEBUG)
            setConverter(GsonConverter())
            addInterceptor(
                LoggingInterceptor.Builder()
                    .setLevel(Level.BASIC)
                    .log(VERBOSE)
                    .build())
            setRequestInterceptor(object : RequestInterceptor {
                override fun interceptor(request: BaseRequest) {
                    request.setHeader("Duihua-token", UserManager.token())
                }
            })
            setErrorHandler(object : NetErrorHandler {
                override fun onError(e: Throwable) {
                    super.onError(e)
                    e.printStackTrace()
                    if (e is ResponseException) {
                        if (e.tag == "0000001") {
                            //未登录/登录失效
                            ActivityUtils.startActivity(ActivityLogin::class.java)
                            ActivityUtils.finishOtherActivities(ActivityLogin::class.java)
                        }
                    }
                }
            })
        }
    }

}