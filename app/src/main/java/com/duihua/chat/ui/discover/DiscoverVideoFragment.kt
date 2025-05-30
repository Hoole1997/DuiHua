package com.duihua.chat.ui.discover

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout.LayoutParams
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.duihua.chat.R
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.bean.PauseVideoEvent
import com.duihua.chat.databinding.FragmentDiscoverVideoBinding
import com.duihua.chat.util.test.RxBus
import com.duihua.chat.widget.ControllerView
import com.duihua.chat.widget.LikeView
import com.duihua.chat.widget.OnVideoControllerListener
import com.duihua.chat.widget.player.VideoPlayer
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import rx.Subscription
import rx.functions.Action1

class DiscoverVideoFragment : BaseFragment<FragmentDiscoverVideoBinding, DiscoverModel>() {

    companion object {
        fun newInstance(title: String) : DiscoverVideoFragment{
            return DiscoverVideoFragment().apply {
                arguments = Bundle().apply {
                    putString("title",title)
                }
            }
        }
    }

    private var currentPage = 1
    private lateinit var tiktok3Adapter: VideoAdapter

    /** 当前播放视频位置  */
    private var curPlayPos = -1
    private lateinit var videoView: VideoPlayer

    private var ivCurCover: ImageView? = null
    private var ivPlay: ImageView? = null
    private var subscribe: Subscription?= null

    override fun initBinding(): FragmentDiscoverVideoBinding {
        return FragmentDiscoverVideoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        model?.title = arguments?.getString("title") ?:return

        initRecyclerView()
        initVideoPlayer()
        setViewPagerLayoutManager()
        setRefreshEvent()
    }

