package com.duihua.chat.ui.auth

import androidx.activity.viewModels
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityBeautyBinding

class BeautyActivity : BaseActivity<ActivityBeautyBinding, AuthModel>() {
    override fun initBinding(): ActivityBeautyBinding {
        return ActivityBeautyBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"")
    }

    override fun initViewModel(): AuthModel? {
        return viewModels<AuthModel>().value
    }

    override fun initObserve() {

    }
}