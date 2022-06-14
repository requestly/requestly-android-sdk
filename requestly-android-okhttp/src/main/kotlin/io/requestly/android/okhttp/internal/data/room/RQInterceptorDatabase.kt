package io.requestly.android.okhttp.internal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction

@Database(entities = [HttpTransaction::class], version = 7, exportSchema = false)
internal abstract class RQInterceptorDatabase : RoomDatabase() {

    abstract fun transactionDao(): HttpTransactionDao

    companion object {
        private const val DB_NAME = "rq_interceptor.db"

        fun create(applicationContext: Context): RQInterceptorDatabase {
            return Room.databaseBuilder(applicationContext, RQInterceptorDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
