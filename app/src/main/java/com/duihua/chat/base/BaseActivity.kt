package com.duihua.chat.base

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.duihua.chat.R

abstract class BaseActivity<DB: ViewBinding,VM: ViewModel> : AppCompatActivity() {

    lateinit var binding:DB
    var model:VM? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        model = initViewModel()
        initView()
        initObserve()
    }

    abstract fun initBinding(): DB
    abstract fun initView()
    abstract fun initViewModel(): VM?
    abstract fun initObserve()

    @SuppressLint("RestrictedApi")
    open fun useDefaultToolbar(toolbar: Toolbar, title: String) {
        BarUtils.setStatusBarLightMode(this,true)
        BarUtils.addMarginTopEqualStatusBarHeight(toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        toolbar.setNavigationOnClickListener {
            closePage()
        }
    }

    open fun closePage() {
        finish()
    }
}