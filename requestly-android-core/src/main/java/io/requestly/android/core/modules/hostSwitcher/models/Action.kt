package io.requestly.android.core.modules.hostSwitcher.models

import java.net.URL

enum class ActionName {
    REDIRECT
}
data class Action(
    val action: ActionName,
    val url: URL
)
