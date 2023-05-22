package ton.coin.wallet.util

import java.time.LocalDateTime

fun LocalDateTime.isSameDay(other: LocalDateTime): Boolean {
    return dayOfMonth == other.dayOfMonth && month == other.month && year == other.year
}