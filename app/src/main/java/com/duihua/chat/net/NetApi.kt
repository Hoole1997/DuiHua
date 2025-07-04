package com.duihua.chat.net

class NetApi {

    companion object {
        const val SP_USER_INFO = "sp_user_info"
        const val SP_USER_TOKEN = "sp_user_token"

        const val API_OSS_TOKEN = "http://139.224.213.222:24918/get_sts_token"

        const val API_HOST = "http://www.duihua.click:23520"
        //im登录地址
        const val API_IM_URL = "/session/im-url"
        //获取自己信息
        const val API_USER_BASIC = "/user/basic"
        //修改自己的资料
        const val API_USER_BASIC_EDIT = "/user/basic/edit"
        //验证码
        const val API_VERIFICATION_CODE = "/session/verification"
        //登录
        const val API_LOGIN = "/session/login"
        //搜索手机号
        const val API_SEARCH = "/user/find-by-phone"
        //查询别人详情
        const val API_OTHER_USER= "/user/other"
        //取消关注
        const val API_CANCEL_FOLLOW = "/relationship/follow/delete"
        //关注
        const val API_FOLLOW = "/relationship/follow"
        //搜索用户
        const val API_SEARCH_ACCOUNT = "/relationship/list"
        //用户的媒体列表
        const val API_OTHER_USER_MEDIA_LIST = "/media/list-other"
        //登录用户的媒体列表
        const val API_USER_MEDIA_LIST = "/media/list"
        //搜索关注、粉丝、好友、拉黑
        const val API_PATTERN_SEARCH = "/media/pattern-search"
        //拉黑/取消拉黑
        const val API_BLOCK_ACCOUNT = "/relationship/block"
        //更新用户备注
        const val API_UPDATE_REMARK = "/relationship/follow/update"
        //获取可用的标签列表
        const val API_DISCOVER_LABEL = "/media/label-list"
        //获取标签内容
        const val API_DISCOVER_DETAIL_LIST = "/media/explore/list"
        //实名认证
        const val API_IDENTITY = "/user/upload-identity"
        //收支明细
        const val API_WITHDRAW_LIST = "/user/withdraw-list"
        //提现
        const val API_DO_WITHDRAW = "/user/do-withdraw"
        //可充值列表
        const val API_RECHARGE_ITEM = "/recharge/items"
        //钻石流水明细
        const val API_DIAMOND_DETAIL_LIST = "/user/get-diamond-detail-list"
        //更改手机号
        const val API_CHANGE_PHONE = "/user/basic/edit_phone"
        //上传内容
        const val API_MEDIA_UPLOAD = "/media/single"
        //收藏内容
        const val API_FAVORITES_MEDIA = "/favorites/do-favorite"
        //获取收藏列表
        const val API_FAVORITES_LIST = "/favorites/favorite-list"
    }

}