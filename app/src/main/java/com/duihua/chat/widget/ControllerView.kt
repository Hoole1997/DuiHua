package com.duihua.chat.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.duihua.chat.R
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.databinding.ViewControllerBinding

/**
 * create by libo
 * create on 2020-05-20
 * description
 */
class ControllerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs), View.OnClickListener {
    private var listener: OnVideoControllerListener? = null
    private var videoData: ExploreContent? = null
    private var binding: ViewControllerBinding = ViewControllerBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        init()
    }

    private fun init() {
        binding.ivHead!!.setOnClickListener(this)
        binding.tvLikecount!!.setOnClickListener(this)
        binding.tvCommentcount!!.setOnClickListener(this)
//        binding.rlLike!!.setOnClickListener(this)
//        binding.ivFocus!!.setOnClickListener(this)
//        setRotateAnim()
    }

    fun setVideoData(videoData: ExploreContent) {
        this.videoData = videoData
        Glide.with(context)
            .load(videoData.profileURL)
            .placeholder(R.mipmap.ic_default_avatar)
            .error(R.mipmap.ic_default_avatar)
            .into(binding.ivHead)
        binding.tvMediaSource.text = videoData.mediaSource
        binding.tvNickname!!.text = "@" + videoData.nickName
        binding.autoLinkTextView.text = videoData.mediaContent
//        AutoLinkHerfManager.setContent(videoData.content, binding.autoLinkTextView)
//        binding.ivHeadAnim!!.setImageResource(videoData.userBean!!.head)
//        binding.tvLikecount!!.text = NumUtils.numberFilter(videoData.likeCount)
        binding.tvCommentcount!!.text = videoData.totalComment.toString()
//        binding.tvSharecount!!.text = NumUtils.numberFilter(videoData.shareCount)
//        binding.animationView!!.setAnimation("like.json")

//        //点赞状态
//        if (videoData.isFavorite) {
//            binding.ivLike!!.setTextColor(Color.parseColor("#ff2c54"))
//        } else {
//            binding.ivLike!!.setTextColor(Color.parseColor("#ffffff"))
//        }

        //关注状态
//        if (videoData.isFocused) {
//            binding.ivFocus!!.visibility = GONE
//        } else {
//            binding.ivFocus!!.visibility = VISIBLE
//        }
    }

    fun setListener(listener: OnVideoControllerListener?) {
        this.listener = listener
    }

    override fun onClick(v: View) {
        if (listener == null) {
            return
        }
        when (v.id) {
            R.id.ivHead -> listener!!.onHeadClick()
            R.id.tvCommentcount -> listener?.onCommentClick()
            R.id.tvLikecount -> listener?.onLikeClick()
//            R.id.rlLike -> {
//                listener!!.onLikeClick()
//                like()
//            }
//            R.id.ivComment -> listener!!.onCommentClick()
//            R.id.ivShare -> listener!!.onShareClick()
//            R.id.ivFocus -> if (!videoData!!.isFocused) {
//                videoData!!.isLiked = true
//                binding.ivFocus!!.visibility = GONE
//            }
        }
    }

    /**
     * 点赞动作
     */
    fun like() {
//        if (!videoData!!.isLiked) {
//            //点赞
//            binding.animationView!!.visibility = VISIBLE
//            binding.animationView!!.playAnimation()
//            binding.ivLike!!.setTextColor(resources.getColor(R.color.color_FF0041))
//        } else {
//            //取消点赞
//            binding.animationView!!.visibility = INVISIBLE
//            binding.ivLike!!.setTextColor(resources.getColor(R.color.white))
//        }
//        videoData!!.isLiked = !videoData!!.isLiked
    }

    /**
     * 循环旋转动画
     */
    private fun setRotateAnim() {
        val rotateAnimation = RotateAnimation(0f, 359f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.duration = 8000
        rotateAnimation.interpolator = LinearInterpolator()
//        binding.rlRecord!!.startAnimation(rotateAnimation)
    }
}