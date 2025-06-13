package com.duihua.chat.bean

data class UserInfo(
    val id: Long,
    val phone: String,
    val currency: Int,
    val introduction: String?,
    val diamond: Long,
    val coverURL: String,
    val profileURL: String,
    val name: String?,
    val identityNumber: String?,
    val identityFrontImageURL: String?,
    val identityBackImageURL: String?,
    val nickName: String?,
    val sex: String,  // 可以考虑使用枚举类
    val canSearchByPhone: Boolean,
    val canSeeFansList: String,  // 可以考虑使用枚举类
    val canSeeFansNumber: String,  // 可以考虑使用枚举类
    val vipLevel: Int,
    val createDate: String,  // 可以考虑使用 Date 或 LocalDateTime
    val lastModifiedDate: String,  // 可以考虑使用 Date 或 LocalDateTime
    val lastLoginIP: String,
    val region: String,
    val city: String,
    val fansNumber: Long,
    val beautyData: String?,
    val aliPayAccount: String?,
    val aliPayName: String?
) {
    fun showNickName(): String {
        if (nickName != null) {
            return nickName
        }
        return if (name.isNullOrBlank()) phone else name
    }

    fun showProfile(): String {
        return if (introduction.isNullOrBlank()) "暂未设置个人简介" else introduction
    }

    /**
     * 将UserInfo转换为OtherUserInfo
     * @return OtherUserInfo对象
     */
    fun toOtherUserInfo(): OtherUserInfo {
        return OtherUserInfo().apply {
            id = this@UserInfo.id
            nickName = this@UserInfo.nickName ?: ""
            phone = this@UserInfo.phone
            profileURL = this@UserInfo.profileURL.ifEmpty { "https://placeholder.com/avatar.png" }  // 设置默认头像
            introduction = this@UserInfo.introduction ?: ""
            city = this@UserInfo.city?.takeIf { it.isNotEmpty() } ?: "未知"
            region = this@UserInfo.region?.takeIf { it.isNotEmpty() } ?: "未知"
            fansNumber = this@UserInfo.fansNumber.toString()
            coverURL = this@UserInfo.coverURL ?: ""
            // 其他字段使用默认值
            anonymity = false
            continueChatting = false
            isFollow = false
            reversedAnonymity = null
            remarkName = this@UserInfo.nickName ?: ""
            remarkPhone = this@UserInfo.phone
            singleSessionConfig = SingleSessionConfig(
                blockMessages = false,
                doNotDisturb = false,
                id = 0L,
                imageMessage = false,
                lastModifiedDate = this@UserInfo.lastModifiedDate ?: "",
                pinChatTop = false,
                setterUserID = 0L,
                shareLocation = false,
                targetUserID = 0L,
                videoCall = false,
                videoMessage = false,
                vipBeauty = false,
                voiceCall = false
            )
        }
    }
}
