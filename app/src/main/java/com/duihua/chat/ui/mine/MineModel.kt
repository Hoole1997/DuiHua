package com.duihua.chat.ui.mine

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.alibaba.sdk.android.oss.signer.SignVersion
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.drake.net.Get
import com.drake.net.Post
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.bean.ExploreListResult
import com.duihua.chat.bean.MediaListResult
import com.duihua.chat.bean.OSSToken
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.bean.SearchAccountResult
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.bean.UserMedia
import com.duihua.chat.net.NetApi
import org.json.JSONObject
import java.io.File

class MineModel : ViewModel() {

    val userInfoEvent = MutableLiveData<UserInfo>()

    val requestCancelEvent = MutableLiveData<Pair<Boolean, Boolean>>()
    val mediaListEvent = MutableLiveData<ArrayList<ExploreContent>>()

    var isOther = false
    var page = 0

    var otherUserId = ""
    var userBlackEvent = false
    val otherUserInfoEvent = MutableLiveData<OtherUserInfo>()

    var ossTask:OSSAsyncTask<PutObjectResult>? = null

    fun refreshUserInfo() {
        scopeNetLife {
            if (isOther) {
                Get<OtherUserInfo>(NetApi.API_OTHER_USER) {
                    param("otherUserID", otherUserId)
                }.await().let {
                    otherUserInfoEvent.setValue(it)
                    refreshBlackState()
                }
            } else {
                Get<UserInfo>(NetApi.API_USER_BASIC) {

                }.await().let {
                    userInfoEvent.setValue(it)
                }
            }

        }
    }

    fun cancelFollow() {
        scopeNetLife {
            Post<Any?>(NetApi.API_CANCEL_FOLLOW) {
                json("otherUserID" to otherUserId)
            }.await()
            refreshUserInfo()
        }
    }

    fun follow(remarkName: String?, remarkPhone: String?, anonymity: Boolean) {
        scopeNetLife {
            Post<Any?>(NetApi.API_FOLLOW) {
                json(
                    "otherUserID" to otherUserId,
                    "remarkName" to remarkName,
                    "remarkPhone" to remarkPhone,
                    "anonymity" to anonymity
                )
            }.await()
            refreshUserInfo()
        }
    }

    fun mediaList(refresh: Boolean) {
        if (refresh) {
            page = 0
        } else {
            page++
        }
        scopeNetLife {
            Get<ExploreListResult>(if (isOther) NetApi.API_OTHER_USER_MEDIA_LIST else NetApi.API_USER_MEDIA_LIST) {
                if (isOther) {
                    param("ownerID", otherUserId)
                }
                param("pageNo", page)
                param("pageSize", 20)
            }.await().let {
                if (refresh) {
                    mediaListEvent.setValue(it.content)
                } else {
                    val list = mediaListEvent.value ?: arrayListOf()
                    list.addAll(it.content)
                    mediaListEvent.setValue(list)
                }
            }
        }.finally({
            if (refresh) {
                requestCancelEvent.setValue(Pair(true, false))
            } else {
                requestCancelEvent.setValue(Pair(false, true))
            }
        })
    }

    private fun refreshBlackState() {
        scopeNetLife {
            Get<SearchAccountResult?>(NetApi.API_SEARCH_ACCOUNT) {
                param("pageNo",0)
                param("pageSize",100)
                param("relationType","BLOCK")
            }.await()?.let {
                userBlackEvent = it.content.filter {
                    it.id.toString() == otherUserId
                }.size>0
            }
        }
    }

    fun changeBlackState(block: Boolean, onResult: (Boolean) -> Unit) {
        scopeNetLife {
            Post<Any?>(NetApi.API_BLOCK_ACCOUNT) {
                json(
                    "targetID" to otherUserId.toLong(),
                    "action" to if (block) "BLOCK" else "UNBLOCK"
                )
            }.await()
            userBlackEvent = block
            onResult.invoke(block)
            refreshUserInfo()
        }
    }

    fun changeRemark(
        remarkName: String?,
        remarkPhone: String?,
        anonymity: Boolean?,
        onResult: (remarkName: String?, remarkPhone: String?, anonymity: Boolean?) -> Unit
    ) {
        scopeNetLife {
            Post<Any?>(NetApi.API_UPDATE_REMARK) {
                json(JSONObject().apply {
                    put("otherUserID", otherUserId.toLong())
                    if (remarkName != null) {
                        put("remarkName", remarkName)
                    }
                    if (remarkPhone != null) {
                        put("remarkPhone", remarkPhone)
                    }
                    if (anonymity != null) {
                        put("anonymity", anonymity)
                    }
                })
            }.await()
            refreshUserInfo()
            onResult.invoke(remarkName,remarkPhone,anonymity)
        }
    }

    fun uploadFile(file: File,onUploadResult:(String) -> Unit) {
        LogUtils.d("上传文件 "+file.path)
        scopeNetLife {
            val ossTokenResult = Get<String>(NetApi.API_OSS_TOKEN).await()
            val ossToken = GsonUtils.fromJson<OSSToken>(ossTokenResult, OSSToken::class.java)
            val endpoint = "https://oss-cn-shanghai.aliyuncs.com"

            val credentialsProvider = OSSStsTokenCredentialProvider(ossToken.AccessKeyId,ossToken.AccessKeySecret,ossToken.SecurityToken)
            ClientConfiguration().apply {
                signVersion = SignVersion.V4
            }
            ToastUtils.showLong("上传中，请稍等...")
            val oss = OSSClient(Utils.getApp(),endpoint,credentialsProvider).apply {
                setRegion("cn-shanghai")
            }
            val bucketName = "cyn-duihua"
            val objectName = "android/${FileUtils.getFileName(file)}"
            val filePath = file.path
            val putObjectRequest = PutObjectRequest(bucketName,objectName,filePath).apply {
                progressCallback = object : OSSProgressCallback<PutObjectRequest> {
                    override fun onProgress(
                        request: PutObjectRequest?,
                        currentSize: Long,
                        totalSize: Long
                    ) {
                        LogUtils.d("$currentSize/$totalSize")
                    }
                }
            }
            ossTask = oss.asyncPutObject(putObjectRequest,object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(
                    request: PutObjectRequest?,
                    result: PutObjectResult?
                ) {
                    ToastUtils.showShort("上传成功")
                    onUploadResult.invoke("http://$bucketName.oss-cn-shanghai.aliyuncs.com/$objectName")
                }

                override fun onFailure(
                    request: PutObjectRequest?,
                    clientException: ClientException?,
                    serviceException: ServiceException?
                ) {
                    ToastUtils.showShort("上传失败")
                    clientException?.printStackTrace()
                    serviceException?.printStackTrace()
                }

            })

        }
    }

    fun basicEdit(jsonObject: JSONObject,onResult: (() -> Unit)? = null) {
        scopeNetLife {
            Post<Any?>(NetApi.API_USER_BASIC_EDIT) {
                json(jsonObject)
            }.await()
            refreshUserInfo()
            onResult?.invoke()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ossTask?.let {
            if (it.isCanceled || it.isCompleted) {

            } else {
                ToastUtils.showShort("取消上传")
                it.cancel()
            }
        }
    }

}