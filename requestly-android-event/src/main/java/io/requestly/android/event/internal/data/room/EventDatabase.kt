package io.requestly.android.event.internal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.entity.MapTypeConverter

@Database(entities = [Event::class], version = 1, exportSchema = false)
@TypeConverters(MapTypeConverter::class)
internal abstract class EventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        private const val DB_NAME = "requestly_event.db"

        fun create(applicationContext: Context): EventDatabase {
            return Room.databaseBuilder(applicationContext, EventDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
