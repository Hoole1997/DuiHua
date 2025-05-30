package com.duihua.chat.ui.mine

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityPersonalBinding
import com.duihua.chat.databinding.DialogPersonalSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PersonalActivity : BaseActivity<ActivityPersonalBinding, MineModel>() {

    companion object {
        fun launch(context: Context,userId: String) {
            context.startActivity(Intent().apply {
                setClass(context, PersonalActivity::class.java)
                putExtra("userId",userId)
            })
        }
    }

    override fun initBinding(): ActivityPersonalBinding {
        return ActivityPersonalBinding.inflate(layoutInflater)
    }

    override fun initView() {
        intent.getStringExtra("userId")?.let {
            val fragment = FragmentMine.newInstance(true,it)
            supportFragmentManager.beginTransaction()
                .add(R.id.fl_container,fragment)
                .commit()
        }
    }

    override fun initViewModel(): MineModel? {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {

    }

}