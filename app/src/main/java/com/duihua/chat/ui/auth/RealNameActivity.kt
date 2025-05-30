package com.duihua.chat.ui.auth

import androidx.activity.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityRealNameBinding

class RealNameActivity : BaseActivity<ActivityRealNameBinding, AuthModel>() {
    override fun initBinding(): ActivityRealNameBinding {
        return ActivityRealNameBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"实名认证")

        binding.btnConfirm.setOnClickListener {
            val name = binding.etZfb.text.toString()
            val identityNumber = binding.etIdCard.text.toString()
            if (name.isNullOrBlank() || identityNumber.isNullOrBlank()) {
                ToastUtils.showShort("请输入完整的信息")
                return@setOnClickListener
            }
            model?.updateIdentity(name,identityNumber) {
                ToastUtils.showShort("提交成功")
                finish()
            }
        }
    }

    override fun initViewModel(): AuthModel? {
        return viewModels<AuthModel>().value
    }

    override fun initObserve() {

    }
}