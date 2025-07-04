package com.duihua.chat.ui.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityCreateMediaBinding
import com.duihua.chat.databinding.ItemMediaImageBinding
import com.duihua.chat.databinding.ItemMediaVideoBinding
import com.duihua.chat.util.test.ImagePickerHelper
import com.duihua.chat.util.test.setupWithStringList

class CreateMediaActivity : BaseActivity<ActivityCreateMediaBinding, MediaModel>() {

    companion object {
        fun launch(context: Context, isImage: Boolean) {
            context.startActivity(Intent().apply {
                setClass(context, CreateMediaActivity::class.java)
                putExtra("isImage", isImage)
            })
        }
    }

    private var isImage = false
    private lateinit var imagePickerHelper: ImagePickerHelper
    
    // 选择的媒体文件URI列表
    private val selectedMediaUris = mutableListOf<Uri>()
    
    // 媒体来源和权限映射
    private val sourceOptions = arrayListOf("自行拍摄", "网络取材")
    private val authOptions = arrayListOf("公开", "仅互关朋友", "仅自己")
    private val authValues = arrayListOf("PUBLIC", "FRIENDS", "PRIVATE")
    private val themeOptions = arrayListOf("吃瓜", "搞笑娱乐", "影视", "游戏", "美食", "旅行", "穿搭", "健康", "其他")
    
    // 当前选择的选项位置
    private var currentSourcePosition = 0
    private var currentAuthPosition = 0
    private var currentThemePosition = 0

    override fun initBinding(): ActivityCreateMediaBinding {
        return ActivityCreateMediaBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar, "发布作品")
        isImage = intent.getBooleanExtra("isImage", false)
        imagePickerHelper = ImagePickerHelper(this)
        
        // 添加媒体按钮点击事件
        binding.ivAdd.setOnClickListener {
            if (isImage) {
                // 检查是否已达到最大选择数量
                if (selectedMediaUris.size >= 9) {
                    ToastUtils.showShort("最多只能选择9张图片")
                    return@setOnClickListener
                }
                // 选择图片
                imagePickerHelper.pickImage { uri ->
                    uri?.let { 
                        selectedMediaUris.add(it)
                        updateMediaDisplay()
                    }
                }
            } else {
                // 视频模式下，如果已经有视频，就不能再选
                if (selectedMediaUris.isNotEmpty()) {
                    ToastUtils.showShort("只能选择一个视频")
                    return@setOnClickListener
                }
                // 选择视频
                imagePickerHelper.pickVideo { uri ->
                    uri?.let {
                        selectedMediaUris.add(it)
                        updateMediaDisplay()
                    }
                }
            }
        }
        
        // 发布按钮点击事件
        binding.btnSend.setOnClickListener {
            uploadMedia()
        }
        
        // 初始化下拉菜单
        setupSpinners()
    }
    
    /**
     * 设置下拉菜单
     */
    private fun setupSpinners() {
        // 作品来源
        binding.spinnerSource.setupWithStringList(sourceOptions) { position: Int, _ ->
            currentSourcePosition = position
        }
        
        // 可见性设置
        binding.spinnerSee.setupWithStringList(authOptions) { position: Int, _ ->
            currentAuthPosition = position
        }
        
        // 主题类型
        binding.spinnerTheme.setupWithStringList(themeOptions) { position: Int, _ ->
            currentThemePosition = position
        }
    }
    
    /**
     * 更新媒体显示
     */
    private fun updateMediaDisplay() {
        if (isImage) {
            updateImageDisplay()
        } else {
            updateVideoDisplay()
        }
    }
    
    /**
     * 更新图片显示
     */
    private fun updateImageDisplay() {
        // 清空现有视图
        binding.gridImage.removeAllViews()
        
        // 添加每张图片
        selectedMediaUris.forEachIndexed { index, uri ->
            val imageBinding = ItemMediaImageBinding.inflate(LayoutInflater.from(this))
            
            // 加载图片
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(imageBinding.ivMedia)
            
            // 设置删除按钮
            imageBinding.ivDelete.setOnClickListener {
                selectedMediaUris.removeAt(index)
                updateMediaDisplay()
            }
            
            // 添加到网格
            binding.gridImage.addView(imageBinding.root)
        }
        
        // 添加"添加"按钮，如果没有达到上限
        if (selectedMediaUris.size < 9) {
            binding.gridImage.addView(binding.ivAdd)
        }
    }
    
    /**
     * 更新视频显示
     */
    private fun updateVideoDisplay() {
        // 清空现有视图
        binding.gridImage.removeAllViews()
        
        if (selectedMediaUris.isNotEmpty()) {
            val videoBinding = ItemMediaVideoBinding.inflate(LayoutInflater.from(this))
            
            // 将视频显示视图的布局参数设置为匹配父容器宽度
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
            layoutParams.columnSpec = GridLayout.spec(0, 3) // 横跨3列
            videoBinding.root.layoutParams = layoutParams
            
            // 加载视频缩略图
            Glide.with(this)
                .load(selectedMediaUris[0])
                .centerCrop()
                .into(videoBinding.ivVideoThumb)
            
            // 设置删除按钮
            videoBinding.ivDelete.setOnClickListener {
                selectedMediaUris.clear()
                updateMediaDisplay()
            }
            
            // 添加到容器
            binding.gridImage.addView(videoBinding.root)
            
            // 隐藏添加按钮，因为视频只能有一个
            binding.ivAdd.visibility = View.GONE
        } else {
            // 如果没有选择视频，显示添加按钮
            binding.ivAdd.visibility = View.VISIBLE
            binding.gridImage.addView(binding.ivAdd)
        }
    }
    
    /**
     * 上传媒体
     */
    private fun uploadMedia() {
        // 检查是否选择了媒体
        if (selectedMediaUris.isEmpty()) {
            ToastUtils.showShort("请选择${if (isImage) "图片" else "视频"}")
            return
        }
        
        // 获取文本内容
        val content = binding.etContent.text.toString().trim()
        
        // 确认上传
        AlertDialog.Builder(this)
            .setTitle("确认发布")
            .setMessage("确定要发布这${if (isImage) "些图片" else "个视频"}吗？")
            .setPositiveButton("确定") { _, _ ->
                // 执行上传
                model?.uploadMediaFiles(
                    fileUris = selectedMediaUris,
                    content = content,
                    mediaType = if (isImage) "image" else "video",
                    mediaSource = sourceOptions[currentSourcePosition],
                    mediaAuth = authValues[currentAuthPosition],
                    mediaLabel = themeOptions[currentThemePosition]
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun initViewModel(): MediaModel {
        return viewModels<MediaModel>().value
    }

    override fun initObserve() {
        // 观察上传状态
        model?.uploadingState?.observe(this) { isUploading ->
            binding.btnSend.isEnabled = !isUploading
            if (isUploading) {
                ToastUtils.showLong("正在上传，请稍候...")
            }
        }
        
        // 观察上传完成事件
        model?.uploadCompleteEvent?.observe(this) { isSuccess ->
            if (isSuccess) {
                ToastUtils.showShort("发布成功")
                finish()
            }
        }
    }
}