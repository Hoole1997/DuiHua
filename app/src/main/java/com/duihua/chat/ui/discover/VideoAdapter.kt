package com.duihua.chat.ui.discover

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseDifferAdapter
import com.duihua.chat.R
import com.duihua.chat.base.BaseAdapter
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.databinding.ItemVideoBinding
import com.duihua.chat.ui.discover.VideoAdapter.VideoViewHolder
import com.duihua.chat.widget.player.CacheManager
import com.duihua.chat.widget.player.VideoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache


/**
 * create by libo
 * create on 2020-05-20
 * description
 */
class VideoAdapter(): BaseDifferAdapter<ExploreContent,RecyclerView.ViewHolder>(VideoDiff()) {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    var isLoading = false // 是否正在加载
    var isLastPage = false // 是否最后一页

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == TYPE_LOADING) {
            return LoadingViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false))
        }
        return VideoViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * 构建一个共用缓存文件
     */
    val cache: SimpleCache by lazy {
//        //构建缓存文件
//        val cacheFile = context.cacheDir.resolve("tiktok_cache_file$this.hashCode()")
//        //构建simpleCache缓存实例
//        SimpleCache(cacheFile, LeastRecentlyUsedCacheEvictor(VideoPlayer.MAX_CACHE_BYTE), StandaloneDatabaseProvider(context))
        CacheManager.getInstance(context)
    }

    /**
     * 构建当前url视频的缓存
     */
    private fun buildMediaSource(url: String): ProgressiveMediaSource? {
        try {
//开启缓存文件
            val mediaItem = MediaItem.fromUri(url)
            //构建 DataSourceFactory
            val dataSourceFactory = CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
            //构建 MediaSource
            return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 通过position获取当前item.rootview
     */
    fun getRootViewAt(position: Int): View? {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        return if (viewHolder != null && viewHolder is VideoViewHolder) {
            viewHolder.itemView
        } else {
            null
        }
    }

    override fun getItemViewType(position: Int, list: List<ExploreContent>): Int {
        return if (position == list.size) TYPE_LOADING else TYPE_ITEM
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        item: ExploreContent?
    ) {
        item?.let {
            if (holder is VideoViewHolder) {
                if (item.mediaType.equals("video")) {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.resourceUrls[0])
                        .into(holder.binding.ivCover)
                    holder.binding.ivPlay.alpha = 0.4f
                    holder.binding.gridImage.visibility = View.GONE
                    //利用预加item，提前加载缓存资源
                    buildMediaSource(item.resourceUrls[0])
                    holder.binding.likeview.visibility = View.VISIBLE
                } else {
                    holder.binding.gridImage.setData(item.resourceUrls)
                    holder.binding.gridImage.visibility = View.VISIBLE
                    holder.binding.likeview.visibility = View.GONE
                }
                holder.binding.controller.setVideoData(item)
            } else {

            }
        }
    }

//    // 设置加载状态
//    fun setLoading(isLoading: Boolean) {
//        this.isLoading = isLoading
//        notifyDataSetChanged()
//    }

    // 设置是否是最后一页
//    fun setLastPage(isLastPage: Boolean) {
//        this.isLastPage = isLastPage
//    }

    inner class VideoViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class VideoDiff: DiffUtil.ItemCallback<ExploreContent>() {
    override fun areItemsTheSame(oldItem: ExploreContent, newItem: ExploreContent): Boolean {
        return (oldItem.id == newItem.id)
    }

    override fun areContentsTheSame(oldItem: ExploreContent, newItem: ExploreContent): Boolean {
        return oldItem.resourceUrls.size == newItem.resourceUrls.size
    }

}