package com.duihua.chat.ui.main

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityMainBinding
import com.duihua.chat.ui.discover.DiscoverFragment
import com.duihua.chat.ui.message.MessageFragment
import com.duihua.chat.ui.mine.FragmentMine
import com.duihua.chat.ui.quick.QuickFragment

class MainActivity : BaseActivity<ActivityMainBinding, MainModel>() {

    companion object {
        var curMainPage = 0
    }

    override fun initBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        BarUtils.transparentStatusBar(this)
        BarUtils.setStatusBarLightMode(this, true)
        initNavigation()
    }

    override fun initViewModel(): MainModel {
        return viewModels<MainModel>().value
    }

    override fun initObserve() {

    }

    private fun initNavigation() {

        val fragmentList = arrayListOf(
            MessageFragment(),
            DiscoverFragment(),
            QuickFragment(),
            FragmentMine()
        )
        binding.pageContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageSelected(position: Int) {
                curMainPage = position
            }
        })
        binding.pageContainer.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getCount(): Int {
                return fragmentList.size
            }
        }
        binding.pageContainer.offscreenPageLimit = 4

        binding.navigation.material().apply {
            addItem(R.drawable.icon_navigation_chat, "对话")
            addItem(R.drawable.icon_navigation_discover, "发现")
            addItem(R.drawable.icon_navigation_quick, "快捷")
            addItem(R.drawable.icon_navigation_mine, "我的")
        }.build().setupWithViewPager(binding.pageContainer)
    }

}