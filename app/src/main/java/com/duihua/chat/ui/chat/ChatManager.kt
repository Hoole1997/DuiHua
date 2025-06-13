@file:Suppress("DEPRECATION")

package com.duihua.chat.ui.chat

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.drake.net.Net
import com.drake.net.utils.scopeNet
import com.duihua.chat.bean.IMMessage
import com.duihua.chat.net.NetApi
import com.duihua.chat.net.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import cn.jiguang.imui.commons.models.IMessage
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.db.ChatDatabase

interface IMMessageListener {
    fun onMessage(message: IMMessage)
}

object ChatManager {
    private val listeners = CopyOnWriteArraySet<IMMessageListener>()
    private var webSocket: WebSocket? = null
    private var isConnecting = false
    private var reconnectCount = 0
    private val maxReconnectAttempts = 5
    private val reconnectInterval = 3000L // 3秒重连间隔

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS) // 设置心跳间隔
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    fun addMessageListener(listener: IMMessageListener, lifecycleOwner: LifecycleOwner? = null) {
        listeners.add(listener)
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                removeMessageListener(listener)
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        })
    }

    fun removeMessageListener(listener: IMMessageListener) {
        listeners.remove(listener)
    }

    private suspend fun getImUrl(): String? {
        return Net.get(NetApi.API_IM_URL).toResult<String>().getOrNull()
    }

    private fun connectIMSocket() {
        if (isConnecting) return
        isConnecting = true

        scopeNet(Dispatchers.IO) {
            try {
                val url = getImUrl()
                if (url?.isEmpty() == true) {
                    isConnecting = false
                    return@scopeNet
                }

                val request = Request.Builder()
                    .url("ws://139.224.213.222:10188/ws/login?Duihua-token=${UserManager.token()}")
                    .build()

                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)
                        LogUtils.d("ChatManager: onOpen---" + response.message)
                        isConnecting = false
                        reconnectCount = 0 // 重置重连次数
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        super.onMessage(webSocket, text)
                        LogUtils.d("ChatManager: onMessage---" + text)
                        val message = GsonUtils.fromJson<IMMessage>(text, IMMessage::class.java)
                        ChatDatabase.getDatabase().chatDao().insertOrUpdateMessage(message)
                        mainHandler.post {
                            listeners.forEach { it.onMessage(message) }
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        LogUtils.d("ChatManager: onClosed---code:$code ---reason:$reason")
                        isConnecting = false
                        handleReconnect()
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        super.onFailure(webSocket, t, response)
                        t.printStackTrace()
                        isConnecting = false
                        handleReconnect()
                    }
                }

                webSocket = client.newWebSocket(request, listener)
            } catch (e: Exception) {
                LogUtils.e("ChatManager: connect error---${e.message}")
                isConnecting = false
                handleReconnect()
            }
        }
    }

    private fun handleReconnect() {
        if (reconnectCount >= maxReconnectAttempts) {
            LogUtils.e("ChatManager: 达到最大重连次数")
            return
        }

        scopeNet(Dispatchers.IO) {
            reconnectCount++
            LogUtils.d("ChatManager: 开始第 $reconnectCount 次重连")
            delay(reconnectInterval)
            connectIMSocket()
        }
    }

    fun loginIM() {
        connectIMSocket()
    }

    fun disconnect() {
        webSocket?.close(1000, "正常关闭")
        webSocket = null
        isConnecting = false
        reconnectCount = 0
    }

    /**
     * 发送文本消息
     * @param localUserInfo 发送者信息
     * @param toUserInfo 接收者信息
     * @param text 消息内容
     * @param onResult 发送结果回调 (message, success)
     */
    fun sendTextMessage(
        localUserInfo: OtherUserInfo,
        toUserInfo: OtherUserInfo,
        text: String,
        onResult: (IMMessage, Boolean) -> Unit
    ) {
        if (text.isBlank()) {
            LogUtils.e("ChatManager: 消息内容不能为空")
            return
        }

        if (webSocket == null) {
            LogUtils.e("ChatManager: WebSocket未连接")
            return
        }

        scopeNet(Dispatchers.IO) {
            try {
                // 创建消息对象
                val messageBean = createTextMessage(localUserInfo, toUserInfo, text)
                // 转换为JSON
                val messageJson = createMessageJson(messageBean)
                // 发送消息
                val sendResult = webSocket?.send(messageJson) ?: false
                
                // 更新消息状态
                if (sendResult) {
                    messageBean.sent = true
                    messageBean.duration = "1" // 发送成功
                    // 保存到数据库
                    ChatDatabase.getDatabase().chatDao().insertOrUpdateMessage(messageBean)
                }
                // 回调结果
                withContext(Dispatchers.Main) {
                    onResult.invoke(messageBean, sendResult)
                }
            } catch (e: Exception) {
                LogUtils.e("ChatManager: 发送消息失败---${e.message}")
                withContext(Dispatchers.Main) {
                    onResult.invoke(createTextMessage(localUserInfo, toUserInfo, text), false)
                }
            }
        }
    }

    /**
     * 创建文本消息对象
     */
    private fun createTextMessage(
        localUserInfo: OtherUserInfo,
        toUserInfo: OtherUserInfo,
        text: String
    ): IMMessage {
        return IMMessage(
            messageId = IMMessage.generateMessageId(),
            type = 1,
            roomId = null,
            fromUserId = localUserInfo.id,
            fromUserName = localUserInfo.showNickName(),
            toUserId = toUserInfo.id,
            toUserName = toUserInfo.showNickName(),
            content = text,
            fileSize = "0",
            duration = "1", // 初始状态为未发送
            location_x = 0.0,
            location_y = 0.0,
            timestamp = "${System.currentTimeMillis()}000".toLong(),
            sent = false,
            version = null
        ).apply {
            toUser = localUserInfo
            fromUser = toUserInfo
        }
    }

    /**
     * 创建消息JSON
     */
    private fun createMessageJson(message: IMMessage): String {
        return GsonUtils.toJson(mutableMapOf<String, Any?>().apply {
            put("id", null)
            put("messageId", message.messageId)
            put("type", message.type)
            put("roomId", message.roomId)
            put("fromUserId", message.fromUserId)
            put("fromUserName", message.fromUserName)
            put("toUserId", message.toUserId)
            put("toUserName", message.toUserName)
            put("content", message.content)
            put("fileSize", message.fileSize)
            put("duration", message.duration)
            put("location_x", message.location_x)
            put("location_y", message.location_y)
            put("timestamp", message.timestamp)
            put("sent", message.sent)
            put("version", message.version)
        })
    }
}