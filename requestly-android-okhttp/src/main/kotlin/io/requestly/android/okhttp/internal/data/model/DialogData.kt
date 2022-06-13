package io.requestly.android.okhttp.internal.data.model

internal data class DialogData(
    val title: String,
    val message: String,
    val positiveButtonText: String?,
    val negativeButtonText: String?
)
