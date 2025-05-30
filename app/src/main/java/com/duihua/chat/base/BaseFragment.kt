package com.duihua.chat.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<DB : ViewBinding, VM : ViewModel> : Fragment() {
    lateinit var binding: DB
    var model: VM? = null

    // 是否已经加载过数据
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initBinding()
        model = initViewModel()
        initView()
        initObserve()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // 当Fragment显示时加载数据
        if (!isDataLoaded && isVisible) {
            lazyLoad()
            isDataLoaded = true
        }
    }

    abstract fun initBinding(): DB
    abstract fun initView()
    abstract fun initViewModel(): VM?
    abstract fun initObserve()

    //使用viewpager配合BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT使用
    open fun lazyLoad() {

    }

}