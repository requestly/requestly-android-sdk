package io.requestly.android.event.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.requestly.android.core.internal.support.ListNotificationHelper
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.launch

class EventListViewModel : ViewModel() {
    val events: LiveData<List<Event>> = with(RepositoryProvider.event()) {
        getSortedEvents()
    }

    suspend fun getAllEvents(): List<Event> = RepositoryProvider.event().getAllEvents()

    fun clearTransactions() {
        viewModelScope.launch {
            RepositoryProvider.event().deleteAllEvents()
            ListNotificationHelper.clearBuffer()
        }
    }
}
