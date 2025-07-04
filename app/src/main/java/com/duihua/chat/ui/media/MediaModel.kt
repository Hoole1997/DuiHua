package com.duihua.chat.ui.media

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UriUtils
import com.drake.net.Post
import com.duihua.chat.net.NetApi
import com.duihua.chat.ui.mine.MineModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MediaModel : ViewModel() {
    
    // 文件上传状态
    val uploadingState = MutableLiveData<Boolean>()
    // 上传完成事件
    val uploadCompleteEvent = MutableLiveData<Boolean>()
    
    // 是否正在上传
    private val isUploading = AtomicBoolean(false)
    // 媒体上传模型
    private val mineModel = MineModel()
    
    /**
     * 上传文件并发布媒体内容
     * @param fileUris 文件URI列表
     * @param content 文本内容
     * @param mediaType 媒体类型 (image/video)
     * @param mediaSource 媒体来源
     * @param mediaAuth 可见性权限
     * @param mediaLabel 媒体标签
     */
    fun uploadMediaFiles(
        fileUris: List<Uri>,
        content: String,
        mediaType: String,
        mediaSource: String,
        mediaAuth: String,
        mediaLabel: String
    ) {
        if (isUploading.get()) return
        if (fileUris.isEmpty()) {
            ToastUtils.showShort("请选择${if (mediaType == "image") "图片" else "视频"}")
            return
        }
        
        isUploading.set(true)
        uploadingState.setValue(true)
        
        // 转换URI为文件
        val files = fileUris.mapNotNull { uri ->
            try {
                UriUtils.uri2File(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        
        if (files.isEmpty()) {
            ToastUtils.showShort("文件处理失败")
            isUploading.set(false)
            uploadingState.setValue(false)
            return
        }
        
        val uploadedUrls = mutableListOf<String>()
        var uploadedCount = 0
        
        // 逐个上传文件
        files.forEach { file ->
            mineModel.uploadFile(file) { url ->
                uploadedUrls.add(url)
                uploadedCount++
                
                // 当所有文件都上传完成后，发布媒体内容
                if (uploadedCount == files.size) {
                    publishMedia(uploadedUrls, content, mediaType, mediaSource, mediaAuth, mediaLabel)
                }
            }
        }
    }
    
    /**
     * 发布媒体内容
     */
    private fun publishMedia(
        resourceURLs: List<String>,
        content: String,
        mediaType: String,
        mediaSource: String,
        mediaAuth: String,
        mediaLabel: String
    ) {
        scopeNetLife {
            // 创建资源URL数组
            val urlsArray = JSONArray().apply {
                resourceURLs.forEach { put(it) }
            }
            
            // 创建请求参数
            val params = JSONObject().apply {
                put("mediaContent", content)
                put("resourceURLs", urlsArray)
                put("mediaType", mediaType)
                put("mediaSource", mediaSource)
                put("mediaAuth", mediaAuth)
                put("mediaLabel", mediaLabel)
                put("fullScreen", true)
                // 可选的封面URL
                if (resourceURLs.isNotEmpty()) {
                    put("coverURL", resourceURLs[0])
                }
            }
            
            // 发送请求
            Post<Any?>(NetApi.API_MEDIA_UPLOAD) {
                json(params)
            }.await()
            
            // 通知上传完成
            uploadCompleteEvent.setValue(true)
            ToastUtils.showShort("发布成功")
        }.finally({
            isUploading.set(false)
            uploadingState.setValue(false)
        })
    }
}