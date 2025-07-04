package com.duihua.chat.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.duihua.chat.R
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.databinding.FragmentNewDiscoverVideoBinding
import com.duihua.chat.databinding.ItemNewVideoBinding
import com.duihua.chat.widget.player.VideoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.duihua.chat.widget.player.CacheManager
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

import java.text.SimpleDateFormat
import java.util.Locale

class NewDiscoverVideoFragment : BaseFragment<FragmentNewDiscoverVideoBinding, DiscoverModel>() {

    companion object {
        private const val PARAM_TITLE = "title"
        private const val PARAM_DATA_LIST = "data_list"
        private const val PARAM_INITIAL_POSITION = "initial_position"
        private const val PARAM_ENABLE_REFRESH = "enable_refresh"
        private const val PARAM_IS_FAVORITES_LIST = "is_favorites_list"

        fun newInstance(title: String): NewDiscoverVideoFragment {
            return NewDiscoverVideoFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_TITLE, title)
                }
            }
        }
        
        fun newInstance(
            dataList: ArrayList<ExploreContent>,
            initialPosition: Int = 0,
            enableRefresh: Boolean = false
        ): NewDiscoverVideoFragment {
            return NewDiscoverVideoFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(PARAM_DATA_LIST, dataList)
                    putInt(PARAM_INITIAL_POSITION, initialPosition)
                    putBoolean(PARAM_ENABLE_REFRESH, enableRefresh)
                }
            }
        }
        
        /**
         * 创建收藏列表实例
         */
        fun newInstance(isFavoritesList: Boolean): NewDiscoverVideoFragment {
            return NewDiscoverVideoFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_IS_FAVORITES_LIST, isFavoritesList)
                    putBoolean(PARAM_ENABLE_REFRESH, true)
                }
            }
        }
    }

    // 视频适配器
    private lateinit var videoAdapter: NewVideoAdapter
    
    // 当前播放位置
    private var currentPlayPosition = -1
    
    // 视频播放器
    private lateinit var videoPlayer: VideoPlayer
    
    // 缓存
    private lateinit var cache: SimpleCache
    
    // 日期格式化
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    
    // 外部传入的数据
    private var externalDataList: List<ExploreContent>? = null
    private var initialPosition: Int = 0
    private var enableRefresh: Boolean = true
    
    // 是否为收藏列表模式
    private var isFavoritesList: Boolean = false

    override fun initBinding(): FragmentNewDiscoverVideoBinding {
        return FragmentNewDiscoverVideoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 获取参数
        readArguments()
        
        // 初始化视频缓存
        initVideoCache()
        
        // 初始化播放器
        initVideoPlayer()
        
        // 初始化适配器
        initAdapter()
        
        // 设置ViewPager2
        setupViewPager()
        
        // 设置刷新控件
        setupRefreshLayout()
        
        // 加载数据
        loadData()
    }
    
    private fun readArguments() {
        // 读取标题参数
        model?.title = arguments?.getString(PARAM_TITLE) ?: ""
        
        // 读取是否为收藏列表模式
        isFavoritesList = arguments?.getBoolean(PARAM_IS_FAVORITES_LIST, false) ?: false
        
        // 读取外部传入的数据列表（使用兼容不同Android版本的方式）
        externalDataList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList(PARAM_DATA_LIST, ExploreContent::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelableArrayList(PARAM_DATA_LIST)
        }
        
        // 读取初始播放位置
        initialPosition = arguments?.getInt(PARAM_INITIAL_POSITION, 0) ?: 0
        
        // 读取是否启用刷新
        enableRefresh = arguments?.getBoolean(PARAM_ENABLE_REFRESH, true) ?: true
    }
    
    private fun loadData() {
        if (externalDataList != null) {
            // 如果有外部传入的数据，直接使用
            videoAdapter.updateData(externalDataList!!, true)
            
            // 设置并播放指定位置的视频
            binding.vpContainer.post {
                binding.vpContainer.setCurrentItem(initialPosition, false)
                playVideoAt(initialPosition)
            }
        } else if (isFavoritesList) {
            // 如果是收藏列表模式，加载收藏列表
            model?.favoritesList(true)
        } else {
            // 否则请求常规数据
            model?.exploreList(true)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 确保在页面恢复时播放当前视频
        if (currentPlayPosition >= 0) {
            videoPlayer.play()
        } else if (videoAdapter.itemCount > 0) {
            // 如果还没有播放任何视频但已有数据，播放第一个
            playVideoAt(0)
        }
    }
    
    private fun initVideoCache() {
        // 使用CacheManager单例获取缓存，避免多个实例使用同一缓存目录
        cache = CacheManager.getInstance(requireContext())
    }
    
    private fun initVideoPlayer() {
        videoPlayer = VideoPlayer(requireContext())
        lifecycle.addObserver(videoPlayer)
    }
    
    private fun initAdapter() {
        videoAdapter = NewVideoAdapter()
        binding.vpContainer.adapter = videoAdapter
    }
    
    private fun setupViewPager() {
        with(binding.vpContainer) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
            
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    playVideoAt(position)
                    
                    // 当滑动到倒数第二项时，加载更多数据
                    // 只在启用刷新且使用非外部数据的情况下触发
                    if (enableRefresh && externalDataList == null && 
                        position >= videoAdapter.itemCount - 2 && 
                        !binding.refreshLayout.isLoading) {
                        loadMoreData()
                    }
                }
            })
        }
    }
    
    /**
     * 加载更多数据
     */
    private fun loadMoreData() {
        // 如果使用外部数据，不执行加载更多
        if (externalDataList != null) return
        
        // 显示底部加载提示
        binding.refreshLayout.autoLoadMore()
        
        // 根据模式请求不同的数据
        if (isFavoritesList) {
            model?.favoritesList(false)
        } else {
            model?.exploreList(false)
        }
    }
    
    private fun setupRefreshLayout() {
        // 如果使用外部数据或禁用了刷新，完全禁用刷新功能
        if (!enableRefresh || externalDataList != null) {
            binding.refreshLayout.setEnableRefresh(false)
            binding.refreshLayout.setEnableLoadMore(false)
            return
        }
        
        // 启用下拉刷新和自动加载更多
        binding.refreshLayout.setEnableRefresh(true)
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.setEnableAutoLoadMore(false)
        
        // 设置刷新和加载更多的监听器
        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (isFavoritesList) {
                    model?.favoritesList(false)
                } else {
                    model?.exploreList(false)
                }
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                if (isFavoritesList) {
                    model?.favoritesList(true)
                } else {
                    model?.exploreList(true)
                }
            }
        })
    }

    override fun initViewModel(): DiscoverModel {
        return viewModels<DiscoverModel>().value
    }

    override fun initObserve() {
        // 仅当未提供外部数据时才观察LiveData变化
        if (externalDataList == null) {
            // 监听数据加载结果
            model?.exploreContentLiveData?.observe(this) { (isRefresh, list) ->
                videoAdapter.updateData(list, isRefresh)
                
                // 如果是刷新，滚动到第一项并立即播放
                if (isRefresh && list.isNotEmpty()) {
                    binding.vpContainer.setCurrentItem(0, false)
                    
                    // 使用post确保在视图完全准备好后再播放
                    binding.vpContainer.post {
                        playVideoAt(0)
                    }
                }
                
                // 如果是收藏列表且为空，显示提示
                if (isFavoritesList && list.isEmpty() && isRefresh) {
                    ToastUtils.showShort("暂无收藏内容")
                }
            }
            
            // 监听刷新/加载更多完成
            model?.requestCancelLiveData?.observe(this) { isRefresh ->
                if (isRefresh) {
                    binding.refreshLayout.finishRefresh()
                } else {
                    binding.refreshLayout.finishLoadMore()
                }
            }
        }
        
        // 监听收藏状态变更事件
        model?.favoriteChangedLiveData?.observe(this) { (mediaId, isFavorite) ->
            // 更新列表中的收藏状态
            if (externalDataList != null) {
                // 对于外部数据，直接更新适配器中的项
                videoAdapter.updateItemFavoriteStatus(mediaId, isFavorite)
            } else {
                // 对于从网络加载的数据，通过Model更新
                val updated = model?.updateItemFavoriteStatus(mediaId, isFavorite)
                
                // 如果Model更新失败，尝试通过适配器直接更新
                if (updated != true) {
                    videoAdapter.updateItemFavoriteStatus(mediaId, isFavorite)
                }
                
                // 如果是收藏列表，且取消了收藏，则需要移除该项
                if (isFavoritesList && !isFavorite) {
                    val position = videoAdapter.removeItem(mediaId)
                    if (position != -1 && position <= currentPlayPosition) {
                        // 如果删除的是当前播放项前面的内容，需要调整播放位置
                        currentPlayPosition = (currentPlayPosition - 1).coerceAtLeast(0)
                    }
                    
                    // 如果删除后列表为空，显示提示
                    if (videoAdapter.itemCount == 0) {
                        ToastUtils.showShort("暂无收藏内容")
                    }
                }
            }
        }
    }
    
    /**
     * 播放指定位置的视频
     */
    private fun playVideoAt(position: Int) {
        // 如果列表为空，不处理
        if (videoAdapter.itemCount == 0) return
        
        // 如果是同一个位置，不处理
        if (position == currentPlayPosition) return
        
        // 停止当前播放
        videoPlayer.stop()
        
        // 更新当前播放位置
        currentPlayPosition = position
        
        // 获取当前项
        val itemView = videoAdapter.getViewAtPosition(position) ?: return
        val item = videoAdapter.getItem(position) ?: return
        
        // 只处理视频类型
        if (item.mediaType != "video") return
        
        // 设置播放器到容器中
        val container = itemView.findViewById<ViewGroup>(R.id.fl_container)
        
        // 先移除视频播放器
        if (videoPlayer.parent != null) {
            (videoPlayer.parent as ViewGroup).removeView(videoPlayer)
        }
        
        // 添加到新的容器
        container.addView(videoPlayer, 0)
        
        // 设置视频源
        val mediaItem = MediaItem.fromUri(item.resourceUrls[0])
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(requireContext()))
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
            
        // 播放视频
        videoPlayer.playVideo(mediaSource)
        
        // 获取视图组件
        val ivCover = itemView.findViewById<ImageView>(R.id.iv_cover)
        val ivPlay = itemView.findViewById<ImageView>(R.id.iv_play)
        val likeView = itemView.findViewById<com.duihua.chat.widget.LikeView>(R.id.like_view)
        
        // 监听播放状态
        videoPlayer.getplayer().addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    // 视频准备好了，隐藏封面
                    ivCover.visibility = View.GONE
                    ivPlay.visibility = View.GONE
                }
            }
        })
        
        // 设置点击事件
        likeView.setOnPlayPauseListener(object : com.duihua.chat.widget.LikeView.OnPlayPauseListener {
            override fun onPlayOrPause() {
                if (videoPlayer.isPlaying()) {
                    videoPlayer.pause()
                    ivPlay.visibility = View.VISIBLE
                } else {
                    videoPlayer.play()
                    ivPlay.visibility = View.GONE
                }
            }
        })
    }
    
    override fun onPause() {
        super.onPause()
        videoPlayer.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.release()
    }
    
    /**
     * 视频适配器
     */
    inner class NewVideoAdapter : 
        RecyclerView.Adapter<NewVideoAdapter.VideoViewHolder>() {
        
        private val items = mutableListOf<ExploreContent>()
        
        /**
         * 更新数据
         */
        fun updateData(newItems: List<ExploreContent>, isRefresh: Boolean) {
            if (isRefresh) {
                items.clear()
            }
            val startPosition = items.size
            items.addAll(newItems)
            
            if (isRefresh) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeInserted(startPosition, newItems.size)
            }
        }
        
        /**
         * 更新指定媒体ID的收藏状态
         */
        fun updateItemFavoriteStatus(mediaId: Long, isFavorite: Boolean): Boolean {
            val itemIndex = items.indexOfFirst { it.id == mediaId }
            if (itemIndex != -1) {
                val item = items[itemIndex]
                item.isFavorite = isFavorite
                
                // 如果项目当前可见，直接更新视图
                val viewHolder = (binding.vpContainer.getChildAt(0) as? RecyclerView)
                    ?.findViewHolderForAdapterPosition(itemIndex) as? VideoViewHolder
                
                viewHolder?.let {
                    // 获取视图组件
                    val ivFavorite = it.itemView.findViewById<ImageView>(R.id.iv_favorite)
                    val tvFavoriteStatus = it.itemView.findViewById<TextView>(R.id.tv_favorite_status)
                    
                    // 更新视图
                    ivFavorite.setImageResource(
                        if (isFavorite) R.drawable.ic_favorite 
                        else R.drawable.ic_favorite_border
                    )
                    tvFavoriteStatus.text = if (isFavorite) "已收藏" else "收藏"
                } ?: run {
                    // 如果视图不可见，通知适配器该项已变更
                    notifyItemChanged(itemIndex)
                }
                
                return true
            }
            return false
        }
        
        /**
         * 移除指定媒体ID的项目
         * @return 返回被移除项的位置，如果未找到则返回-1
         */
        fun removeItem(mediaId: Long): Int {
            val itemIndex = items.indexOfFirst { it.id == mediaId }
            if (itemIndex != -1) {
                items.removeAt(itemIndex)
                notifyItemRemoved(itemIndex)
                return itemIndex
            }
            return -1
        }
        
        /**
         * 获取指定位置的View
         */
        fun getViewAtPosition(position: Int): View? {
            return (binding.vpContainer.getChildAt(0) as? RecyclerView)
                ?.findViewHolderForAdapterPosition(position)
                ?.itemView
        }
        
        /**
         * 获取指定位置的数据
         */
        fun getItem(position: Int): ExploreContent? {
            return items.getOrNull(position)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
            val binding = ItemNewVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return VideoViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class VideoViewHolder(private val binding: ItemNewVideoBinding) : 
            RecyclerView.ViewHolder(binding.root) {
            
            fun bind(item: ExploreContent) {
                // 根据媒体类型设置不同的显示
                if (item.mediaType == "video") {
                    setupVideo(item)
                } else {
                    setupImage(item)
                }
                
                // 设置视频信息
                setupVideoInfo(item)
                
                // 设置用户信息
                setupUserInfo(item)
                
                // 设置收藏状态
                setupFavorite(item)
            }
            
            private fun setupVideo(item: ExploreContent) {
                // 显示视频封面
                binding.ivCover.visibility = View.VISIBLE
                binding.gridImage.visibility = View.GONE
                binding.likeView.visibility = View.VISIBLE
                
                // 加载封面图
                Glide.with(requireContext())
                    .load(item.coverURL.ifEmpty { item.resourceUrls.firstOrNull() })
                    .into(binding.ivCover)
                
                // 预加载视频
                if (item.resourceUrls.isNotEmpty()) {
                    preloadVideo(item.resourceUrls[0])
                }
            }
            
            private fun setupImage(item: ExploreContent) {
                // 显示九宫格图片
                binding.ivCover.visibility = View.GONE
                binding.gridImage.visibility = View.VISIBLE
                binding.likeView.visibility = View.GONE
                
                // 设置图片数据
                binding.gridImage.setData(item.resourceUrls)
            }
            
            private fun setupVideoInfo(item: ExploreContent) {
                // 设置用户昵称
                binding.tvNickname.text = "@${item.nickName}"
                
                // 设置内容
                binding.tvContent.text = item.mediaContent
                
                // 设置来源和标签
                binding.tvMediaSource.text = item.mediaSource
                binding.tvMediaLabel.text = item.mediaLabel
                
                // 设置创建时间
                try {
                    val date = dateFormat.parse(item.createDate)
                    date?.let {
                        binding.tvCreateDate.text = displayFormat.format(it)
                    }
                } catch (e: Exception) {
                    binding.tvCreateDate.text = item.createDate
                }
            }
            
            private fun setupUserInfo(item: ExploreContent) {
                // 加载用户头像
                Glide.with(requireContext())
                    .load(item.profileURL)
                    .placeholder(R.mipmap.ic_default_avatar)
                    .error(R.mipmap.ic_default_avatar)
                    .into(binding.ivAvatar)
                
                // 设置评论数
                binding.tvCommentCount.text = item.totalComment.toString()
                
                // 设置打赏数
                binding.tvRewardCount.text = item.totalDiamond.toString()
                
                // 设置点击事件
                binding.ivAvatar.setOnClickListener {
                    // 可以添加跳转到用户主页的逻辑
                }
                
                binding.llComment.setOnClickListener {
                    // 可以添加打开评论的逻辑
                }
                
                binding.llReward.setOnClickListener {
                    // 可以添加打赏的逻辑
                }
            }
            
            private fun setupFavorite(item: ExploreContent) {
                // 根据收藏状态设置图标
                binding.ivFavorite.setImageResource(
                    if (item.isFavorite) R.drawable.ic_favorite 
                    else R.drawable.ic_favorite_border
                )
                
                // 设置收藏文本
                binding.tvFavoriteStatus.text = if (item.isFavorite) "已收藏" else "收藏"
                
                // 设置点击事件
                binding.llFavorite.setOnClickListener {
                    // 防止快速点击导致重复请求
                    binding.llFavorite.isEnabled = false
                    
                    // 立即更新UI状态，提供即时反馈
                    val newState = !item.isFavorite
                    item.isFavorite = newState
                    
                    // 更新UI
                    binding.ivFavorite.setImageResource(
                        if (newState) R.drawable.ic_favorite 
                        else R.drawable.ic_favorite_border
                    )
                    binding.tvFavoriteStatus.text = if (newState) "已收藏" else "收藏"
                    
                    // 调用收藏/取消收藏API
                    model?.toggleFavorite(item.id, !newState) { apiSuccess ->
                        // 操作完成后恢复点击功能
                        binding.llFavorite.isEnabled = true
                        
                        // 如果API失败，恢复原状态
                        if (!apiSuccess) {
                            item.isFavorite = !newState
                            binding.ivFavorite.setImageResource(
                                if (!newState) R.drawable.ic_favorite 
                                else R.drawable.ic_favorite_border
                            )
                            binding.tvFavoriteStatus.text = if (!newState) "已收藏" else "收藏"
                            ToastUtils.showShort("操作失败，请稍后重试")
                        } else {
                            // 提示用户
                            ToastUtils.showShort(if (newState) "收藏成功" else "已取消收藏")
                        }
                    }
                }
            }
            
            private fun preloadVideo(url: String) {
                // 预加载视频到缓存
                try {
                    val mediaItem = MediaItem.fromUri(url)
                    val dataSourceFactory = CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(
                            DefaultDataSource.Factory(requireContext())
                        )
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
} 