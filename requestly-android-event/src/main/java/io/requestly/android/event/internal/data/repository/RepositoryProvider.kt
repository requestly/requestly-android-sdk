package io.requestly.android.event.internal.data.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.requestly.android.event.internal.data.room.EventDatabase

internal object RepositoryProvider {

    private var eventRepository: EventRepository? = null

    fun event(): EventRepository {
        return checkNotNull(eventRepository) {
            "You can't access the event repository if you don't initialize it!"
        }
    }

    /**
     * Idempotent method. Must be called before accessing the repositories.
     */
    fun initialize(applicationContext: Context) {
        if (eventRepository == null) {
            val db = EventDatabase.create(applicationContext)
            eventRepository = EventDatabaseRepository(db)
        }
    }

    /**
     * Cleanup stored singleton objects
     */
    @VisibleForTesting
    fun close() {
        eventRepository = null
    }
}

