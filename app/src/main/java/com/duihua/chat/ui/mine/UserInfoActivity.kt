package com.duihua.chat.ui.mine

import android.view.LayoutInflater
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ActivityUtils
import com.bumptech.glide.Glide
import com.duihua.chat.R
import com.duihua.chat.base.BaseActivity
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.databinding.ActivityUserInfoBinding
import com.duihua.chat.databinding.DialogEditBinding
import com.duihua.chat.net.UserManager
import com.lxj.xpopup.XPopup
import org.json.JSONObject

class UserInfoActivity : BaseActivity<ActivityUserInfoBinding, MineModel>() {
    override fun initBinding(): ActivityUserInfoBinding {
        return ActivityUserInfoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"资料设置")

        binding.itemAvatar.setOnClickListener {
            AvatarActivity.launch(this,true)
        }
        binding.itemCover.setOnClickListener {
            AvatarActivity.launch(this,false)
        }
        binding.itemName.setOnClickListener {
            showEditDialog(title = "修改名称","请输入你要修改的名称") {
                model?.basicEdit(JSONObject().apply {
                    put("nickName",it)
                })
            }
        }
        binding.itemIntroduce.setOnClickListener {
            showEditDialog(title = "修改介绍","请输入你要修改的介绍") {
                model?.basicEdit(JSONObject().apply {
                    put("introduction",it)
                })
            }
        }
        binding.itemGender.setOnClickListener {
            XPopup.Builder(this)
                .asCenterList("修改性别",arrayOf("男","女")) { position,text ->
                    model?.basicEdit(JSONObject().apply {
                        put("sex",if (position == 0) "1" else "2")
                    })
                }
                .show()
        }
    }

    override fun initViewModel(): MineModel? {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {
        model?.userInfoEvent?.observe(this) {
            setUserInfo(it)
        }
    }

    private fun setUserInfo(userInfo: UserInfo) {
        UserManager.updateUserInfo(userInfo)
        Glide.with(this).load(userInfo.profileURL).placeholder(R.mipmap.ic_default_avatar).error(R.mipmap.ic_default_avatar).into(binding.ivAvatar)
        Glide.with(this).load(userInfo.coverURL).placeholder(R.mipmap.bg_mine_default).error(R.mipmap.bg_mine_default).into(binding.ivCover)
        binding.tvNickname.text = userInfo.nickName
        binding.tvIntroduce.text = userInfo.introduction
        binding.tvGender.text = if (userInfo.sex == "MALE") {
            "男性"
        } else if (userInfo.sex == "FEMALE") {
            "女性"
        } else {
            "未知"
        }
    }

    private fun showEditDialog(title: String, hint: String, onConfirm: (String) -> Unit) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null)
        val etInput = view.findViewById<EditText>(R.id.et_input)
        etInput.setHint(hint)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setPositiveButton("确定") { dialog, _ ->
                val content = etInput.text.toString() ?: return@setPositiveButton
                onConfirm.invoke(content)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog,_->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        model?.refreshUserInfo()
    }

}