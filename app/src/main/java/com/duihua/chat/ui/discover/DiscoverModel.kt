package com.duihua.chat.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.bean.ExploreListResult
import com.duihua.chat.net.NetApi

class DiscoverModel : ViewModel() {

    val tabLiveData = MutableLiveData<ArrayList<String>>(arrayListOf<String>("朋友","推荐"))
    val exploreContentLiveData = MutableLiveData<Pair<Boolean,ArrayList<ExploreContent>>>()
    val requestCancelLiveData = MutableLiveData<Boolean>()
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

}