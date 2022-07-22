package io.requestly.android.event.internal.data.repository

import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.requestly.android.event.internal.data.entity.Event

internal interface EventRepository {
    fun getSortedEvents(): LiveData<List<Event>>

    suspend fun getAllEvents(): List<Event>

    suspend fun insertEvent(event: Event)

    suspend fun updateEvent(event: Event): Int

    suspend fun deleteAllEvents()

    suspend fun getEvent(id: Long): LiveData<Event?>

    suspend fun deleteOldEvents(threshold: Long)
}
