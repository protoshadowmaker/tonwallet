package ton.coin.wallet.common.ui

import android.content.Context
import android.content.res.Configuration
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

object Density {
    var density: Float = 1f

    fun updateDisplaySize(context: Context, newConfiguration: Configuration) {
        density = context.resources.displayMetrics.density
    }
}

fun Number.dp(): Int {
    return if (this == 0) {
        0
    } else {
        ceil(Density.density * this.toDouble()).toInt()
    }
}

fun Number.dpr(): Int {
    return if (this == 0) {
        0
    } else {
        round(Density.density * this.toDouble()).toInt()
    }
}

fun Number.dp2(): Int {
    return if (this == 0) {
        0
    } else {
        floor(Density.density * this.toDouble()).toInt()
    }
}