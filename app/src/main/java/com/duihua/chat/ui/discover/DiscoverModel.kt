package com.duihua.chat.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.drake.net.Post
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.bean.ExploreListResult
import com.duihua.chat.net.NetApi

class DiscoverModel : ViewModel() {

    val tabLiveData = MutableLiveData<ArrayList<String>>(arrayListOf<String>("朋友","推荐"))
    val exploreContentLiveData = MutableLiveData<Pair<Boolean,ArrayList<ExploreContent>>>()
    val requestCancelLiveData = MutableLiveData<Boolean>()
    // 收藏状态变更事件
    val favoriteChangedLiveData = MutableLiveData<Pair<Long, Boolean>>()
    var title = ""
    private var pageNo: Int = 1

    fun getLabel() {
        scopeNetLife {
            Get<ArrayList<String>>(NetApi.API_DISCOVER_LABEL).await().let {
                val tableList = tabLiveData.value!!
                tableList.addAll(it)
                tabLiveData.setValue(tableList)
            }
        }
    }

    fun favoritesList(refresh: Boolean) {
        if (refresh) {
            pageNo = 0
        } else {
            pageNo++
        }
        scopeNetLife {
            Get<ExploreListResult?>(NetApi.API_FAVORITES_LIST){
                param("pageNo",pageNo)
                param("pageSize",20)
            }.await()?.let {
                if (refresh) {
                    exploreContentLiveData.setValue(Pair(true,it.content))
                } else {
                    val list = exploreContentLiveData.value?.second ?: arrayListOf()
                    list.addAll(it.content)
                    exploreContentLiveData.setValue(Pair(false,list))
                }
            }
        }.finally({
            requestCancelLiveData.setValue(refresh)
        })
    }

    fun exploreList(refresh: Boolean) {
        if (refresh) {
            pageNo = 0
        } else {
            pageNo++
        }
        scopeNetLife {
            Get<ExploreListResult?>(NetApi.API_DISCOVER_DETAIL_LIST){
                param("mediaLabel",title)
                param("pageNo",pageNo)
                param("pageSize",20)
            }.await()?.let {
                if (refresh) {
                    exploreContentLiveData.setValue(Pair(true,it.content))
                } else {
                    val list = exploreContentLiveData.value?.second ?: arrayListOf()
                    list.addAll(it.content)
                    exploreContentLiveData.setValue(Pair(false,list))
                }
            }
        }.finally({
            requestCancelLiveData.setValue(refresh)
        })
    }

    /**
     * 收藏或取消收藏媒体内容
     * @param mediaId 媒体ID
     * @param isFavorite 当前是否已收藏，用于决定操作类型
     * @param callback 操作完成后的回调，参数为操作是否成功
     */
    fun toggleFavorite(mediaId: Long, isFavorite: Boolean, callback: ((Boolean) -> Unit)? = null) {
        // 确定操作类型：收藏或取消收藏
        val action = if (isFavorite) "CANCEL_FAVORITE" else "FAVORITE"
        
        scopeNetLife {
            try {
                Post<Any?>(NetApi.API_FAVORITES_MEDIA) {
                    json(
                        "action" to action,
                        "mediaIDs" to arrayListOf(mediaId)
                    )
                }.await()
                
                // 操作成功，发送事件通知
                val newFavoriteState = !isFavorite
                favoriteChangedLiveData.postValue(Pair(mediaId, newFavoriteState))
                
                // 调用回调，传递操作成功
                callback?.invoke(true)
            } catch (e: Exception) {
                e.printStackTrace()
                // 操作失败，调用回调，传递操作失败
                callback?.invoke(false)
            }
        }
    }
    
    /**
     * 更新列表中指定ID的项目的收藏状态
     * @param mediaId 媒体ID
     * @param isFavorite 新的收藏状态
     * @return 是否成功更新了状态
     */
    fun updateItemFavoriteStatus(mediaId: Long, isFavorite: Boolean): Boolean {
        val currentList = exploreContentLiveData.value?.second ?: return false
        
        // 寻找并更新对应项
        val itemIndex = currentList.indexOfFirst { it.id == mediaId }
        if (itemIndex != -1) {
            val item = currentList[itemIndex]
            item.isFavorite = isFavorite
            // 发布更新的列表
            exploreContentLiveData.postValue(Pair(false, currentList))
            return true
        }
        
        return false
    }
}