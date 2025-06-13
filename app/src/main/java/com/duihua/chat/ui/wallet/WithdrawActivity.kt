package com.duihua.chat.ui.wallet

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityWithdrawBinding
import com.duihua.chat.ui.mine.MineModel
import java.lang.Exception

class WithdrawActivity : BaseActivity<ActivityWithdrawBinding, WalletModel>() {

    lateinit var mineModel: MineModel

    override fun initBinding(): ActivityWithdrawBinding {
        return ActivityWithdrawBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"提现")

        binding.tvWithdrawType.setOnClickListener {
            if (mineModel.userInfoEvent.value?.aliPayAccount != null)return@setOnClickListener
            ActivityUtils.startActivity(BindAliPayActivity::class.java)
        }
        binding.btnConfirm.setOnClickListener {
            if (mineModel.userInfoEvent.value?.aliPayAccount == null)return@setOnClickListener
            val money = try {
                binding.etWithdraw.text.toString().trim().toInt()
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
            model?.withdraw(money) {
                ToastUtils.showShort("申请提现成功")
                binding.btnConfirm.postDelayed({
                    finish()
                },1500)
            }
        }
    }

    override fun initViewModel(): WalletModel? {
        mineModel = viewModels<MineModel>().value
        return viewModels<WalletModel>().value
    }

    override fun initObserve() {
        mineModel.userInfoEvent.observe(this) {
            binding.tvWithdrawType.text = if (it.aliPayAccount == null) "支付宝（未绑定）" else it.aliPayAccount+"(${it.aliPayName})"
            binding.etWithdraw.hint = "可提现收入${it.currency}元"
        }
    }

    override fun onResume() {
        super.onResume()
        mineModel.refreshUserInfo()
    }
}