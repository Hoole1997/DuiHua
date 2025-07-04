package com.duihua.chat.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ExploreListResult(
    val content: ArrayList<ExploreContent>
//    val page: Page
)

@Parcelize
data class ExploreContent(
    val coverURL: String = "",
    val createDate: String = "",
    val delete: Boolean = false,
    val fullScreen: Boolean = false,
    val id: Long = 0L,
    var isFavorite: Boolean = false,
    val lastModifiedDate: String = "",
    val mediaAuth: String = "",
    val mediaContent: String = "",
    val mediaLabel: String = "",
    var mediaSource: String = "",
    val mediaType: String = "",
    val nickName: String = "",
    val owner: Long = 0L,
    val profileURL: String = "",
    val remarkName: String = "",
    val resourceUrls: List<String> = emptyList(),
    val ruleName: String? = null,
    val status: String = "",
    val totalComment: Long = 0L,
    val totalDiamond: Long = 0L
) : Parcelable

//data class Page(
//    val pageNumber: Int,
//    val pageSize: Int,
//    val totalElements: Long,
//    val totalPages: Int
//)