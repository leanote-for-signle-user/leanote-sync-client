package net.g3tit.leanote.client.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent
import java.time.ZonedDateTime

/**
 * @author zhixiao.mzx
 * @date 2018/11/05
 */
@JsonComponent
class JacksonZonedDateTimeSerializer : JsonSerializer<ZonedDateTime>() {
    override fun serialize(value: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value.toString())
    }
}