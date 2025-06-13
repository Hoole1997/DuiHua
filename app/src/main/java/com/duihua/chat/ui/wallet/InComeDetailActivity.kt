package com.duihua.chat.ui.wallet

import android.content.Context
import android.view.ViewGroup
import androidx.activity.viewModels
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.bean.InComeDetail
import com.duihua.chat.bean.Operate
import com.duihua.chat.bean.OperateStatus
import com.duihua.chat.databinding.ActivityIncomeDetailBinding
import com.duihua.chat.databinding.ItemIncomeDetailBinding
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class InComeDetailActivity : BaseActivity<ActivityIncomeDetailBinding, WalletModel>() {



    override fun initBinding(): ActivityIncomeDetailBinding {
        return ActivityIncomeDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"收支明细")

        incomeDetailAdapter.isStateViewEnable = true
        incomeDetailAdapter.setStateViewLayout(this,R.layout.layout_empty)
        binding.rvList.adapter = incomeDetailAdapter

        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                model?.requestWithdrawList(true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                model?.requestWithdrawList(false)
            }
        })
    }

    override fun initViewModel(): WalletModel? {
        return viewModels<WalletModel>().value
    }

    override fun initObserve() {
        model?.withdrawLiveData?.observe(this) {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            if (it.second) {
                incomeDetailAdapter.submitList(it.first)
            } else {
                incomeDetailAdapter.addAll(it.first)
            }
        }
        model?.requestWithdrawList(true)
    }

    private val incomeDetailAdapter = object : BaseQuickAdapter<InComeDetail, DataBindingHolder<ItemIncomeDetailBinding>>() {
        override fun onBindViewHolder(
            holder: DataBindingHolder<ItemIncomeDetailBinding>,
            position: Int,
            item: InComeDetail?
        ) {
            item?.let {
                holder.binding.apply {
                    if (it.operate() == Operate.WITHDRAW) {
                        ivType.setImageResource(R.mipmap.ic_come_detail_withdraw)
                        val operateStatus = when(it.operateStatus()) {
                            OperateStatus.SUBMIT -> "提现"
                            OperateStatus.REJECT -> "拒绝"
                            OperateStatus.FINISH -> "完成"
                            else -> ""
                        }
                        tvType.text = "提现($operateStatus)"
                    } else {
                        ivType.setImageResource(R.mipmap.ic_come_detail_income)
                        tvType.text = "收入"
                    }
                }
            }
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int
        ): DataBindingHolder<ItemIncomeDetailBinding> {
            return DataBindingHolder(R.layout.item_income_detail,parent)
        }

    }

}