package com.duihua.chat.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.drake.net.Post
import com.duihua.chat.bean.CommentItem
import com.duihua.chat.bean.CommentResponse
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.bean.ExploreListResult
import com.duihua.chat.net.NetApi
import com.duihua.chat.net.UserManager

class DiscoverModel : ViewModel() {

    val tabLiveData = MutableLiveData<ArrayList<String>>(arrayListOf<String>("朋友","推荐"))
    val exploreContentLiveData = MutableLiveData<Pair<Boolean,ArrayList<ExploreContent>>>()
    val requestCancelLiveData = MutableLiveData<Boolean>()
    // 收藏状态变更事件
    val favoriteChangedLiveData = MutableLiveData<Pair<Long, Boolean>>()
    // 评论数据
    val commentsLiveData = MutableLiveData<Pair<Boolean, List<CommentItem>>>()
    // 评论请求完成事件
    val commentRequestCancelLiveData = MutableLiveData<Boolean>()
    
    var title = ""
    private var pageNo: Int = 1
    private var commentPageNo: Int = 0

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
    
    /**
     * 加载评论列表
     * @param mediaId 媒体ID
     * @param refresh 是否刷新（如果是则重置页码）
     */
    fun loadComments(mediaId: Long, refresh: Boolean) {
        if (refresh) {
            commentPageNo = 0
        } else {
            commentPageNo++
        }
        
        scopeNetLife {
            try {
                val response = Get<CommentResponse>(NetApi.API_COMMENT_LIST) {
                    param("mediaID", mediaId)
                    param("pageNo", commentPageNo)
                    param("pageSize", 20)
                }.await()
                
                if (response.code == "200") {
                    // 发送评论列表
                    if (refresh) {
                        commentsLiveData.postValue(Pair(true, response.body.content))
                    } else {
                        val currentList = commentsLiveData.value?.second ?: emptyList()
                        val newList = currentList.toMutableList()
                        newList.addAll(response.body.content)
                        commentsLiveData.postValue(Pair(false, newList))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                commentRequestCancelLiveData.postValue(refresh)
            }
        }
    }
    
    /**
     * 发表评论
     * @param mediaId 媒体ID
     * @param content 评论内容
     * @param replyToUser 回复的用户ID，可为空
     * @param replyToComment 回复的评论ID，可为空
     * @param callback 操作完成后的回调，参数为操作是否成功
     */
    fun postComment(
        mediaId: Long,
        content: String,
        replyToUser: Long? = null,
        replyToComment: Long? = null,
        callback: ((Boolean) -> Unit)? = null
    ) {
        // 获取当前用户ID
        val currentUserId = UserManager.userInfo()?.id ?: run {
            callback?.invoke(false)
            return
        }
        
        val params = mutableMapOf<String, Any>(
            "id" to mediaId,
            "mediaID" to mediaId,
            "fromUser" to currentUserId,
            "content" to content
        )
        
        // 添加回复信息（如果有）
        replyToUser?.let { params["replyToUser"] = it }
        replyToComment?.let { params["replyToComment"] = it }
        
        scopeNetLife {
            try {
                Post<Any?>(NetApi.API_COMMENT_MEDIA) {
                    json(params)
                }.await()
                
                // 操作成功
                callback?.invoke(true)
            } catch (e: Exception) {
                e.printStackTrace()
                // 操作失败
                callback?.invoke(false)
            }
        }
    }
}