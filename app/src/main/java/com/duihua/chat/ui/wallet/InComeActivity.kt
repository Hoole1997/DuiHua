package com.duihua.chat.ui.wallet

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityIncomeBinding
import com.duihua.chat.ui.mine.MineModel

class InComeActivity : BaseActivity<ActivityIncomeBinding, MineModel>() {

    override fun initBinding(): ActivityIncomeBinding {
        return ActivityIncomeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"我的创作收入")

        binding.itemDetails.setOnClickListener {
            ActivityUtils.startActivity(InComeDetailActivity::class.java)
        }
        binding.llWithdraw.setOnClickListener {
            ActivityUtils.startActivity(WithdrawActivity::class.java)
        }
    }

    override fun initViewModel(): MineModel? {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {
        model?.userInfoEvent?.observe(this) {
            binding.tvMoney.text = it.currency.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        model?.refreshUserInfo()
    }
}