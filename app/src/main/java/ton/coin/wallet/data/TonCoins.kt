package ton.coin.wallet.data

import kotlinx.serialization.Serializable
import ton.coin.wallet.common.serializable.BigIntegerSerializer
import java.math.BigInteger

@Serializable
data class TonCoins(
    @Serializable(BigIntegerSerializer::class)
    val value: BigInteger = BigInteger.valueOf(0L)
)