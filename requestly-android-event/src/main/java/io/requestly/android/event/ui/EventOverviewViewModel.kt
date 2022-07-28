package io.requestly.android.event.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.launch

class EventOverviewViewModel(eventId: Long) : ViewModel() {
    lateinit var event: LiveData<Event?>

    init {
//        eventData = LiveData<Event>()
        Log.i("EventOverviewViewModel", "init $eventId")
        viewModelScope.launch {
            event = RepositoryProvider.event().getEvent(eventId)
            Log.i("EventOverviewViewModel", "Event Data Fetched $eventId")
        }
    }
}

internal class EventOverviewViewModelFactory(
    private val eventId: Long = 0L
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == EventOverviewViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return EventOverviewViewModel(eventId) as T
    }
}
