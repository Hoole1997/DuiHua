package com.duihua.chat.ui.quick

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.duihua.chat.R
import com.duihua.chat.bean.SearchAccount

class QuickFriendAdapter : BaseDifferAdapter<SearchAccount, QuickViewHolder>(object : DiffUtil.ItemCallback<SearchAccount>() {
    override fun areItemsTheSame(
        oldItem: SearchAccount,
        newItem: SearchAccount
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SearchAccount,
        newItem: SearchAccount
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun getChangePayload(
        oldItem: SearchAccount,
        newItem: SearchAccount
    ): Any? {
        return super.getChangePayload(oldItem, newItem)
    }
}) {
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: SearchAccount?
    ) {
        item?.let {
            Glide.with(context)
                .load(item.profileURL)
                .error(R.mipmap.ic_default_avatar)
                .into(holder.getView(R.id.iv_avatar))
            holder.setText(R.id.tv_nickname,"${item.phone}"+if (item.nickName.isNullOrBlank()){
                ""
            } else {
                "(${item.nickName})"
            })
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_search_account,parent)
    }


}