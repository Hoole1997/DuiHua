package com.duihua.chat.bean

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.jiguang.imui.commons.models.IMessage
import cn.jiguang.imui.commons.models.IUser
import com.blankj.utilcode.util.TimeUtils
import com.duihua.chat.net.UserManager
import java.util.HashMap
import java.util.UUID

@Entity(tableName = "im_message",indices = [Index(value = ["messageId"], unique = true)])
data class IMMessage(
    @PrimaryKey var messageId: String,
    var type: Int,
    var roomId: String?,
    var fromUserId: Long,
    var fromUserName: String,
    var toUserId: Long,
    var toUserName: String,
    var content: String,
    var fileSize: String,
    var duration: String,
    var location_x: Double,
    var location_y: Double,
    var timestamp: Long,
    var sent: Boolean,
    var version: String?
) : IMessage{
    companion object {
        /**
         * 生成UUID格式的messageId
         * 格式示例：1a4d5c05-b063-473c-ac45-07e48a3234a8
         */
        fun generateMessageId(): String {
            return UUID.randomUUID().toString()
        }
    }

    @Ignore
    var fromUser: OtherUserInfo? = null
    @Ignore
    var toUser: OtherUserInfo? = null

    // 公共构造函数
    constructor() : this(
        messageId = "",
        type = 0,
        roomId = null,
        fromUserId = 0L,
        fromUserName = "",
        toUserId = 0L,
        toUserName = "",
        content = "",
        fileSize = "",
        duration = "",
        location_x = 0.0,
        location_y = 0.0,
        timestamp = 0L,
        sent = false,
        version = null
    )

    override fun getMsgId(): String? {
        return messageId
    }

    override fun getFromUser(): IUser? {
        return if (duration == "0") {  // 接收的消息，显示发送者信息
            fromUser
        } else {  // 发送的消息，显示接收者信息
            toUser
        }
    }

    override fun getTimeString(): String? {
        // 处理timestamp最后6位为000
        val fixedTimestamp = if (timestamp > 1000000) {
            val tsStr = timestamp.toString()
            val newTsStr = tsStr.substring(0, tsStr.length - 6) + "000"
            newTsStr.toLongOrNull() ?: timestamp
        } else {
            timestamp
        }
        return TimeUtils.getFriendlyTimeSpanByNow(fixedTimestamp)
    }

    override fun getMessageType(): Int {
        if (type == 1) {
            //文字
            return if (duration == "0") IMessage.MessageType.RECEIVE_TEXT.ordinal else IMessage.MessageType.SEND_TEXT.ordinal
        }
        return 0
    }

    override fun getMessageStatus(): IMessage.MessageStatus? {
        if (duration == "0") {
            return IMessage.MessageStatus.RECEIVE_SUCCEED
        } else if (duration == "1") {
            return IMessage.MessageStatus.SEND_SUCCEED
        }
        return null
    }

    override fun getText(): String? {
        return content
    }

    override fun getMediaFilePath(): String? {
        return null
    }

    override fun getDuration(): Long {
        return duration.toLong()
    }

    override fun getProgress(): String? {
        return null
    }

    override fun getExtras(): HashMap<String?, String?>? {
        return null
    }
}
