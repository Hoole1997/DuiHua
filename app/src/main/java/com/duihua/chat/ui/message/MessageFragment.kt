package com.duihua.chat.ui.message

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.chad.library.adapter4.BaseQuickAdapter
import com.duihua.chat.R
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.bean.IMMessage
import com.duihua.chat.databinding.FragmentMessageBinding
import com.duihua.chat.db.ChatDatabase
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.chat.ChatActivity
import com.duihua.chat.ui.chat.ChatManager
import com.duihua.chat.ui.chat.IMMessageListener
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class MessageFragment : BaseFragment<FragmentMessageBinding, MessageModel>() {

    lateinit var messageAdapter: RecentMessageAdapter
    private val currentUserId = UserManager.userInfo()?.id ?: 0L

    override fun initBinding(): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {
        messageAdapter = RecentMessageAdapter()
        messageAdapter.isStateViewEnable = true
        messageAdapter.setStateViewLayout(requireActivity(),R.layout.item_recent_message)
        messageAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<IMMessage> {
            override fun onClick(
                adapter: BaseQuickAdapter<IMMessage, *>,
                view: View,
                position: Int
            ) {
                val item = messageAdapter.getItem(position) ?:return
                ChatActivity.launch(requireActivity(),item.fromUserId)
            }
        })
        binding.rvMessage.adapter = messageAdapter
        loadRecentMessages()
    }

    override fun initViewModel(): MessageModel? {
        return viewModels<MessageModel>().value
    }

    override fun initObserve() {
        // 添加消息监听
        ChatManager.addMessageListener(object : IMMessageListener {
            override fun onMessage(message: IMMessage) {
                // 获取当前列表
                val currentList = messageAdapter.items.toMutableList()
                
                // 查找是否存在相同发送者的消息
                val existingIndex = currentList.indexOfFirst { it.fromUserId == message.fromUserId }
                
                if (existingIndex != -1) {
                    // 如果存在，更新该位置的消息，但保持原有的用户信息和messageId
                    val existingMessage = currentList[existingIndex]
                    val updatedMessage = message.copy().apply {
                        // 保持原有的用户信息
                        fromUser = existingMessage.fromUser
                        // 保持原有的messageId
                        messageId = existingMessage.messageId
                    }
                    currentList[existingIndex] = updatedMessage
                    messageAdapter.submitList(currentList)
                } else {
                    // 如果不存在，重新加载列表
                    loadRecentMessages()
                }
            }
        }, this)
    }

    private fun loadRecentMessages() {
        lifecycleScope.launch {
            try {
                // 直接获取每个用户的最新消息
                val latestMessages = ChatDatabase.getDatabase().chatDao().getLatestMessagesForEachUser(currentUserId)
                
                // 创建一个可变的列表用于后续更新
                val messageList = latestMessages.toMutableList()
                
                // 先提交初始列表
                messageAdapter.submitList(messageList)
                
                // 使用 AtomicInteger 追踪异步请求完成情况
                val pendingRequests = AtomicInteger(messageList.size)
                val updatedList = messageList.toMutableList()
                
                // 加载用户信息
                messageList.forEachIndexed { index, message ->
                    model?.refreshInfo(message.fromUserId) { userInfo ->
                        // 使用 copy 创建新对象
                        updatedList[index] = message.copy().apply { fromUser = userInfo }
                        
                        // 减少待处理请求计数
                        if (pendingRequests.decrementAndGet() == 0) {
                            // 所有请求都完成后，一次性提交更新后的列表
                            messageAdapter.submitList(updatedList)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .titleBar(binding.toolbar)
            .init()
    }
}