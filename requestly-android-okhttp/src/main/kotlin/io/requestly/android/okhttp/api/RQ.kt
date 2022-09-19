package io.requestly.android.okhttp.api

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.internal.support.Logger
import io.requestly.android.okhttp.internal.support.NotificationHelper
import io.requestly.android.okhttp.internal.ui.MainActivity

/**
 * RQ-Interceptor methods and utilities to interact with the library.
 */
public object RQ {

    private const val SHORTCUT_ID = "rqInterceptorShortcutId"

    /**
     * Check if this instance is the operation one or no-op.
     * @return `true` if this is the operation instance.
     */
    @Suppress("MayBeConst ") // https://github.com/ChuckerTeam/chucker/pull/169#discussion_r362341353
    public val isOp: Boolean = true

    /**
     * Get an Intent to launch the RQ Interceptor UI directly.
     * @param context An Android [Context].
     * @return An Intent for the main RQ Interceptor Activity that can be started with [Context.startActivity].
     */
    @JvmStatic
    public fun getLaunchIntent(context: Context): Intent {
        return Intent(context, io.requestly.android.core.ui.MainRequestlyActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Create a shortcut to launch RQ Interceptor UI.
     * @param context An Android [Context].
     */
    internal fun createShortcut(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return
        }

        val shortcutManager = context.getSystemService<ShortcutManager>() ?: return
        if (shortcutManager.dynamicShortcuts.any { it.id == SHORTCUT_ID }) {
            return
        }

        val shortcut = ShortcutInfo.Builder(context, SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.rq_interceptor_shortcut_label))
            .setLongLabel(context.getString(R.string.rq_interceptor_shortcut_label))
            .setIcon(Icon.createWithResource(context, R.mipmap.rq_interceptor_ic_launcher))
            .setIntent(getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
            .build()
        try {
            shortcutManager.addDynamicShortcuts(listOf(shortcut))
        } catch (e: IllegalArgumentException) {
            Logger.warn("ShortcutManager addDynamicShortcuts failed ", e)
        } catch (e: IllegalStateException) {
            Logger.warn("ShortcutManager addDynamicShortcuts failed ", e)
        }
    }

    /**
     * Dismisses all previous RQ Interceptor notifications.
     */
    @JvmStatic
    public fun dismissNotifications(context: Context) {
        NotificationHelper(context).dismissNotifications()
    }

    internal var logger: Logger = object : Logger {
        val TAG = "RQInterceptor"

        override fun info(message: String, throwable: Throwable?) {
            Log.i(TAG, message, throwable)
        }

        override fun warn(message: String, throwable: Throwable?) {
            Log.w(TAG, message, throwable)
        }

        override fun error(message: String, throwable: Throwable?) {
            Log.e(TAG, message, throwable)
        }
    }
}
