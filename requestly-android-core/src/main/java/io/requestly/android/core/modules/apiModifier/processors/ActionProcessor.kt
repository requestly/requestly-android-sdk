package io.requestly.android.core.modules.apiModifier.processors

import io.requestly.android.core.modules.apiModifier.processors.models.Action
import io.requestly.android.core.modules.apiModifier.processors.models.ActionName
import okhttp3.Request

object ActionProcessor {

    fun process(actions: List<Action>, request: Request): Request {
        if (actions.isEmpty()) {
            return request
        }

        val firstAction = actions.first()

        return when (firstAction.action) {
            ActionName.REDIRECT -> {
                request.newBuilder().url(firstAction.url).build()
            }
        }
    }
}
