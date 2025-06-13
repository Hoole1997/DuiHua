package com.duihua.chat.ui.wallet

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.bean.RechargeItem
import com.duihua.chat.databinding.ActivityRechargeBinding
import com.duihua.chat.databinding.ItemRechargeItemBinding

class RechargeActivity : BaseActivity<ActivityRechargeBinding, WalletModel>(){
    override fun initBinding(): ActivityRechargeBinding {
        return ActivityRechargeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"充值")
        binding.rvRecharge.adapter = rechargeAdapter

        binding
    }

    override fun initViewModel(): WalletModel? {
        return viewModels<WalletModel>().value
    }

    override fun initObserve() {
        model?.rechargeItemListLiveData?.observe(this) {
            rechargeAdapter.submitList(it)
        }
        model?.rechargeItems()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recharge,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_details) {
            ActivityUtils.startActivity(RechargeDetailActivity::class.java)
        }
        return super.onOptionsItemSelected(item)
    }

    private val rechargeAdapter = object : BaseQuickAdapter<RechargeItem, DataBindingHolder<ItemRechargeItemBinding>>() {
        override fun onBindViewHolder(
            holder: DataBindingHolder<ItemRechargeItemBinding>,
            position: Int,
            item: RechargeItem?
        ) {
            item?.let {
                holder.binding.apply {
                    tvRechargeName.text = it.name
                    tvRechargePrice.text = it.price.toString()+"元"
                }
            }
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int
        ): DataBindingHolder<ItemRechargeItemBinding> {
            return DataBindingHolder(R.layout.item_recharge_item,parent)
        }

    }

}