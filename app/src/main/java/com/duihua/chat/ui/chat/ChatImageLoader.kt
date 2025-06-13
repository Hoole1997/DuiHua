package com.duihua.chat.ui.chat

import android.widget.ImageView
import cn.jiguang.imui.commons.ImageLoader
import com.bumptech.glide.Glide
import com.duihua.chat.R

class ChatImageLoader : ImageLoader {
    override fun loadAvatarImage(
        avatarImageView: ImageView?,
        string: String?
    ) {
        avatarImageView?.let {
            Glide.with(it).load(string).placeholder(R.mipmap.ic_default_avatar).error(R.mipmap.ic_default_avatar).into(it)
        }
    }

    override fun loadImage(imageView: ImageView?, string: String?) {
        imageView?.let {
            Glide.with(it).load(string).placeholder(R.mipmap.bg_media_defalut).error(R.mipmap.bg_media_defalut).into(it)
        }
    }

    override fun loadVideo(imageCover: ImageView?, uri: String?) {

    }
}