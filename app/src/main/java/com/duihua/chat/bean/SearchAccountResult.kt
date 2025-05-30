package com.duihua.chat.bean

data class SearchAccountResult(
    val content: ArrayList<SearchAccount>,
    val page: MediaListPage
)

data class SearchAccount(
    val aliPayAccount: String,
    val alipayName: String,
    val birthYear: Int,
    val canSearchByPhone: Boolean,
    val canSeeFansList: String,
    val canSeeFansNumber: String,
    val city: String,
    val coverURL: String,
    val createDate: String,
    val currency: Int,
    val device: String,
    val diamond: Long,
    val id: Long,
    val identityBackImageURL: String,
    val identityFrontImageURL: String,
    val identityNumber: String,
    val introduction: String,
    val isAdminBanned: Boolean,
    val isWriteOff: Boolean,
    val lastLoginDate: String,
    val lastLoginIP: String,
    val lastModifiedDate: String,
    val lastSession: String,
    val name: String,
    val nickName: String?,
    val phone: String,
    val profileURL: String,
    val region: String,
    val sex: String,
    val vipLevel: Int
)

data class Page(
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int
)