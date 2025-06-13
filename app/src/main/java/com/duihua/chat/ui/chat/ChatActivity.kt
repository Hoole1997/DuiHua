package com.duihua.chat.ui.chat

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener
import cn.jiguang.imui.chatinput.model.FileItem
import cn.jiguang.imui.messages.MsgListAdapter
import com.blankj.utilcode.util.ConvertUtils
import com.drake.softinput.setWindowSoftInput
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.bean.IMMessage
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.bean.SingleSessionConfig
import com.duihua.chat.databinding.ActivityChatBinding
import com.duihua.chat.db.ChatDatabase
import com.duihua.chat.net.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class ChatActivity : BaseActivity<ActivityChatBinding, ChatModel>() {

    companion object {
        fun launch(context: Context, userId: Long) {
            context.startActivity(Intent().apply {
                setClass(context, ChatActivity::class.java)
                putExtra("userId", userId)
            })
        }
    }

    lateinit var msgAdapter: MsgListAdapter<IMMessage>
    var userId = 0L
    private val currentUserId = UserManager.userInfo()?.id ?: 0L

    override fun initBinding(): ActivityChatBinding {
        return ActivityChatBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar, "聊天")
        userId = intent.getLongExtra("userId", 0L)

        msgAdapter = MsgListAdapter(currentUserId.toString(), ChatImageLoader())
        binding.msgList.apply {
            setWindowSoftInput(float = this,
                onChanged = {
                })
            setShowSenderDisplayName(true)
            setShowReceiverDisplayName(true)
            setAdapter<IMMessage>(msgAdapter)
        }

        binding.chatInput.apply {
            binding.chatInput.setMenuContainerHeight(ConvertUtils.dp2px(300f))
            setMenuClickListener(object : OnMenuClickListener {
                override fun onSendTextMessage(input: CharSequence?): Boolean {
                    if (input.isNullOrBlank()) return true

                    val user = model?.userinfoLiveData?.value ?:return false
                    ChatManager.sendTextMessage(user.second, user.first, input.toString()) { message, success ->
                        msgAdapter.addToStart(message, success)
                    }
                    return true
                }

                override fun onSendFiles(list: List<FileItem?>?) {
                }

                override fun switchToMicrophoneMode(): Boolean {
                    return true
                }

                override fun switchToGalleryMode(): Boolean {
                    return true
                }

                override fun switchToCameraMode(): Boolean {
                    return true
                }

                override fun switchToEmojiMode(): Boolean {
                    return true
                }
            })
            setWindowSoftInput(float = this,
                onChanged = {
                    binding.chatInput.setMenuContainerHeight(ConvertUtils.dp2px(300f))
                })
        }
    }

    private fun loadHistoryMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messages = ChatDatabase.getDatabase().chatDao().getMessagesBetweenUsers(currentUserId, userId)
                withContext(Dispatchers.Main) {
                    val user = model?.userinfoLiveData?.value ?:return@withContext
                    messages.forEach { message ->
                        message.fromUser = user.first
                        message.toUser = user.second
                        msgAdapter.addToStart(message, false)
                    }
                    binding.msgList.post {
                        if (msgAdapter.itemCount>0) {
                            binding.msgList.scrollToPosition(0)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initViewModel(): ChatModel? {
        return viewModels<ChatModel>().value
    }

    override fun initObserve() {
        model?.userinfoLiveData?.observe(this) { userInfo ->
            binding.toolbar.title = userInfo.first.showNickName()
            loadHistoryMessages()
        }
        ChatManager.addMessageListener(object : IMMessageListener {
            override fun onMessage(message: IMMessage) {
                // 只处理当前会话的消息
                if (message.fromUserId == userId && message.toUserId == currentUserId) {
                    val user = model?.userinfoLiveData?.value ?:return
                    msgAdapter.addToStart(message.apply {
                        fromUser = user.first
                        toUser = user.second
                    }, true)
                }
            }
        }, this)
        model?.refreshInfo(userId,currentUserId)
    }
}