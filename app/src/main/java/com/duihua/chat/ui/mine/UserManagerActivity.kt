package com.duihua.chat.ui.mine

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityUserManagerBinding

class UserManagerActivity : BaseActivity<ActivityUserManagerBinding, MineModel>() {
    override fun initBinding(): ActivityUserManagerBinding {
        return ActivityUserManagerBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar, "账号管理")
        
        binding.itemLogout.setOnClickListener {
            ActivityUtils.startActivity(UserLogoutActivity::class.java)
        }
        
        binding.itemChangePhone.setOnClickListener {
            ActivityUtils.startActivity(ChangePhoneActivity::class.java)
        }
    }

    override fun initViewModel(): MineModel {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {
        // 无需观察任何LiveData
    }
} 