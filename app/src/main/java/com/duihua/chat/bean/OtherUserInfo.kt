package com.duihua.chat.bean

data class OtherUserInfo(
    val anonymity: Boolean,
    val city: String,
    val continueChatting: Boolean,
    val coverURL: String,
    val fansNumber: String,
    val id: Long,
    val introduction: String,
    val isFollow: Boolean,
    val nickName: String,
    val phone: String,
    val profileURL: String,
    val region: String,
    val remarkName: String,
    val remarkPhone: String,
    val reversedAnonymity: Boolean?,
    val singleSessionConfig: SingleSessionConfig
) {
    fun showNickName(): String {
        if (!remarkName.isNullOrBlank()) {
            return remarkName
        }
        return if (nickName.isNullOrBlank()) phone else nickName
    }

    fun showProfile() : String {
        return if (introduction.isNullOrBlank()) "暂未设置个人简介" else introduction
    }
}

data class SingleSessionConfig(
    val blockMessages: Boolean,
    val doNotDisturb: Boolean,
    val id: Long,
    val imageMessage: Boolean,
    val lastModifiedDate: String,
    val pinChatTop: Boolean,
    val setterUserID: Long,
    val shareLocation: Boolean,
    val targetUserID: Long,
    val videoCall: Boolean,
    val videoMessage: Boolean,
    val vipBeauty: Boolean,
    val voiceCall: Boolean
)