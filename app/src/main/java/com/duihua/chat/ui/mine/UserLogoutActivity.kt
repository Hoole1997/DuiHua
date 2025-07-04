package com.duihua.chat.ui.mine

import android.os.CountDownTimer
import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityUserLogoutBinding
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.login.ActivityLogin
import java.util.concurrent.atomic.AtomicBoolean

class UserLogoutActivity : BaseActivity<ActivityUserLogoutBinding, UserAccountModel>() {

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

    override fun initBinding(): ActivityUserLogoutBinding {
        return ActivityUserLogoutBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar, "注销账号")
        
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
        
        binding.btnLogout.setOnClickListener {
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
            model?.logoutAccount(phone, code)
        }
    }

    override fun initViewModel(): UserAccountModel {
        return viewModels<UserAccountModel>().value
    }

    override fun initObserve() {
        model?.logoutEvent?.observe(this) {
            ToastUtils.showShort("账号已注销")
            binding.btnLogout.postDelayed({
                UserManager.clearUser()
                ActivityUtils.startActivity(ActivityLogin::class.java)
                ActivityUtils.finishOtherActivities(ActivityLogin::class.java)
            }, 1500)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeDownTimer.cancel()
    }
} 