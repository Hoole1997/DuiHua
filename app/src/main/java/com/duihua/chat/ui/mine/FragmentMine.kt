@file:Suppress("DEPRECATION")

package com.duihua.chat.ui.mine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.BaseQuickAdapter.OnItemClickListener
import com.duihua.chat.R
import com.duihua.chat.base.BaseAdapter
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.bean.ExploreContent
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.bean.UserInfo
import com.duihua.chat.bean.UserMedia
import com.duihua.chat.databinding.DialogPersonalAttentionBinding
import com.duihua.chat.databinding.DialogPersonalSettingBinding
import com.duihua.chat.databinding.FragmentMineBinding
import com.duihua.chat.databinding.LayoutEmpty1Binding
import com.duihua.chat.net.UserManager
import com.duihua.chat.ui.auth.ServiceActivity
import com.duihua.chat.ui.chat.ChatActivity
import com.duihua.chat.ui.media.CreateMediaActivity
import com.duihua.chat.ui.setting.SettingActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gyf.immersionbar.ImmersionBar
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class FragmentMine : BaseFragment<FragmentMineBinding, MineModel>() {

    companion object {
        const val MINE_PARAM_USERID = "userId"
        const val MINE_PARAM_OTHER = "other"
        fun newInstance(isOther: Boolean, userId: String): FragmentMine {
            return FragmentMine().apply {
                arguments = Bundle().apply {
                    putString(MINE_PARAM_USERID, userId)
                    putBoolean(MINE_PARAM_OTHER, isOther)
                }
            }
        }
    }

    lateinit var mediaAdapter: UserMediaAdapter

    override fun initBinding(): FragmentMineBinding {
        return FragmentMineBinding.inflate(layoutInflater)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        model?.isOther = arguments?.getBoolean(MINE_PARAM_OTHER) ?: false
        model?.otherUserId = arguments?.getString(MINE_PARAM_USERID) ?: ""

        initRvMedia()

        if (model?.isOther == true) {
            binding.toolbar.navigationIcon =
                resources.getDrawable(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            binding.toolbar.navigationIcon?.setTint(resources.getColor(R.color.black))
            binding.toolbar.setNavigationOnClickListener {
                activity?.finish()
            }
            binding.toolbar.inflateMenu(R.menu.menu_personal)
            binding.toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.menu_edit) {
                    showMoreDialog()
                }
                true
            }
            binding.llProfile.isGone = true
            binding.llOther.isVisible = true
            binding.tvWork.setCompoundDrawablesRelative(null, null, null, null)
            binding.btnAttention.setOnClickListener {
                model?.otherUserInfoEvent?.value?.let {
                    if (it.isFollow) {
                        ChatActivity.launch(requireActivity(),it.id)
                    } else {
                        showAttentionDialog()
                    }
                }
            }
            binding.btnHasAttention.setOnClickListener {
                showCancelAttentionDialog()
            }
        } else {
            binding.llProfile.isVisible = true
            binding.llOther.isGone = true
            binding.itemProfileService.setOnClickListener {
                ActivityUtils.startActivity(ServiceActivity::class.java)
            }
            binding.itemProfileEdit.setOnClickListener {
                ActivityUtils.startActivity(UserInfoActivity::class.java)
            }
            binding.itemProfileCollect.setOnClickListener {
                // 打开收藏列表
                com.duihua.chat.ui.media.MediaListActivity.launchFavorites(requireActivity())
            }
            binding.tvWork.setOnClickListener {
                XPopup.Builder(requireActivity())
                    .asCenterList("发布作品",arrayOf("图片","短视频")) { position,text ->
                        CreateMediaActivity.launch(requireActivity(),position == 0)
                    }
                    .show()
            }
            binding.itemProfileSetting.setOnClickListener {
                ActivityUtils.startActivity(SettingActivity::class.java)
            }
        }
        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                model?.mediaList(true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                model?.mediaList(false)
            }
        })

        model?.mediaList(true)
    }

    private fun initRvMedia() {
        mediaAdapter = UserMediaAdapter()
        mediaAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<ExploreContent> {
            override fun onClick(
                adapter: BaseQuickAdapter<ExploreContent, *>,
                view: View,
                position: Int
            ) {
                // 获取当前所有媒体列表
                val mediaList = ArrayList<ExploreContent>()
                mediaAdapter.items.forEach { item ->
                    mediaList.add(item)
                }
                
                // 打开MediaListActivity
                if (mediaList.isNotEmpty()) {
                    val nickname = if (model?.isOther == true) {
                        model?.otherUserInfoEvent?.value?.nickName ?: ""
                    } else {
                        UserManager.userInfo()?.nickName ?: ""
                    }
                    
                    val title = "$nickname 的作品"
                    com.duihua.chat.ui.media.MediaListActivity.launch(
                        requireActivity(),
                        mediaList,
                        position,
                        title
                    )
                }
            }
        })
        mediaAdapter.isStateViewEnable = true
        mediaAdapter.stateView = LayoutEmpty1Binding.inflate(layoutInflater).root
        binding.rvWork.adapter = mediaAdapter
    }

    private fun setUserView(userInfo: UserInfo) {
        UserManager.updateUserInfo(userInfo)
        binding.tvNickname.text = userInfo.showNickName()
        binding.tvIp.text = "IP: ${userInfo.region}"
        binding.tvFans.text = "粉丝: ${userInfo.fansNumber}"
        binding.tvProfile.text = userInfo.showProfile()
        Glide.with(this)
            .load(userInfo.coverURL)
            .error(R.mipmap.bg_mine_default)
            .into(binding.ivUserBg)
        Glide.with(this)
            .load(userInfo.profileURL)
            .error(R.mipmap.ic_default_avatar)
            .into(binding.ivAvatar)
    }

    private fun setOtherUserView(userInfo: OtherUserInfo) {
        binding.tvNickname.text = userInfo.showNickName()
        binding.tvIp.text = "IP: ${userInfo.region}"
        binding.tvFans.text = if (userInfo.anonymity) {
            "粉丝: ${userInfo.fansNumber}"
        } else {
            "粉丝: 私密"
        }
        binding.tvProfile.text = userInfo.showProfile()
        binding.btnHasAttention.isVisible = userInfo.isFollow
        binding.btnAttention.text = if (userInfo.isFollow) "对话" else "关注对话"
        Glide.with(this)
            .load(userInfo.coverURL)
            .error(R.mipmap.bg_mine_default)
            .into(binding.ivUserBg)
        Glide.with(this)
            .load(userInfo.profileURL)
            .error(R.mipmap.ic_default_avatar)
            .into(binding.ivAvatar)

    }

    override fun initViewModel(): MineModel? {
        return viewModels<MineModel>().value
    }

    override fun initObserve() {
        model?.userInfoEvent?.observe(this, ::setUserView)
        model?.otherUserInfoEvent?.observe(this, ::setOtherUserView)

        model?.mediaListEvent?.observe(this) { list ->
            if (list.isEmpty()) {
                binding.rvWork.layoutManager = LinearLayoutManager(requireActivity()).apply {
                    orientation = LinearLayoutManager.VERTICAL
                }
            } else {
                binding.rvWork.layoutManager = GridLayoutManager(requireActivity(), 2)
            }
            mediaAdapter.submitList(list)
        }
        model?.requestCancelEvent?.observe(this) {
            if (it.first) {
                binding.refreshLayout.finishRefresh()
            }
            if (it.second) {
                binding.refreshLayout.finishLoadMore()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ImmersionBar.with(this).statusBarDarkFont(true).statusBarView(binding.toolbar).init()
        model?.refreshUserInfo()
    }

    override fun lazyLoad() {
        super.lazyLoad()

    }

    private fun showAttentionDialog() {
        if (model?.otherUserInfoEvent?.value?.isFollow == true) return
        if (activity == null || activity?.isFinishing == true) return
        val dialog = BottomSheetDialog(requireActivity())
        val dialogBinding = DialogPersonalAttentionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)


        dialogBinding.btnConfirm.setOnClickListener {
            val remarkName = dialogBinding.etRemakeNickname.text.toString()
            val remarkPhone = dialogBinding.etRemakePhone.text.toString()
            val anonymity = dialogBinding.switchAnonymity.isChecked
            model?.follow(remarkName, remarkPhone, anonymity)
            dialog.dismiss()
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showCancelAttentionDialog() {
        if (activity == null || activity?.isFinishing == true) return
        AlertDialog.Builder(requireActivity())
            .setTitle("温馨提示")
            .setMessage("确定取消关注吗？")
            .setNeutralButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                model?.cancelFollow()
            }
            .show()
    }

    private fun showMoreDialog() {
        if (activity == null || activity?.isFinishing == true) return
        val dialog = BottomSheetDialog(requireActivity())
        val dialogBinding = DialogPersonalSettingBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.llNormal.isVisible = model?.otherUserInfoEvent?.value?.isFollow == true
        if (model?.otherUserInfoEvent?.value?.isFollow == true) {
            dialogBinding.tvRemarkNickname.text = model?.otherUserInfoEvent?.value?.remarkName ?:""
            dialogBinding.tvRemarkPhone.text = model?.otherUserInfoEvent?.value?.remarkPhone ?:""
            dialogBinding.switchAnonymity.isChecked = model?.otherUserInfoEvent?.value?.anonymity ?:false
        }
        dialogBinding.switchBlack.setChecked(model?.userBlackEvent!!)
        dialogBinding.switchBlack.setOnCheckedChangeListener { _, check ->
            model?.changeBlackState(check) {
                if (check) {
                    dialogBinding.llNormal.isGone = true
                }
            }
        }
        dialogBinding.tvRemarkNickname.setOnClickListener {
            showEditDialog(title = "备注昵称","请输入昵称") {
                model?.changeRemark(remarkName = it,null,null) { remarkName: String?, remarkPhone: String?, anonymity: Boolean? ->
                    dialogBinding.tvRemarkNickname.text = remarkName ?:""
                }
            }
        }
        dialogBinding.tvRemarkPhone.setOnClickListener {
            showEditDialog(title = "备注电话","请输入电话") {
                model?.changeRemark(null, remarkPhone = it,null) { remarkName: String?, remarkPhone: String?, anonymity: Boolean? ->
                    dialogBinding.tvRemarkPhone.text = remarkPhone ?:""
                }
            }
        }
        dialogBinding.switchAnonymity.setOnCheckedChangeListener {  _, check ->
            model?.changeRemark(null,null,check) { remarkName: String?, remarkPhone: String?, anonymity: Boolean? ->
                dialogBinding.switchAnonymity.isChecked = anonymity ?: false
            }
        }
        dialogBinding.tvRecommend.setOnClickListener {

        }
        dialogBinding.tvReport.setOnClickListener {

        }

        dialog.show()
    }

    private fun showEditDialog(title: String, hint: String, onConfirm: (String) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null)
        val etInput = view.findViewById<EditText>(R.id.et_input)
        etInput.setHint(hint)
        AlertDialog.Builder(requireActivity())
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


}