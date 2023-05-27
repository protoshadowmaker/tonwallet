package ton.coin.wallet.common.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ton.coin.wallet.data.CompletedTransaction

class DirectionSerializer : KSerializer<CompletedTransaction.Direction> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CompletedTransaction.Direction", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CompletedTransaction.Direction {
        return CompletedTransaction.Direction.valueOf(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: CompletedTransaction.Direction) {
        encoder.encodeString(value.toString())
    }
}