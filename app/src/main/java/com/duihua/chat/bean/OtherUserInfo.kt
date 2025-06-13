package com.duihua.chat.bean

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.jiguang.imui.commons.models.IUser

@Entity(tableName = "OtherUserInfo",indices = [Index(value = ["id"], unique = true)])
data class OtherUserInfo(
    var anonymity: Boolean,
    var city: String,
    var continueChatting: Boolean,
    var coverURL: String,
    var fansNumber: String,
    @PrimaryKey var id: Long,
    var introduction: String,
    var isFollow: Boolean,
    var nickName: String,
    var phone: String,
    var profileURL: String,
    var region: String,
    var remarkName: String,
    var remarkPhone: String,
    var reversedAnonymity: Boolean?,
    @Ignore
    var singleSessionConfig: SingleSessionConfig
) : IUser {
    constructor() : this(
        anonymity = false,
        city = "",
        continueChatting = false,
        coverURL = "",
        fansNumber = "",
        id = 0L,
        introduction = "",
        isFollow = false,
        nickName = "",
        phone = "",
        profileURL = "",
        region = "",
        remarkName = "",
        remarkPhone = "",
        reversedAnonymity = null,
        singleSessionConfig = SingleSessionConfig(
            blockMessages = false,
            doNotDisturb = false,
            id = 0L,
            imageMessage = false,
            lastModifiedDate = "",
            pinChatTop = false,
            setterUserID = 0L,
            shareLocation = false,
            targetUserID = 0L,
            videoCall = false,
            videoMessage = false,
            vipBeauty = false,
            voiceCall = false
        )
    )

    fun showNickName(): String {
        if (!remarkName.isNullOrBlank()) {
            return remarkName
        }
        return if (nickName.isNullOrBlank()) phone else nickName
    }

    fun showProfile() : String {
        return if (introduction.isNullOrBlank()) "暂未设置个人简介" else introduction
    }

    override fun getId(): String? {
        return id.toString()
    }

    override fun getDisplayName(): String? {
        return showNickName()
    }

    override fun getAvatarFilePath(): String? {
        return profileURL.ifEmpty { "https://placeholder.com/avatar.png" }
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