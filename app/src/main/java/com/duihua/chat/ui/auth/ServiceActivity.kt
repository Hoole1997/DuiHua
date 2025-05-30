package com.duihua.chat.ui.auth

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.databinding.ActivityServiceBinding
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.wallet.RechargeActivity
import com.duihua.chat.ui.wallet.WalletActivity

class ServiceActivity : BaseActivity<ActivityServiceBinding, AuthModel>() {

    override fun initBinding(): ActivityServiceBinding {
        return ActivityServiceBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"服务")

        binding.itemBeauty.setOnClickListener {
            ActivityUtils.startActivity(BeautyActivity::class.java)
        }
        binding.itemRealName.setOnClickListener {
            ActivityUtils.startActivity(RealNameActivity::class.java)
        }
        binding.llWallet.setOnClickListener {
            ActivityUtils.startActivity(WalletActivity::class.java)
        }
        binding.llRecharge.setOnClickListener {
            ActivityUtils.startActivity(RechargeActivity::class.java)
        }
    }

    override fun initViewModel(): AuthModel? {
        return viewModels<AuthModel>().value
    }

    override fun initObserve() {
        model?.userInfoEvent?.observe(this) {
            UserManager.updateUserInfo(it)
            setUserView(it)
        }
    }

    private fun setUserView(userInfo: UserInfo) {
        binding.tvMoney.text = userInfo.currency.toString()
        binding.tvDiamonds.text = userInfo.diamond.toString()

        binding.tvAuthState.text = if (userInfo.identityNumber == null) "未认证" else "已认证"
        binding.tvBeauty.text = if (userInfo.beautyData == null) "未开通" else userInfo.beautyData

        if (userInfo.identityNumber != null) {
            binding.itemRealName.isEnabled = false
        }
        if (userInfo.beautyData != null) {
            binding.itemBeauty.isClickable = false
        }
    }

    override fun onResume() {
        super.onResume()
        model?.refreshUserInfo()
    }

}