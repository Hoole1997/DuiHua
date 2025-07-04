package com.duihua.chat.ui.mine

import android.os.CountDownTimer
import androidx.activity.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityChangePhoneBinding
import com.duihua.chat.net.UserManager
import java.util.concurrent.atomic.AtomicBoolean

class ChangePhoneActivity : BaseActivity<ActivityChangePhoneBinding, UserAccountModel>() {

    private val downTimer = AtomicBoolean(false)

    private val timeDownTimer = object : CountDownTimer(60*1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.btnSend.text = (millisUntilFinished/1000).toInt().toString()
        }

        override fun onFinish() {
            binding.btnSend.text = "发送"
            downTimer.set(false)
        }
    }

    override fun initBinding(): ActivityChangePhoneBinding {
        return ActivityChangePhoneBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar, "更改手机号")
        
        // 显示当前绑定的手机号
        val userInfo = UserManager.userInfo()
        if (userInfo != null && userInfo.phone.isNotEmpty()) {
            binding.tvCurrentPhone.text = "已绑定的手机号：${userInfo.phone}"
        }
        
        binding.btnSend.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.isEmpty()) {
                ToastUtils.showShort("电话不能为空")
                return@setOnClickListener
            }
            if (phone.length != 11) {
                ToastUtils.showShort("请输入正确的号码")
                return@setOnClickListener
            }
            if (downTimer.get()) {
                return@setOnClickListener
            }
            downTimer.set(true)
            timeDownTimer.start()
            model?.sendCode(phone)
        }
        
        binding.btnChangePhone.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val code = binding.etCode.text.toString().trim()
            if (phone.isEmpty()) {
                ToastUtils.showShort("电话不能为空")
                return@setOnClickListener
            }
            if (phone.length != 11) {
                ToastUtils.showShort("请输入正确的号码")
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                ToastUtils.showShort("验证码不能为空")
                return@setOnClickListener
            }
            model?.changePhone(phone, code)
        }
    }

    override fun initViewModel(): UserAccountModel {
        return viewModels<UserAccountModel>().value
    }

    override fun initObserve() {
        model?.changePhoneEvent?.observe(this) {
            ToastUtils.showShort("手机号变更成功")
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeDownTimer.cancel()
    }
} 