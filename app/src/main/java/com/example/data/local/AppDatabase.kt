package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.LikeDao
import com.example.data.local.dao.MediaAssetDao
import com.example.data.local.dao.PersonaDao
import com.example.data.local.dao.PostDao
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.CommentLikeEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MediaAssetEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.PostLikeEntity
import com.example.data.local.entity.PostMediaEntity

@Database(
    entities = [
        PersonaEntity::class,
        PostEntity::class,
        PostMediaEntity::class,
        CommentEntity::class,
        PostLikeEntity::class,
        CommentLikeEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MediaAssetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun conversationDao(): ConversationDao
    abstract fun mediaAssetDao(): MediaAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personanet.db"
                )
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
