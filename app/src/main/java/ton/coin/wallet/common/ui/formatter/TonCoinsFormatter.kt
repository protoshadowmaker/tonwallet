package ton.coin.wallet.common.ui.formatter

import ton.coin.wallet.data.TonCoins
import java.math.BigDecimal
import java.math.RoundingMode

object TonCoinsFormatter {
    fun format(coins: TonCoins, scale: Int = -1): String {
        val result = coins.value.toBigDecimal().divide(BigDecimal.valueOf(1_000_000_000))
        return if (scale == -1) {
            result.stripTrailingZeros().toPlainString()
        } else {
            result.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }
    }
}