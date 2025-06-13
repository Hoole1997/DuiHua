package com.duihua.chat.ui.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.drake.net.Post
import com.duihua.chat.bean.InComeDetail
import com.duihua.chat.bean.InComeDetailResult
import com.duihua.chat.bean.RechargeDetail
import com.duihua.chat.bean.RechargeDetailResult
import com.duihua.chat.bean.RechargeItem
import com.duihua.chat.net.NetApi

class WalletModel: ViewModel() {

    var pageNo = 0
    val withdrawLiveData = MutableLiveData<Pair<List<InComeDetail>, Boolean>>()
    val rechargeItemListLiveData = MutableLiveData<List<RechargeItem>>()
    val rechargeDetailItemListLiveData = MutableLiveData<Pair<List<RechargeDetail>, Boolean>>()

    fun requestWithdrawList(refresh: Boolean) {
        if (refresh) pageNo = 0 else pageNo++
        scopeNetLife {
            Get<InComeDetailResult?>(NetApi.API_WITHDRAW_LIST) {
                param("pageNo",pageNo)
                param("pageSize",20)
            }.await()?.let {
                withdrawLiveData.postValue(Pair(it.content,refresh))
            }
        }
    }

    fun withdraw(currency: Int,onResultListener:() -> Unit) {
        if (currency == 0)return
        scopeNetLife {
            Post<Any?>(NetApi.API_DO_WITHDRAW) {
                json("currency" to currency)
            }.await()
            onResultListener.invoke()
        }
    }

    fun rechargeItems() {
        scopeNetLife {
            Get<List<RechargeItem>?>(NetApi.API_RECHARGE_ITEM).await()?.let {
                rechargeItemListLiveData.postValue(it.filter {
                    !it.isDelete
                })
            }
        }
    }

    fun rechargeDetailList(refresh: Boolean) {
        if (refresh) pageNo = 0 else pageNo++
        scopeNetLife {
            Get<RechargeDetailResult?>(NetApi.API_DIAMOND_DETAIL_LIST) {
                param("pageNo",pageNo)
                param("pageSize",20)
            }.await()?.let {
                rechargeDetailItemListLiveData.postValue(Pair(it.content,refresh))
            }
        }
    }

}