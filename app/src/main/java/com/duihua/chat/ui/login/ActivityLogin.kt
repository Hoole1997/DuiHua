package com.duihua.chat.ui.login

import android.os.CountDownTimer
import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityLoginBinding
import com.duihua.chat.ui.main.MainActivity
import java.util.concurrent.atomic.AtomicBoolean

class ActivityLogin : BaseActivity<ActivityLoginBinding, LoginModel>() {

    val downTimer = AtomicBoolean(false)

    val timeDownTimer = object : CountDownTimer(60*1000,1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.btnSend.text = (millisUntilFinished/1000).toInt().toString()
        }

        override fun onFinish() {
            binding.btnSend.text = "发送"
            downTimer.set(false)
        }
    }

    override fun initBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.btnSend.setOnClickListener {
            val phone = binding.etPhone.text.trim()
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
            model?.sendCode(phone.toString())
        }
        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.trim()
            val code = binding.etCode.text.trim()
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
            if (!binding.cbPrivacy.isChecked) {
                ToastUtils.showShort("请先同意隐私协议")
                return@setOnClickListener
            }
            model?.login(phone.toString(),code.toString())
        }
    }

    override fun initViewModel(): LoginModel? {
        return viewModels<LoginModel>().value
    }

    override fun initObserve() {
        model?.loginEvent?.observe(this) {
            ToastUtils.showShort("登录成功")
            binding.btnLogin.postDelayed({
                ActivityUtils.startActivity(MainActivity::class.java)
                ActivityUtils.finishOtherActivities(MainActivity::class.java)
            },1500)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeDownTimer.cancel()
    }
}