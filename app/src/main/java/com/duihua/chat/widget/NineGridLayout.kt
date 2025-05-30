package com.duihua.chat.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener
import com.lxj.xpopup.util.SmartGlideImageLoader

class NineGridLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var imageUrls: List<String>? = null // 图片URL列表
    private val spacing = 8.dpToPx() // 图片间距，单位：px
//    private val imageViewList: List<ImageView> = null
    /**
     * 设置图片数据
     *
     * @param imageUrls 图片URL列表
     */
    fun setData(imageUrls: List<String>?) {
        if (this.imageUrls == imageUrls) return // 数据相同，避免重复渲染
        this.imageUrls = imageUrls
        removeAllViews() // 清除旧View
        imageUrls?.forEachIndexed {index, url ->
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    showImageDialog(this,index,imageUrls)
                }
            }
            addView(imageView)
//            imageViewList
            Glide.with(context).load(url).into(imageView) // 加载图片
        }
        requestLayout() // 重新布局
    }

    private fun showImageDialog(imageView: ImageView,position: Int,list: List<String>) {
        XPopup.Builder(context)
            .asImageViewer(imageView,position,list,object : OnSrcViewUpdateListener {
                override fun onSrcViewUpdate(
                    popupView: ImageViewerPopupView,
                    position: Int
                ) {
                    popupView.updateSrcView(getChildAt(position) as ImageView)
                }
            },SmartGlideImageLoader())
            .show()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec) // 获取宽度
        val itemSize = (width - 2 * spacing) / 3 // 每个图片的尺寸
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(
                MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY)
            )
        }
        setMeasuredDimension(width, width) // 设置View的宽高
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val itemSize = (width - 2 * spacing) / 3 // 每个图片的尺寸
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val row = i / 3 // 行号
            val col = i % 3 // 列号
            val left = col * (itemSize + spacing)
            val top = row * (itemSize + spacing)
            val right = left + itemSize
            val bottom = top + itemSize
            child.layout(left, top, right, bottom)
        }
    }

    // 扩展函数：dp转px
    private fun Int.dpToPx(): Int {
        val density = context.resources.displayMetrics.density
        return (this * density).toInt()
    }
}
