package com.duihua.chat.ui.mine

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.duihua.chat.R
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.bean.SearchAccount
import com.duihua.chat.bean.UserMedia

class UserMediaAdapter : BaseDifferAdapter<ExploreContent, QuickViewHolder>(object : DiffUtil.ItemCallback<ExploreContent>() {
    override fun areItemsTheSame(
        oldItem: ExploreContent,
        newItem: ExploreContent
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ExploreContent,
        newItem: ExploreContent
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun getChangePayload(
        oldItem: ExploreContent,
        newItem: ExploreContent
    ): Any? {
        return super.getChangePayload(oldItem, newItem)
    }
}) {
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: ExploreContent?
    ) {
        item?.let {
            Glide.with(context)
                .load(item.coverURL)
                .placeholder(R.mipmap.bg_mine_default)
                .error(R.mipmap.bg_mine_default)
                .into(holder.getView(R.id.iv_cover))
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_user_media,parent)
    }


}