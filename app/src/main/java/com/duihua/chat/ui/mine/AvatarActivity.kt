package com.duihua.chat.ui.mine

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.UserManager
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.UriUtils
import com.bumptech.glide.Glide
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.databinding.ActivityAvatarBinding
import com.duihua.chat.util.test.ImagePickerHelper
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSelectListener
import org.json.JSONObject

class AvatarActivity : BaseActivity<ActivityAvatarBinding, MineModel>() {

    companion object {
        fun launch(context: Context,isAvatar: Boolean) {
            context.startActivity(Intent().apply {
                setClass(context, AvatarActivity::class.java)
                putExtra("isAvatar",isAvatar)
            })
        }
    }

    lateinit var imagePickerHelper: ImagePickerHelper
    var isAvatar = true

    override fun initBinding(): ActivityAvatarBinding {
        return ActivityAvatarBinding.inflate(layoutInflater)
    }

    override fun initView() {
        isAvatar = intent.getBooleanExtra("isAvatar",true)
        useDefaultToolbar(binding.toolbar,if (isAvatar) "设置头像" else "设置封面")

        imagePickerHelper = ImagePickerHelper(this)

        val url = if (isAvatar) {
            com.duihua.chat.net.UserManager.userInfo()?.profileURL
        } else {
            com.duihua.chat.net.UserManager.userInfo()?.coverURL
        }
        Glide.with(this).load(url).into(binding.ivAvatar)

        binding.btnChoose.setOnClickListener {
            XPopup.Builder(this).asBottomList("选择",arrayOf("相片","拍摄"),object : OnSelectListener {
                override fun onSelect(position: Int, text: String?) {
                    if (position == 0) {
                        imagePickerHelper.pickImage {
                            it?.let {
                                model?.uploadFile(UriUtils.uri2File(it)){
                                    LogUtils.d("上传成功" + it)
                                    model?.basicEdit(JSONObject().apply {
                                        put(if (isAvatar) "profileURL" else "coverURL",it)
                                    })
                                    Glide.with(this@AvatarActivity).load(it).into(binding.ivAvatar)
                                }
                            } ?: run {
//                                ToastUtils.showShort("获取图片失败")
                            }
                        }
                    } else {
                        imagePickerHelper.takePhoto {
                            it?.let {
                                model?.uploadFile(UriUtils.uri2File(it)) {
                                    LogUtils.d("上传成功" + it)
                                    Glide.with(this@AvatarActivity).load(it).into(binding.ivAvatar)
                                    model?.basicEdit(JSONObject().apply {
                                        put(if (isAvatar) "profileURL" else "coverURL",it)
                                    })
                                }

                            } ?: run {
//                                ToastUtils.showShort("获取图片失败")
                            }
                        }
                    }

                }
            }).show()
        }
    }

    override fun initViewModel(): MineModel? {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {

    }

}