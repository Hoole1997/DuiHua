package com.duihua.chat.bean

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentItem(
    val id: Long = 0,
    val mediaID: Long = 0,
    val fromUser: Long = 0,
    val fromUserNickname: String = "",
    val fromUserProfileURL: String = "",
    val replyToUser: Long? = null,
    val replyToUserNickname: String? = null,
    val replyToUserProfileURL: String? = null,
    val replyToComment: Long? = null,
    val content: String = "",
    val replies: String? = null,
    val createDate: String = "",
    val lastModifiedDate: String = ""
) : Parcelable {
    // Parse replies from JSON string
    val parsedReplies: List<CommentItem>
        get() {
            if (replies.isNullOrEmpty()) return emptyList()
            
            return try {
                val typeToken = object : TypeToken<List<CommentItem>>() {}.type
                Gson().fromJson(replies, typeToken)
            } catch (e: Exception) {
                emptyList()
            }
        }
}

data class CommentResponse(
    val message: String,
    val code: String,
    val body: CommentBody
)

data class CommentBody(
    val page: PageInfo,
    val content: List<CommentItem>
)

data class PageInfo(
    val totalPages: Int,
    val totalElements: Long,
    val pageNumber: Int,
    val pageSize: Int
) 