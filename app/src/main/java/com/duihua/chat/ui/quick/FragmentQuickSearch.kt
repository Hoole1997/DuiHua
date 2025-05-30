package com.duihua.chat.ui.quick

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.VibrateUtils
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.databinding.FragmentQuickSearchBinding
import com.duihua.chat.ui.mine.PersonalActivity

class FragmentQuickSearch : BaseFragment<FragmentQuickSearchBinding, QuickModel>() {

    private var inputText = StringBuilder()
    private val MAX_LENGTH = 11

    override fun initBinding(): FragmentQuickSearchBinding {
        return FragmentQuickSearchBinding.inflate(layoutInflater)
    }

    override fun initView() {
        initKeyBoard()
    }

    override fun initViewModel(): QuickModel? {
        return viewModels<QuickModel>().value
    }

    override fun initObserve() {
        model?.searchPhoneEvent?.observe(this) {
            activity?.let { act ->
                if (it == null) {
                    ToastUtils.showShort("未找到此用户")
                } else {
                    clearInput()
                    PersonalActivity.launch(act,it.id.toString())
                }
            }
        }
    }

    private fun initKeyBoard() {
        val numberButtons = arrayOf(
            binding.btn0,binding.btn1,binding.btn2,binding.btn3,
            binding.btn4,binding.btn5,binding.btn6,binding.btn7,
            binding.btn8,binding.btn9
        )

        // 为每个数字按钮设置点击事件
        numberButtons.forEach { button ->
            button.setOnClickListener {
                appendNumber(button.text.toString())
            }
        }
        // 删除按钮点击事件
        binding.btnDelete.setOnClickListener {
            deleteLastNumber()
        }
        binding.btnSearch.setOnClickListener {
            val phone = binding.tvPhone.text.trim()
            if (phone.isNullOrBlank()) {
                ToastUtils.showShort("请输入手机号")
                return@setOnClickListener
            }
            if (phone.length != 11) {
                ToastUtils.showShort("请输入11位手机号")
                return@setOnClickListener
            }
            model?.searchPhone(phone.toString())
        }
    }

    /**
     * 追加数字
     * @param number 要追加的数字
     */
    private fun appendNumber(number: String) {
        if (inputText.length < MAX_LENGTH) {
            inputText.append(number)
            updateDisplay()
        } else {
            // 可以在这里添加提示，比如震动或者 Toast
            showMaxLengthTip()
        }
    }

    /**
     * 删除最后一位数字
     */
    private fun deleteLastNumber() {
        if (inputText.isNotEmpty()) {
            inputText.deleteCharAt(inputText.length - 1)
            updateDisplay()
        }
    }

    /**
     * 更新显示
     */
    private fun updateDisplay() {
        binding.tvPhone.text = inputText.toString()
    }

    /**
     * 显示最大长度提示
     */
    private fun showMaxLengthTip() {
        ToastUtils.showShort("电话号码最多${MAX_LENGTH}位")
        // 可选：添加震动反馈
        vibrate()
    }

    /**
     * 震动反馈
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        VibrateUtils.vibrate(longArrayOf(1000), VibrationEffect.DEFAULT_AMPLITUDE)
    }

    /**
     * 清空输入
     */
    fun clearInput() {
        inputText.clear()
        updateDisplay()
    }

}