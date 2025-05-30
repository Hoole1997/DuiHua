package com.duihua.chat.ui.wallet

import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityRechargeBinding

class RechargeActivity : BaseActivity<ActivityRechargeBinding, WalletModel>(){
    override fun initBinding(): ActivityRechargeBinding {
        return ActivityRechargeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"充值")
    }

    override fun initViewModel(): WalletModel? {
        return viewModels<WalletModel>().value
    }

    override fun initObserve() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recharge,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_details) {

        }
        return super.onOptionsItemSelected(item)
    }
}