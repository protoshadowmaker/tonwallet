package ton.coin.wallet.data

import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.block.AddrStd
import org.ton.block.Either
import org.ton.block.IntMsgInfo
import org.ton.block.Maybe
import org.ton.block.Message
import org.ton.cell.Cell
import org.ton.cell.CellSlice
import org.ton.lite.client.internal.TransactionId
import org.ton.lite.client.internal.TransactionInfo
import org.ton.tlb.CellRef
import ton.coin.wallet.common.serializable.DirectionSerializer
import ton.coin.wallet.common.serializable.LocalDateTimeSerializer
import ton.coin.wallet.common.serializable.TransactionIdSerializer
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Serializable
data class CompletedTransaction(
    @Serializable(TransactionIdSerializer::class)
    val id: TransactionId,
    val address: String,
    val amount: TonCoins,
    val fee: TonCoins,
    @Serializable(DirectionSerializer::class)
    val direction: Direction,
    @Serializable(LocalDateTimeSerializer::class)
    val date: LocalDateTime,
    val message: String
) {
    enum class Direction {
        IN, OUT
    }
}

object CompletedTransactionMapUtil {
    fun toCompletedTransaction(transactionInfo: TransactionInfo): CompletedTransaction {
        val transaction = transactionInfo.transaction.value
        var amount = BigInteger.ZERO
        var fee = transaction.totalFees.coins.amount.value
        var direction = CompletedTransaction.Direction.IN
        val inInfo = transaction.r1.value.inMsg.value?.value?.info
        var address = ""
        if (inInfo is IntMsgInfo) {
            val src = inInfo.src
            if (src is AddrStd) {
                address = src.toString(userFriendly = true)
            }
            val inAmount = inInfo.value.coins.amount.value
            if (inAmount > BigInteger.ZERO) {
                direction = CompletedTransaction.Direction.IN
            }
            amount += inAmount
            fee += inInfo.fwd_fee.amount.value
        }
        transaction.r1.value.outMsgs.forEach { outMsg ->
            when (val outInfo = outMsg.second.value.info) {
                is IntMsgInfo -> {
                    val dest = outInfo.dest
                    if (dest is AddrStd) {
                        address = dest.toString(userFriendly = true)
                    }
                    val outAmount = outInfo.value.coins.amount.value
                    if (outAmount > BigInteger.ZERO) {
                        direction = CompletedTransaction.Direction.OUT
                    }
                    amount += outAmount
                    fee += outInfo.fwd_fee.amount.value
                }

                else -> {}
            }
        }
        val inMsg = mapMsgIn(Maybe.of(transaction.r1.value.inMsg.value?.value))
        val outMsg =
            transaction.r1.value.outMsgs.toMap().entries.firstNotNullOfOrNull { mapMsgOut(it.value.value) }
        val comment = inMsg ?: outMsg ?: ""

        return CompletedTransaction(
            transactionInfo.id,
            address,
            TonCoins(amount),
            TonCoins(fee),
            direction,
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(transaction.now.toLong()),
                ZoneOffset.systemDefault()
            ),
            comment
        )
    }

    private fun mapMsg(body: Either<Cell, Cell>?): String? {
        val bodyValue = body?.toPair()?.toList()?.firstOrNull { it != null && !it.isEmpty() }
        var comment: String? = null

        bodyValue?.parse {
            if (this.bits.size >= 32) {
                val tag = loadUInt(32).toLong()
                if (tag == 0xd53276db || tag == 0L) {
                    comment = String(loadRemainingBits().toByteArray())
                }
            }
        }

        return comment
    }

    private fun <X> Either<X, CellRef<X>>?.trimCellRef(): Either<X, X>? {
        if (this == null) return null
        return Either.of(this.x, this.y?.value)
    }

    private fun mapMsgIn(m: Maybe<Message<Cell>>) = mapMsg(
        m.value?.body?.trimCellRef()
    )

    private fun mapMsgOut(m: Message<Cell>) = mapMsg(
        m.body.trimCellRef()
    )

    private fun CellSlice.loadRemainingBits(): BitString {
        return BitString((this.bitsPosition until this.bits.size).map { this.loadBit() })
    }
}