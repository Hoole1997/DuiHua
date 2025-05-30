package com.duihua.chat.ui.wallet

import androidx.activity.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityBindAliPayBinding
import com.duihua.chat.ui.mine.MineModel
import org.json.JSONObject

class BindAliPayActivity : BaseActivity<ActivityBindAliPayBinding, MineModel>() {

    override fun initBinding(): ActivityBindAliPayBinding {
        return ActivityBindAliPayBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"绑定支付宝")

        binding.btnConfirm.setOnClickListener {
            val account = binding.etAccount.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            if (account.isBlank() ||  name.isBlank()) {
                ToastUtils.showShort("请填写完整信息")
                return@setOnClickListener
            }
            model?.basicEdit(JSONObject().apply {
                put("aliPayAccount",account)
                put("aliPayName",name)
            }) {
                ToastUtils.showShort("绑定成功")
                binding.btnConfirm.postDelayed({
                    finish()
                },1000)
            }
        }
    }

    override fun initViewModel(): MineModel? {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {

    }
}