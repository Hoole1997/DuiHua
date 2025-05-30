package com.duihua.chat.bean

data class MediaListResult(
    val content: ArrayList<UserMedia>,
    val page: MediaListPage
)

data class UserMedia(
    val coverURL: String,
    val createDate: String,
    val delete: Boolean,
    val fullScreen: Boolean,
    val id: Long,
    val isFavorite: Boolean,
    val lastModifiedDate: String,
    val mediaAuth: String,
    val mediaContent: String,
    val mediaLabel: String,
    val mediaSource: String,
    val mediaType: String,
    val nickName: String,
    val owner: Long,
    val profileURL: String,
    val remarkName: String,
    val resourceURLs: List<String>,
    val ruleName: String,
    val status: String,
    val totalComment: Long,
    val totalDiamond: Long
)

data class MediaListPage(
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int
)