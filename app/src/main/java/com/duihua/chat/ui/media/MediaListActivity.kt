package com.duihua.chat.ui.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ToastUtils
import com.duihua.chat.R
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.ui.discover.NewDiscoverVideoFragment
import com.gyf.immersionbar.ImmersionBar

/**
 * 媒体列表Activity，用于显示视频或图片列表
 */
class MediaListActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MEDIA_LIST = "extra_media_list"
        private const val EXTRA_INITIAL_POSITION = "extra_initial_position"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_SHOW_FAVORITES = "extra_show_favorites"

        /**
         * 启动Activity的便捷方法
         * @param context 上下文
         * @param mediaList 媒体列表
         * @param initialPosition 初始播放位置
         * @param title 标题，可选
         */
        fun launch(
            context: Context,
            mediaList: ArrayList<ExploreContent>,
            initialPosition: Int = 0,
            title: String? = null
        ) {
            val intent = Intent(context, MediaListActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_MEDIA_LIST, mediaList)
                putExtra(EXTRA_INITIAL_POSITION, initialPosition)
                title?.let { putExtra(EXTRA_TITLE, it) }
                putExtra(EXTRA_SHOW_FAVORITES, false)
            }
            context.startActivity(intent)
        }
        
        /**
         * 启动收藏列表
         * @param context 上下文
         */
        fun launchFavorites(context: Context) {
            val intent = Intent(context, MediaListActivity::class.java).apply {
                putExtra(EXTRA_SHOW_FAVORITES, true)
                putExtra(EXTRA_TITLE, "我的收藏")
            }
            context.startActivity(intent)
        }
    }

    lateinit var toolbar: Toolbar
    
    // 是否显示收藏列表
    private var isShowingFavorites = false
    
    // 当前是否为外部传入的数据
    private var hasExternalData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_list)
        // 读取是否显示收藏列表
        isShowingFavorites = intent.getBooleanExtra(EXTRA_SHOW_FAVORITES, false)
        
        // 设置标题和返回按钮
        setupToolbar()
        // 设置沉浸式状态栏
        setupStatusBar()
        
        // 加载Fragment
        if (savedInstanceState == null) {
            loadVideoFragment()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 仅当不是收藏列表时才显示收藏入口
        if (!isShowingFavorites && hasExternalData) {
            menuInflater.inflate(R.menu.menu_media_list, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                // 打开收藏列表
                launchFavorites(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupStatusBar() {
        BarUtils.transparentStatusBar(this)
        BarUtils.isStatusBarLightMode(this)
        BarUtils.addMarginTopEqualStatusBarHeight(toolbar)
    }
    
    private fun setupToolbar() {
        toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // 设置自定义标题
        val title = intent.getStringExtra(EXTRA_TITLE) ?: (if (isShowingFavorites) "我的收藏" else "媒体")
        toolbar.title = title
        
        // 设置返回按钮点击事件
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun loadVideoFragment() {
        if (isShowingFavorites) {
            // 加载收藏列表
            loadFavoritesFragment()
        } else {
            // 加载常规媒体列表
            loadRegularMediaFragment()
        }
    }
    
    private fun loadRegularMediaFragment() {
        // 获取传入的媒体列表（使用兼容不同Android版本的方式）
        val mediaList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(EXTRA_MEDIA_LIST, ExploreContent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<ExploreContent>(EXTRA_MEDIA_LIST)
        }
        val initialPosition = intent.getIntExtra(EXTRA_INITIAL_POSITION, 0)
        
        // 标记是否有外部数据
        hasExternalData = mediaList != null && mediaList.isNotEmpty()
        
        // 创建Fragment实例
        val fragment = if (hasExternalData) {
            // 使用外部传入的数据
            NewDiscoverVideoFragment.newInstance(
                dataList = mediaList!!,
                initialPosition = initialPosition,
                enableRefresh = false
            )
        } else {
            // 使用默认模式
            NewDiscoverVideoFragment.newInstance("推荐")
        }
        
        // 添加Fragment到容器
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
            
        // 通知选项菜单更新
        invalidateOptionsMenu()
    }
    
    private fun loadFavoritesFragment() {
        // 使用收藏列表模式的Fragment
        val fragment = NewDiscoverVideoFragment.newInstance(
            isFavoritesList = true
        )
        
        // 添加Fragment到容器
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
} 