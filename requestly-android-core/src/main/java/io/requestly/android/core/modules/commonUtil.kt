package io.requestly.android.core.modules

import android.app.AlertDialog
import android.content.Context

internal fun loadSimpleYesNoAlertDialog(
    context: Context,
    message: String,
    onPositiveButtonClick: () -> Unit,
    onNegativeButtonClick: (() -> Unit)? = null,
) {
    AlertDialog.Builder(context)
        .setCancelable(false)
        .setTitle(message)
        .setPositiveButton("Yes") { dialog, _ ->
            onPositiveButtonClick()
            dialog.cancel()
        }
        .setNegativeButton("No") { dialog, _ ->
            onNegativeButtonClick?.let { it() }
            dialog.cancel()
        }
        .create()
        .show()
}
