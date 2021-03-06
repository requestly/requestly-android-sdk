package io.requestly.android.event.api

import com.google.gson.Gson
import io.requestly.android.core.Requestly
import io.requestly.android.event.internal.Utils
import io.requestly.android.event.internal.data.entity.Event
import io.requestly.android.event.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class RequestlyEvent private constructor() {
    companion object {
        fun send(eventName: String, eventData: Map<String, Any>) {
            var customEvent = Event(
                eventName = eventName,
                eventData = stringifyEventDataValues(eventData),
                timestamp = System.currentTimeMillis()
            )

            // Insert into Repository
            CoroutineScope(Dispatchers.IO).launch {
                // @wrongsahil: Remove this hack. Hack to init Repository. Ideally it should get auto init
                @Suppress("TooGenericExceptionCaught", "SwallowedException")
                try {
                    RepositoryProvider.event()
                } catch (err: Exception) {
                    RepositoryProvider.initialize((Requestly.getInstance()?.applicationContext!!))
                }
                RepositoryProvider.event().insertEvent(customEvent)

                // Show Notification after inserted into Repository since `id` is generated afterwards
                Requestly.getInstance()?.listNotificationHelper?.show(
                    customEvent.notificationText,
                    customEvent.id,
                    Utils.getDefaultScreenIntent(Requestly.getInstance()?.applicationContext!!),
                    "Analytics Interceptor"
                )
            }
        }

        private fun stringifyEventDataValues(eventData: Map<String, Any>): Map<String, String> {
            var updatedData = emptyMap<String, String>().toMutableMap()

            eventData.keys.map {
                key ->
                if (eventData[key] is Map<*, *>) {
                    updatedData[key] = Gson().toJson(eventData[key])
                } else {
                    updatedData[key] = eventData[key].toString()
                }
            }

            return updatedData
        }
    }
}
