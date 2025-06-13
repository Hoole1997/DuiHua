package com.duihua.chat.ui.wallet

import android.content.Context
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.bean.RechargeDetail
import com.duihua.chat.databinding.ActivityRechargeDetailBinding
import com.duihua.chat.databinding.ItemRechargeDetailBinding
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class RechargeDetailActivity : BaseActivity<ActivityRechargeDetailBinding, WalletModel>() {

    override fun initBinding(): ActivityRechargeDetailBinding {
        return ActivityRechargeDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"钻石明细")

        binding.rvList.adapter = detailAdapter
        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                model?.rechargeDetailList(true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                model?.rechargeDetailList(false)
            }
        })
    }

    override fun initViewModel(): WalletModel? {
        return viewModels<WalletModel>().value
    }

    override fun initObserve() {
        model?.rechargeDetailItemListLiveData?.observe(this) {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            if (it.second) {
                detailAdapter.submitList(it.first)
            } else {
                detailAdapter.addAll(it.first)
            }
        }
        model?.rechargeDetailList(true)
    }

    private val detailAdapter = object : BaseQuickAdapter<RechargeDetail, DataBindingHolder<ItemRechargeDetailBinding>>() {
        override fun onBindViewHolder(
            holder: DataBindingHolder<ItemRechargeDetailBinding>,
            position: Int,
            item: RechargeDetail?
        ) {
            item?.let {
                holder.binding.apply {
                    tvState.text = if (it.changeOrderID == null) "支出" else "收入"
                    tvOrder.isVisible = it.changeOrderID!=null
                    if (it.changeOrderID == null) {
                        tvOrder.text = ""
                    } else {
                        tvOrder.text = it.changeOrderID
                    }
                    tvTime.text = it.createDate
                    tvDiamond.text = it.totalDiamond.toString()
                }
            }
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int
        ): DataBindingHolder<ItemRechargeDetailBinding> {
            return DataBindingHolder(R.layout.item_recharge_detail,parent)
        }

    }

}