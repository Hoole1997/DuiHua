package com.duihua.chat.ui.setting

import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivitySettingBinding
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.login.ActivityLogin
import com.duihua.chat.ui.mine.UserManagerActivity
import com.duihua.chat.util.test.setupWithStringList
import android.view.View
import android.widget.AdapterView

class SettingActivity : BaseActivity<ActivitySettingBinding, SettingModel>() {
    
    // 定义隐私选项对应的值
    private val privacyOptions = arrayListOf("公开", "互关的朋友", "私密")
    private val privacyValues = arrayListOf("PUBLIC", "FRIEND", "PRIVATE")
    
    // 记录当前选中的位置
    private var currentFansNumPosition = -1
    private var currentFansListPosition = -1
    
    // 是否已初始化监听器
    private var listenersInitialized = false
    
    override fun initBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"设置")

        binding.itemAccountManager.setOnClickListener {
            //用户管理
            ActivityUtils.startActivity(UserManagerActivity::class.java)
        }
        
        // 首先初始化UI，不添加监听器
        initUIWithoutListeners()
        
        // 使用post延迟设置监听器，确保UI初始化完成
        binding.root.post {
            setupListeners()
        }
        
        binding.itemClearCache.setOnClickListener {
            ToastUtils.showShort("清理成功")
        }
        
        binding.itemLogout.setOnClickListener {
            UserManager.clearUser()
            ActivityUtils.startActivity(ActivityLogin::class.java)
            ActivityUtils.finishOtherActivities(ActivityLogin::class.java)
        }
    }
    
    /**
     * 初始化UI但不添加监听器
     */
    private fun initUIWithoutListeners() {
        val userInfo = UserManager.userInfo() ?: return
        
        // 设置Spinner的数据，使用空监听器
        binding.spinnerFansNum.setupWithStringList(privacyOptions) { _, _ -> }
        binding.spinnerFansList.setupWithStringList(privacyOptions) { _, _ -> }
        
        // 设置手机号搜索开关状态
        binding.switchSearchPhone.isChecked = userInfo.canSearchByPhone
        
        // 设置粉丝数据可见性下拉菜单选中项并记录位置
        val fansNumberIndex = privacyValues.indexOf(userInfo.canSeeFansNumber)
        if (fansNumberIndex >= 0) {
            binding.spinnerFansNum.setSelection(fansNumberIndex)
            currentFansNumPosition = fansNumberIndex
        }
        
        // 设置粉丝列表可见性下拉菜单选中项并记录位置
        val fansListIndex = privacyValues.indexOf(userInfo.canSeeFansList)
        if (fansListIndex >= 0) {
            binding.spinnerFansList.setSelection(fansListIndex)
            currentFansListPosition = fansListIndex
        }
    }
    
    /**
     * 设置监听器
     */
    private fun setupListeners() {
        if (listenersInitialized) return
        
        // 设置手机号搜索开关的点击事件
        binding.switchSearchPhone.setOnCheckedChangeListener { _, isChecked ->
            model?.updateSearchByPhone(isChecked)
        }
        
        // 为Spinner设置专用监听器
        setupFansNumSpinner()
        setupFansListSpinner()
        
        listenersInitialized = true
    }
    
    /**
     * 设置粉丝数据可见性Spinner监听器
     */
    private fun setupFansNumSpinner() {
        // 移除现有适配器
        val adapter = binding.spinnerFansNum.adapter
        binding.spinnerFansNum.adapter = null
        
        // 重新设置适配器和监听器
        binding.spinnerFansNum.setupWithStringList(privacyOptions) { position: Int, _: String -> 
            // 检查位置是否变化
            if (position != currentFansNumPosition) {
                ToastUtils.showShort("更新粉丝数据可见性: ${privacyOptions[position]}")
                model?.updateFansNumberVisibility(privacyValues[position])
                // 更新当前位置
                currentFansNumPosition = position
            }
        }
        
        // 恢复选中状态
        if (currentFansNumPosition >= 0) {
            binding.spinnerFansNum.setSelection(currentFansNumPosition)
        }
    }
    
    /**
     * 设置粉丝列表可见性Spinner监听器
     */
    private fun setupFansListSpinner() {
        // 移除现有适配器
        val adapter = binding.spinnerFansList.adapter
        binding.spinnerFansList.adapter = null
        
        // 重新设置适配器和监听器
        binding.spinnerFansList.setupWithStringList(privacyOptions) { position: Int, _: String ->
            // 检查位置是否变化
            if (position != currentFansListPosition) {
                ToastUtils.showShort("更新粉丝列表可见性: ${privacyOptions[position]}")
                model?.updateFansListVisibility(privacyValues[position])
                // 更新当前位置
                currentFansListPosition = position
            }
        }
        
        // 恢复选中状态
        if (currentFansListPosition >= 0) {
            binding.spinnerFansList.setSelection(currentFansListPosition)
        }
    }

    override fun initViewModel(): SettingModel {
        return viewModels<SettingModel>().value
    }

    override fun initObserve() {
        // 观察用户信息更新事件
        model?.userInfoUpdateEvent?.observe(this) {
            val userInfo = UserManager.userInfo() ?: return@observe
            
            // 临时移除监听器
            binding.switchSearchPhone.setOnCheckedChangeListener(null)
            
            // 更新UI状态
            binding.switchSearchPhone.isChecked = userInfo.canSearchByPhone
            
            // 更新Spinner状态
            val fansNumberIndex = privacyValues.indexOf(userInfo.canSeeFansNumber)
            if (fansNumberIndex >= 0 && fansNumberIndex != currentFansNumPosition) {
                currentFansNumPosition = fansNumberIndex
                binding.spinnerFansNum.setSelection(fansNumberIndex)
            }
            
            val fansListIndex = privacyValues.indexOf(userInfo.canSeeFansList)
            if (fansListIndex >= 0 && fansListIndex != currentFansListPosition) {
                currentFansListPosition = fansListIndex
                binding.spinnerFansList.setSelection(fansListIndex)
            }
            
            // 恢复监听器
            binding.switchSearchPhone.setOnCheckedChangeListener { _, isChecked ->
                model?.updateSearchByPhone(isChecked)
            }
            
            ToastUtils.showShort("设置已更新")
        }
    }
}