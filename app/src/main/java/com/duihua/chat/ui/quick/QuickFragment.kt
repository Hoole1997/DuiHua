package com.duihua.chat.ui.quick

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.databinding.FragmentQuickBinding
import com.gyf.immersionbar.ImmersionBar

class QuickFragment : BaseFragment<FragmentQuickBinding, QuickModel>() {
    override fun initBinding(): FragmentQuickBinding {
        return FragmentQuickBinding.inflate(layoutInflater)
    }

    override fun initView() {
        initTabAndPage()
    }

    override fun initViewModel(): QuickModel? {
        return viewModels<QuickModel>().value
    }

    override fun initObserve() {

    }

    private fun initTabAndPage() {

        binding.tab.setupWithViewPager(binding.vpContainer)
        binding.vpContainer.offscreenPageLimit = 3
        val fragmentList = arrayListOf(
            FragmentQuickSearch(),
            FragmentQuickFriend.newInstance(FragmentQuickFriend.QUICK_TYPE_FRIEND),
            FragmentQuickFriend.newInstance(FragmentQuickFriend.QUICK_TYPE_ATTENTION)
        )
        binding.vpContainer.adapter = object : FragmentStatePagerAdapter(
            childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getCount(): Int {
                return fragmentList.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position == 0) {
                    "查找"
                } else if (position == 1) {
                    "朋友"
                } else {
                    "关注"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .titleBar(binding.llTop)
            .init()
    }

}