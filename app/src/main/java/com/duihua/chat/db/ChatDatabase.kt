package com.duihua.chat.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blankj.utilcode.util.Utils
import com.duihua.chat.bean.OtherUserInfo
import com.duihua.chat.bean.IMMessage
import kotlin.jvm.java

@Database(
    entities = [
        OtherUserInfo::class,
        IMMessage::class
    ],
    version = 1,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 3, to = 4),
//        AutoMigration(from = 4, to = 5),
//        AutoMigration(from = 5, to = 6),
//    ]
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(Utils.getApp(), ChatDatabase::class.java, "chat_database")
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
