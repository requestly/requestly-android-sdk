package io.requestly.android.event.api

import io.requestly.android.core.Requestly
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.*
import java.lang.Exception

class RequestlyEvent {
    companion object {
        fun send(eventName: String, eventData: Map<String, Any>) {
            var customEvent = Event(
                eventName = eventName,
                eventData = stringifyEventDataValues(eventData),
                timestamp = System.currentTimeMillis()
            )

            // TODO: Insert into Repository

            CoroutineScope(Dispatchers.IO).launch {
                // TODO: Remove this hack. Hack to init Repository. Ideally it should get auto init
                try {
                    RepositoryProvider.event()
                } catch (err: Exception) {
                    RepositoryProvider.initialize((Requestly.getInstance()?.applicationContext!!))
                }
                RepositoryProvider.event().insertEvent(customEvent)
            }
        }

        private fun stringifyEventDataValues(eventData: Map<String, Any>): Map<String, String> {
            var updatedData = emptyMap<String, String>().toMutableMap()

            eventData.keys.map {
                key -> updatedData[key] = eventData[key].toString()
            }

            return updatedData
        }
    }
}
