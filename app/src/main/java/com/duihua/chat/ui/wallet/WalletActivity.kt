package com.duihua.chat.ui.wallet

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityWalletBinding
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.mine.MineModel

class WalletActivity : BaseActivity<ActivityWalletBinding, WalletModel>() {

    lateinit var mineModel: MineModel

    override fun initBinding(): ActivityWalletBinding {
        return ActivityWalletBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar, "资产")

        binding.tvZfb.setOnClickListener {
            if (mineModel.userInfoEvent.value?.aliPayAccount.isNullOrEmpty()) {
                ActivityUtils.startActivity(BindAliPayActivity::class.java)
            }
        }
        binding.tvMediaIncome.setOnClickListener {
            ActivityUtils.startActivity(InComeActivity::class.java)
        }
    }

    override fun initViewModel(): WalletModel? {
        mineModel = viewModels<MineModel>().value
        return viewModels<WalletModel>().value
    }

    override fun initObserve() {
        mineModel.userInfoEvent.observe(this) {
            UserManager.updateUserInfo(it)
            binding.tvMoney.text = it.currency.toString()
            binding.tvMediaIncome.text = it.currency.toString()
            binding.tvZfb.text = if (it.aliPayAccount.isNullOrEmpty()) {
                "未绑定"
            }else {
                "${it.aliPayAccount}(${it.aliPayName})"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mineModel.refreshUserInfo()
    }
}