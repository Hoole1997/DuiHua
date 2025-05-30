package com.duihua.chat.ui.quick

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.duihua.chat.bean.SearchAccount
import com.duihua.chat.bean.SearchAccountResult
import com.duihua.chat.bean.SearchPhoneResult
import com.duihua.chat.net.NetApi

class QuickModel : ViewModel() {

    val searchPhoneEvent = MutableLiveData<SearchPhoneResult?>()
    val accountListEvent = MutableLiveData<ArrayList<SearchAccount>>()
    val requestCancelEvent = MutableLiveData<Pair<Boolean, Boolean>>()
    var page = 0
    fun searchPhone(phone: String) {
        scopeNetLife {
            Get<SearchPhoneResult?>(NetApi.API_SEARCH) {
                param("phone",phone)
            }.await().let {
                searchPhoneEvent.setValue(it)
            }
        }
    }

    fun queryList(type: String,refresh: Boolean) {
        if (refresh) {
            page = 0
        } else {
            page++
        }
        scopeNetLife {
            Get<SearchAccountResult?>(NetApi.API_SEARCH_ACCOUNT) {
                param("pageNo",page)
                param("pageSize",10)
                param("relationType",when(type) {
                    FragmentQuickFriend.QUICK_TYPE_FRIEND -> "MUTUAL"
                    FragmentQuickFriend.QUICK_TYPE_ATTENTION -> "FOLLOW"
                    FragmentQuickFriend.QUICK_TYPE_FANS -> "FANS"
                    FragmentQuickFriend.QUICK_TYPE_BLACK -> "BLOCK"
                    else -> ""
                })
            }.await()?.let {
                if (refresh) {
                    accountListEvent.setValue(it.content)
                } else {
                    val list = accountListEvent.value ?: arrayListOf()
                    list.addAll(it.content)
                    accountListEvent.setValue(list)
                }
            }
        }.finally({
            if (refresh) {
                requestCancelEvent.setValue(Pair(true,false))
            } else {
                requestCancelEvent.setValue(Pair(false,true))
            }
        })
    }

}