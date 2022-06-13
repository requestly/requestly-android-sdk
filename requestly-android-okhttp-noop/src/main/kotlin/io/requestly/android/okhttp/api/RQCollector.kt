package io.requestly.android.okhttp.api

import android.content.Context

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember")
public class RQCollector @JvmOverloads constructor(
    context: Context,
    private val sdkKey: String? = null,
    private var uniqueDeviceId: String? = null,
    public var showNotification: Boolean = true,
    retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
)
