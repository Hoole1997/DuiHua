package com.duihua.chat.ui.discover

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.duihua.chat.R
import com.duihua.chat.bean.CommentItem
import com.duihua.chat.databinding.FragmentCommentBottomSheetBinding
import com.duihua.chat.net.UserManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentBottomSheetFragment : BottomSheetDialogFragment() {
    
    companion object {
        private const val ARG_MEDIA_ID = "arg_media_id"
        
        fun newInstance(mediaId: Long): CommentBottomSheetFragment {
            return CommentBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_MEDIA_ID, mediaId)
                }
            }
        }
    }
    
    private var _binding: FragmentCommentBottomSheetBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DiscoverModel
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var behavior: BottomSheetBehavior<View>
    
    private var mediaId: Long = 0
    private var replyToUser: Long? = null
    private var replyToComment: Long? = null
    private var replyToNickname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaId = arguments?.getLong(ARG_MEDIA_ID) ?: 0
        setStyle(STYLE_NORMAL, R.style.CommentBottomSheetDialogTheme)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        
        dialog.setOnShowListener {
            val bottomSheet = (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                
                // 设置高度为屏幕高度的50%（半屏）
                val windowHeight = requireActivity().window.decorView.height
                val halfHeight = (windowHeight * 0.5).toInt()
                behavior.peekHeight = halfHeight
                
                // 设置固定高度
                val layoutParams = it.layoutParams
                layoutParams.height = halfHeight
                it.layoutParams = layoutParams
                
                // 禁止拖拽改变高度，只能关闭
                behavior.isDraggable = true
                behavior.skipCollapsed = true
                
                // 设置拖动回调
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            dismiss()
                        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            // 如果被折叠了，直接展开到固定高度
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // 可以根据滑动设置透明度等效果
                    }
                })
            }
            
            // 设置软键盘不会将底部表单推上去
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[DiscoverModel::class.java]
        
        // Setup UI components
        setupRecyclerView()
        setupRefreshLayout()
        setupCommentInput()
        setupObservers()
        
        // 设置点击空白区域收起键盘
        binding.rvComments.setOnClickListener {
            KeyboardUtils.hideSoftInput(binding.etComment)
        }
        
        // Load initial comments
        viewModel.loadComments(mediaId, true)
    }
    
    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }
    }
    
    private fun setupRefreshLayout() {
        binding.refreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    viewModel.loadComments(mediaId, true)
                }
                
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    viewModel.loadComments(mediaId, false)
                }
            })
        }
    }
    
    private fun setupCommentInput() {
        // Set submit button click listener
        binding.btnSubmit.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isEmpty()) {
                ToastUtils.showShort("评论内容不能为空")
                return@setOnClickListener
            }
            
            // Disable button to prevent multiple clicks
            binding.btnSubmit.isEnabled = false
            
            // Post comment
            viewModel.postComment(
                mediaId = mediaId,
                content = content,
                replyToUser = replyToUser,
                replyToComment = replyToComment
            ) { success ->
                // Re-enable button
                binding.btnSubmit.isEnabled = true
                
                if (success) {
                    // Clear input field and reset reply state
                    binding.etComment.setText("")
                    resetReplyState()
                    
                    // Hide keyboard
                    KeyboardUtils.hideSoftInput(binding.etComment)
                    
                    // Refresh comments to show the new one
                    viewModel.loadComments(mediaId, true)
                } else {
                    ToastUtils.showShort("评论发送失败，请重试")
                }
            }
        }
        
        // Set cancel reply button click listener
        binding.ivCancelReply.setOnClickListener {
            resetReplyState()
        }
        
        // Set close button click listener
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
    
    private fun resetReplyState() {
        replyToUser = null
        replyToComment = null
        replyToNickname = null
        
        binding.layoutReplyIndicator.visibility = View.GONE
        binding.etComment.hint = "发表评论..."
    }
    
    private fun setupObservers() {
        viewModel.commentsLiveData.observe(viewLifecycleOwner) { (isRefresh, comments) ->
            if (isRefresh) {
                commentAdapter.updateData(comments)
            } else {
                commentAdapter.addData(comments)
            }
            
            // Update empty view visibility
            binding.layoutEmpty.visibility = if (commentAdapter.itemCount == 0) View.VISIBLE else View.GONE
            binding.tvCommentCount.text = "评论 ${commentAdapter.itemCount}"
        }
        
        viewModel.commentRequestCancelLiveData.observe(viewLifecycleOwner) { isRefresh ->
            if (isRefresh) {
                binding.refreshLayout.finishRefresh()
            } else {
                binding.refreshLayout.finishLoadMore()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    inner class CommentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val items = mutableListOf<CommentItem>()
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        private val displayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        
        fun updateData(newItems: List<CommentItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        
        fun addData(newItems: List<CommentItem>) {
            val startPosition = items.size
            items.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CommentViewHolder).bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
            private val tvNickname: TextView = itemView.findViewById(R.id.tv_nickname)
            private val tvContent: TextView = itemView.findViewById(R.id.tv_content)
            private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
            private val tvReply: TextView = itemView.findViewById(R.id.tv_reply)
            private val rvReplies: RecyclerView = itemView.findViewById(R.id.rv_replies)
            
            fun bind(item: CommentItem) {
                // Load avatar
                Glide.with(requireContext())
                    .load(item.fromUserProfileURL)
                    .placeholder(R.mipmap.ic_default_avatar)
                    .error(R.mipmap.ic_default_avatar)
                    .circleCrop()
                    .into(ivAvatar)
                
                // Set nickname
                tvNickname.text = item.fromUserNickname
                
                // Set content with reply information if available
                if (item.replyToUserNickname.isNullOrEmpty()) {
                    tvContent.text = item.content
                } else {
                    tvContent.text = "回复 @${item.replyToUserNickname}: ${item.content}"
                }
                
                // Set time
                try {
                    val date = dateFormat.parse(item.createDate)
                    tvTime.text = date?.let { displayFormat.format(it) } ?: item.createDate
                } catch (e: Exception) {
                    tvTime.text = item.createDate
                }
                
                // Setup reply button
                tvReply.setOnClickListener {
                    replyToUser = item.fromUser
                    replyToComment = item.id
                    replyToNickname = item.fromUserNickname
                    
                    // Show reply indicator
                    binding.layoutReplyIndicator.visibility = View.VISIBLE
                    binding.tvReplyTo.text = "回复 @${replyToNickname}"
                    binding.etComment.hint = "回复 @${replyToNickname}..."
                    binding.etComment.requestFocus()
                    
                    // Show keyboard
                    KeyboardUtils.showSoftInput(binding.etComment)
                }
                
                // Setup replies if available
                if (!item.replies.isNullOrEmpty()) {
                    try {
                        val replies = item.parsedReplies
                        if (replies.isNotEmpty()) {
                            rvReplies.visibility = View.VISIBLE
                            rvReplies.layoutManager = LinearLayoutManager(requireContext())
                            rvReplies.adapter = ReplyAdapter(replies)
                        } else {
                            rvReplies.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        rvReplies.visibility = View.GONE
                    }
                } else {
                    rvReplies.visibility = View.GONE
                }
            }
        }
    }
    
    inner class ReplyAdapter(private val replies: List<CommentItem>) : 
        RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment_reply, parent, false)
            return ReplyViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
            holder.bind(replies[position])
        }
        
        override fun getItemCount(): Int = replies.size
        
        inner class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvReplyContent: TextView = itemView.findViewById(R.id.tv_reply_content)
            private val tvReplyTime: TextView = itemView.findViewById(R.id.tv_reply_time)
            
            fun bind(reply: CommentItem) {
                // Set content with user information
                val replyText = if (reply.replyToUserNickname.isNullOrEmpty()) {
                    "${reply.fromUserNickname}: ${reply.content}"
                } else {
                    "${reply.fromUserNickname} 回复 @${reply.replyToUserNickname}: ${reply.content}"
                }
                tvReplyContent.text = replyText
                
                // Set time
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    val date = dateFormat.parse(reply.createDate)
                    tvReplyTime.text = date?.let { displayFormat.format(it) } ?: reply.createDate
                } catch (e: Exception) {
                    tvReplyTime.text = reply.createDate
                }
                
                // Set click listener for replying to this reply
                itemView.setOnClickListener {
                    replyToUser = reply.fromUser
                    replyToComment = reply.id
                    replyToNickname = reply.fromUserNickname
                    
                    // Show reply indicator
                    binding.layoutReplyIndicator.visibility = View.VISIBLE
                    binding.tvReplyTo.text = "回复 @${replyToNickname}"
                    binding.etComment.hint = "回复 @${replyToNickname}..."
                    binding.etComment.requestFocus()
                    
                    // Show keyboard
                    KeyboardUtils.showSoftInput(binding.etComment)
                }
            }
        }
    }
} 