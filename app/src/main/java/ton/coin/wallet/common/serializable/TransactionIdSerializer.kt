package ton.coin.wallet.common.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.bitstring.BitString
import org.ton.lite.client.internal.TransactionId

class TransactionIdSerializer : KSerializer<TransactionId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CustomTransactionId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TransactionId {
        val lt = decoder.decodeLong()
        val hash = decoder.decodeString()
        val arr = BooleanArray(hash.length)
        hash.forEachIndexed { index, c -> arr[index] = c == '1' }
        return TransactionId(BitString(arr.toList()), lt)
    }

    override fun serialize(encoder: Encoder, value: TransactionId) {
        encoder.encodeLong(value.lt)
        val hash = value.hash.toBooleanArray().joinToString(separator = "") { if (it) "1" else "0" }
        encoder.encodeString(hash)
    }
}