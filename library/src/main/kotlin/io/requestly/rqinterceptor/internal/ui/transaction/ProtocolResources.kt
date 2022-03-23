package io.requestly.rqinterceptor.internal.ui.transaction

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import io.requestly.rqinterceptor.R

internal sealed class ProtocolResources(@DrawableRes val icon: Int, @ColorRes val color: Int) {
    class Http : ProtocolResources(R.drawable.rq_interceptor_ic_http, R.color.rq_interceptor_color_error)
    class Https : ProtocolResources(R.drawable.rq_interceptor_ic_https, R.color.rq_interceptor_color_primary)
}
