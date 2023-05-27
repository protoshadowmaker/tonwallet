package ton.coin.wallet.common.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(decoder.decodeLong()),
            ZoneOffset.systemDefault()
        )
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeLong(value.atZone(ZoneOffset.systemDefault()).toInstant().epochSecond)
    }
}