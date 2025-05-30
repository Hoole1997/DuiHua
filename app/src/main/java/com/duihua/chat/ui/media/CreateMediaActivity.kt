package com.duihua.chat.ui.media

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityCreateMediaBinding
import com.duihua.chat.util.test.setupWithStringList

class CreateMediaActivity : BaseActivity<ActivityCreateMediaBinding, MediaModel>() {

    companion object {
        fun launch(context: Context,isImage: Boolean) {
            context.startActivity(Intent().apply {
                setClass(context, CreateMediaActivity::class.java)
                putExtra("isImage",isImage)
            })
        }
    }

    override fun initBinding(): ActivityCreateMediaBinding {
        return ActivityCreateMediaBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"发布作品")

        binding.spinnerSource.setupWithStringList(arrayListOf("自行拍摄","网络取材")) {position: Int, item: String ->

        }
        binding.spinnerSee.setupWithStringList(arrayListOf("公开","仅互关朋友","仅自己")) {position: Int, item: String ->

        }
        binding.spinnerTheme.setupWithStringList(arrayListOf("吃瓜","搞笑娱乐","影视","游戏","美食","旅行","穿搭","健康","其他")) {position: Int, item: String ->

        }
    }

    override fun initViewModel(): MediaModel? {
        return viewModels<MediaModel>().value
    }

    override fun initObserve() {

    }

}