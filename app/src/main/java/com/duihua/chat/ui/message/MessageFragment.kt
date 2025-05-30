package com.duihua.chat.ui.message

import androidx.fragment.app.viewModels
import com.duihua.chat.base.BaseFragment
import com.duihua.chat.databinding.FragmentMessageBinding

class MessageFragment : BaseFragment<FragmentMessageBinding, MessageModel>() {
    override fun initBinding(): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun initViewModel(): MessageModel? {
        return viewModels<MessageModel>().value
    }

    override fun initObserve() {

    }
}