package io.requestly.android.event.internal.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(tableName = "events")
class Event (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "name") var eventName: String,

    @TypeConverters(MapTypeConverter::class)
    @ColumnInfo(name = "data") var eventData: Map<String, String>,

    @ColumnInfo(name = "timestamp") var timestamp: Long? = System.currentTimeMillis(),

    @ColumnInfo(name = "status") var status: Boolean? = true,
) {

    val notificationText: String
        get() {
            return eventName
        }
}
