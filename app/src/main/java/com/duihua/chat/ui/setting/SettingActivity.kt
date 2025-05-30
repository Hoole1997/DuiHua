package com.duihua.chat.ui.setting

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivitySettingBinding
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.login.ActivityLogin
import com.duihua.chat.util.test.setupWithStringList

class SettingActivity : BaseActivity<ActivitySettingBinding, SettingModel>() {
    override fun initBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"设置")

        binding.spinnerFansNum.setupWithStringList(arrayListOf("公开","互关的朋友","私密")) {position: Int, item: String ->

        }
        binding.spinnerFansList.setupWithStringList(arrayListOf("公开","互关的朋友","私密")) {position: Int, item: String ->

        }
        binding.itemClearCache.setOnClickListener {
            ToastUtils.showShort("清理成功")
        }
        binding.itemLogout.setOnClickListener {
            UserManager.clearUser()
            ActivityUtils.startActivity(ActivityLogin::class.java)
            ActivityUtils.finishOtherActivities(ActivityLogin::class.java)
        }
    }

    override fun initViewModel(): SettingModel? {
        return viewModels<SettingModel>().value
    }

    override fun initObserve() {

    }
}