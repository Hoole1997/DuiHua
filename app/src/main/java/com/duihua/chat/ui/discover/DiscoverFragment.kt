package com.duihua.chat.ui.discover

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.databinding.FragmentDiscoverBinding
import com.gyf.immersionbar.ImmersionBar

class DiscoverFragment : BaseFragment<FragmentDiscoverBinding, DiscoverModel>() {
    override fun initBinding(): FragmentDiscoverBinding {
        return FragmentDiscoverBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun initViewModel(): DiscoverModel? {
        return activityViewModels<DiscoverModel>().value
    }

    override fun initObserve() {
        model?.tabLiveData?.observe(this,::setTable)

        model?.getLabel()
    }

    private fun setTable(titleList: List<String>) {
        val fragmentList = arrayListOf<Fragment>()
        titleList.forEach { title ->
            binding.tabTitle.apply {
                addTab(newTab().setText(title))
                fragmentList.add(DiscoverVideoFragment.newInstance(title))
            }
        }
        binding.vpPage.adapter = object : FragmentStatePagerAdapter(
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
                return titleList[position]
            }
        }
        binding.vpPage.offscreenPageLimit = fragmentList.size
        binding.tabTitle.setupWithViewPager(binding.vpPage)
    }

    override fun onResume() {
        super.onResume()
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .titleBar(binding.tabTitle)
            .init()
    }

}