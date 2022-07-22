package io.requestly.android.event.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.repository.RepositoryProvider

class EventListViewModel: ViewModel() {
    val events: LiveData<List<Event>> = with(RepositoryProvider.event()) {
       getSortedEvents()
    }

    suspend fun getAllEvents(): List<Event> = RepositoryProvider.event().getAllEvents()

    suspend fun clearTransactions() {
        RepositoryProvider.event().deleteAllEvents()
//        NotificationHelper.clearBuffer()
    }
}
