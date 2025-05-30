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
    val beautyData: String,
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
}
