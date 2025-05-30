package com.duihua.chat.bean

data class ExploreListResult(
    val content: ArrayList<ExploreContent>
//    val page: Page
)

data class ExploreContent(
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
    var mediaSource: String,
    val mediaType: String,
    val nickName: String,
    val owner: Long,
    val profileURL: String,
    val remarkName: String,
    val resourceUrls: List<String>,
    val ruleName: String,
    val status: String,
    val totalComment: Long,
    val totalDiamond: Long
)

//data class Page(
//    val pageNumber: Int,
//    val pageSize: Int,
//    val totalElements: Long,
//    val totalPages: Int
//)