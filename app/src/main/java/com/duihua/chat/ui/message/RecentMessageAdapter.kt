package com.duihua.chat.ui.message

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.duihua.chat.R
import com.duihua.chat.bean.IMMessage
import com.duihua.chat.databinding.ItemRecentMessageBinding

class RecentMessageAdapter : BaseDifferAdapter<IMMessage, DataBindingHolder<ItemRecentMessageBinding>>(object : DiffUtil.ItemCallback<IMMessage>() {
    override fun areItemsTheSame(oldItem: IMMessage, newItem: IMMessage): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: IMMessage, newItem: IMMessage): Boolean {
        // 如果fromUser从null变为非null，认为内容不同
        if (oldItem.fromUser == null && newItem.fromUser != null) {
            return false
        }
        
        // 如果fromUser从非null变为null，认为内容不同
        if (oldItem.fromUser != null && newItem.fromUser == null) {
            return false
        }
        
        // 如果fromUser都不为null，比较具体内容
        if (oldItem.fromUser != null && newItem.fromUser != null) {
            // 如果只有content不同，其他都相同，返回true
            if (oldItem.content != newItem.content &&
                oldItem.fromUserName == newItem.fromUserName &&
                oldItem.fromUser?.profileURL == newItem.fromUser?.profileURL &&
                oldItem.timestamp == newItem.timestamp) {
                return true
            }
            
            return oldItem.content == newItem.content &&
                    oldItem.fromUserName == newItem.fromUserName &&
                    oldItem.fromUser?.profileURL == newItem.fromUser?.profileURL &&
                    oldItem.timestamp == newItem.timestamp
        }
        
        // 如果fromUser都为null，比较其他内容
        // 如果只有content不同，其他都相同，返回true
        if (oldItem.content != newItem.content &&
            oldItem.fromUserName == newItem.fromUserName &&
            oldItem.timestamp == newItem.timestamp) {
            return true
        }
        
        return oldItem.content == newItem.content &&
                oldItem.fromUserName == newItem.fromUserName &&
                oldItem.timestamp == newItem.timestamp
    }

    override fun getChangePayload(oldItem: IMMessage, newItem: IMMessage): Any? {
        // 如果fromUser从null变为非null，返回完整更新
        if (oldItem.fromUser == null && newItem.fromUser != null) {
            return null
        }
        
        // 如果fromUser从非null变为null，返回完整更新
        if (oldItem.fromUser != null && newItem.fromUser == null) {
            return null
        }
        
        // 如果fromUser都不为null，检查具体变化
        if (oldItem.fromUser != null && newItem.fromUser != null) {
            // 如果只有content不同，其他都相同，返回CONTENT
            if (oldItem.content != newItem.content &&
                oldItem.fromUserName == newItem.fromUserName &&
                oldItem.fromUser?.profileURL == newItem.fromUser?.profileURL &&
                oldItem.timestamp == newItem.timestamp) {
                return UpdateType.CONTENT
            }
            
            return when {
                oldItem.fromUser?.profileURL != newItem.fromUser?.profileURL -> UpdateType.AVATAR
                oldItem.fromUserName != newItem.fromUserName -> UpdateType.NAME
                oldItem.content != newItem.content -> UpdateType.CONTENT
                else -> null
            }
        }
        
        // 如果fromUser都为null，检查内容变化
        if (oldItem.content != newItem.content &&
            oldItem.fromUserName == newItem.fromUserName &&
            oldItem.timestamp == newItem.timestamp) {
            return UpdateType.CONTENT
        }
        
        return null
    }
}) {
    
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemRecentMessageBinding>,
        position: Int,
        item: IMMessage?
    ) {
        item?.let {
            holder.binding.apply {
                Glide.with(context).load(it.fromUser?.profileURL).placeholder(R.mipmap.ic_default_avatar).error(R.mipmap.ic_default_avatar).into(ivAvatar)
                tvNickname.text = it.fromUserName
                tvContent.text = it.content
                tvTime.text = it.getTimeString()
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): DataBindingHolder<ItemRecentMessageBinding> {
        return DataBindingHolder(R.layout.item_recent_message,parent)
    }

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemRecentMessageBinding>,
        position: Int,
        item: IMMessage?,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position, item)
            return
        }

        item?.let {
            when (payloads[0] as? UpdateType) {
                UpdateType.AVATAR -> {
                    holder.binding.ivAvatar.let { ivAvatar ->
                        Glide.with(context)
                            .load(it.fromUser?.profileURL)
                            .placeholder(R.mipmap.ic_default_avatar)
                            .error(R.mipmap.ic_default_avatar)
                            .into(ivAvatar)
                    }
                }
                UpdateType.NAME -> {
                    holder.binding.tvNickname.text = it.fromUserName
                }
                UpdateType.CONTENT -> {
                    holder.binding.apply {
                        tvContent.text = it.content
                        tvTime.text = it.getTimeString()
                    }
                }
                else -> {
                    onBindViewHolder(holder, position, item)
                }
            }
        }
    }

    /**
     * 更新类型枚举
     */
    enum class UpdateType {
        AVATAR,    // 头像更新
        NAME,      // 名称更新
        CONTENT    // 内容更新
    }
}