    private fun setRefreshEvent() {
        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                model?.exploreList(false)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                model?.exploreList(true)
            }
        })
        binding.refreshLayout.setEnableLoadMore(false)
    }

    override fun initViewModel(): DiscoverModel? {
        return viewModels<DiscoverModel>().value
    }

    override fun initObserve() {
        model?.requestCancelLiveData?.observe(this) {
            if (it) {
                binding.refreshLayout.finishRefresh()
            } else {
                binding.refreshLayout.finishLoadMore()
                tiktok3Adapter.isLoading = false // 隐藏加载中
                tiktok3Adapter.notifyDataSetChanged()
            }
        }
        model?.exploreContentLiveData?.observe(this) {
            tiktok3Adapter.submitList(it.second)
            if (it.first) {
                binding.vpContainer.setCurrentItem(0)
            }
        }
        subscribe = RxBus.getDefault().toObservable(PauseVideoEvent::class.java)
            .subscribe(Action1 { event: PauseVideoEvent ->
                if (event.isPlayOrPause) {
                    videoView!!.play()
                } else {
                    videoView!!.pause()
                }
            } as Action1<PauseVideoEvent>)
    }

    private fun initRecyclerView() {
        tiktok3Adapter  = VideoAdapter()
//        tiktok3Adapter.setStateViewLayout(requireActivity(),R.layout.layout_empty)
//        tiktok3Adapter.isStateViewEnable = true
        binding.vpContainer.adapter = tiktok3Adapter
//        tiktok3Adapter.submitList(arrayListOf())

        (binding.vpContainer.getChildAt(0) as RecyclerView).addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 判断是否滑动到最后一项且未加载完成
                if (!tiktok3Adapter.isLoading && !tiktok3Adapter.isLastPage &&
                    lastVisibleItemPosition == totalItemCount - 1
                ) {
                    currentPage++
                    model?.exploreList(false)
                    tiktok3Adapter.isLoading = true
                    tiktok3Adapter.notifyDataSetChanged()
//                    loadData(currentPage) // 加载更多数据
                }
            }
        })
    }

    private fun initVideoPlayer() {
        var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        videoView = VideoPlayer(requireActivity())
        videoView.layoutParams = params
        lifecycle.addObserver(videoView)
    }

    private fun setViewPagerLayoutManager() {
        with(binding.vpContainer) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
            registerOnPageChangeCallback(pageChangeCallback)
//            (binding.vpContainer.getChildAt(0) as RecyclerView).scrollToPosition(PlayListActivity.initPos)
        }
    }

    private fun playCurVideo(position: Int) {
        if (position == curPlayPos) {
            return
        }
        val itemView = tiktok3Adapter!!.getRootViewAt(position)
        val rootView = itemView!!.findViewById<ViewGroup>(R.id.rl_container)
        val likeView: LikeView = rootView.findViewById(R.id.likeview)
        val controllerView: ControllerView = rootView.findViewById(R.id.controller)
        val ivPlay = rootView.findViewById<ImageView>(R.id.iv_play)
        val ivCover = rootView.findViewById<ImageView>(R.id.iv_cover)
        this.ivCurCover = ivCover
        this.ivPlay = ivPlay
        //播放暂停事件
        likeView.setOnPlayPauseListener(object: LikeView.OnPlayPauseListener {
            override fun onPlayOrPause() {
                if (videoView!!.isPlaying()) {
                    videoView?.pause()
                    ivPlay.visibility = View.VISIBLE
                } else {
                    videoView?.play()
                    ivPlay.visibility = View.GONE
                }
            }
        })
        likeView.setOnLikeListener(object : LikeView.OnLikeListener {
            override fun onLikeListener() {

            }
        })

        //评论点赞事件
        likeShareEvent(controllerView)

        //切换播放视频的作者主页数据
//        RxBus.getDefault().post(CurUserBean(DataCreate.datas[position]?.userBean!!))
        curPlayPos = position

        //切换播放器位置
        dettachParentView(rootView)
        autoPlayVideo(curPlayPos, ivCover)
    }

    /**
     * 用户操作事件
     */
    private fun likeShareEvent(controllerView: ControllerView) {
        controllerView.setListener(object : OnVideoControllerListener {
            override fun onHeadClick() {
//                RxBus.getDefault().post(MainPageChangeEvent(1))
            }

            override fun onLikeClick() {}
            override fun onCommentClick() {
//                val commentDialog = CommentDialog()
//                commentDialog.show(childFragmentManager, "")
            }

            override fun onShareClick() {
//                ShareDialog().show(childFragmentManager, "")
            }
        })
    }

    /**
     * 移除videoview父view
     */
    private fun dettachParentView(rootView: ViewGroup) {
        //1.添加videoView到当前需要播放的item中,添加进item之前，保证videoView没有父view
        videoView?.parent?.let {
            (it as ViewGroup).removeView(videoView)
        }

        rootView.addView(videoView, 0)
    }

    /**
     * 自动播放视频
     */
    private fun autoPlayVideo(position: Int, ivCover: ImageView) {
        videoView.playVideo(tiktok3Adapter!!.getItem(position)!!.resourceUrls[0]!!)

        videoView.getplayer()?.addListener(object: Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                // 播放状态发生变化时的回调
                // 播放状态包括：Player.STATE_IDLE、Player.STATE_BUFFERING、Player.STATE_READY、Player.STATE_ENDED
                if (state == Player.STATE_READY) {

                }
            }

            fun onPlayerError(error: ExoPlaybackException?) {
                // 播放发生错误时的回调
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // 播放状态变为播放或暂停时的回调
            }

            override fun onRenderedFirstFrame() {
                //第一帧已渲染，隐藏封面
                ivCover.visibility = View.GONE
                ivCurCover = ivCover
            }
        })
    }

    private val pageChangeCallback = object: OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            playCurVideo(position)
        }
    }

    override fun lazyLoad() {
        super.lazyLoad()
        model?.exploreList(true)
    }

    override fun onResume() {
        super.onResume()
        if (videoView.isPlaying()) {
            videoView.pause()
            ivCurCover?.visibility = View.VISIBLE
        } else {
            videoView.play()
            ivCurCover?.visibility = View.GONE
            ivPlay?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribe?.unsubscribe()
    }
}