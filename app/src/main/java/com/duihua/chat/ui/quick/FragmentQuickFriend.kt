package com.duihua.chat.ui.quick

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.chad.library.adapter4.BaseQuickAdapter
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.bean.SearchAccount
import com.duihua.chat.databinding.FragmentQuickFriendBinding
import com.duihua.chat.ui.mine.PersonalActivity
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class FragmentQuickFriend : BaseFragment<FragmentQuickFriendBinding, QuickModel>() {

    companion object {
        const val QUICK_TYPE_FRIEND = "quick_friend"//MUTUAL
        const val QUICK_TYPE_ATTENTION = "quick_attention"//FOLLOW
        const val QUICK_TYPE_FANS = "quick_fans"//FANS
        const val QUICK_TYPE_BLACK = "quick_black"//BLOCK
        const val QUICK_PARAM_TYPE = "type"
        fun newInstance(type: String) : FragmentQuickFriend {
            return FragmentQuickFriend().apply {
                arguments = Bundle().apply {
                    putString(QUICK_PARAM_TYPE,type)
                }
            }
        }
    }

    var type = ""
    lateinit var accountAdapter: QuickFriendAdapter

    override fun initBinding(): FragmentQuickFriendBinding {
        return FragmentQuickFriendBinding.inflate(layoutInflater)
    }

    override fun initView() {
        type = arguments?.getString(QUICK_PARAM_TYPE,QUICK_TYPE_FRIEND) ?: QUICK_TYPE_FRIEND

        accountAdapter = QuickFriendAdapter()
        binding.rvList.adapter = accountAdapter
        accountAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<SearchAccount> {
            override fun onClick(
                adapter: BaseQuickAdapter<SearchAccount, *>,
                view: View,
                position: Int
            ) {
                val item = accountAdapter.getItem(position)?:return
                PersonalActivity.launch(requireActivity(),item.id.toString())
            }
        })
        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                model?.queryList(type,true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                model?.queryList(type,false)
            }
        })
    }

    override fun initViewModel(): QuickModel? {
        return viewModels<QuickModel>().value
    }

    override fun initObserve() {
        model?.accountListEvent?.observe(this) { list ->
            accountAdapter.submitList(list)
            binding.rvList.post {
                if (list.isEmpty()) {
                    binding.stateLayout.showEmpty()
                } else {
                    binding.stateLayout.showContent()
                }
            }
        }
        model?.requestCancelEvent?.observe(this) {
            if (it.first) {
                binding.refreshLayout.finishRefresh()
            }
            if (it.second) {
                binding.refreshLayout.finishLoadMore()
            }
        }
    }

    override fun lazyLoad() {
        super.lazyLoad()
        model?.queryList(type,true)
    }
}