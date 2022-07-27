package io.requestly.android.event.internal.data.repository

import androidx.lifecycle.LiveData
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.room.EventDatabase

internal class EventDatabaseRepository(
    private val database: EventDatabase
) : EventRepository {

    private val eventDao get() = database.eventDao()

    override fun getSortedEvents(): LiveData<List<Event>> {
        return eventDao.getSorted()
    }

    override suspend fun getAllEvents(): List<Event> {
        return eventDao.getAll()
    }

    override suspend fun insertEvent(event: Event) {
        val id = eventDao.insert(event)
        event.id = id ?: 0
    }

    override suspend fun updateEvent(event: Event): Int {
        return eventDao.update(event)
    }

    override suspend fun deleteAllEvents() {
        eventDao.deleteAll()
    }

    override suspend fun getEvent(id: Long): LiveData<Event?> {
        return eventDao.getById(id)
    }

    override suspend fun deleteOldEvents(threshold: Long) {
        eventDao.deleteBefore(threshold)
    }
}
