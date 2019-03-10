package net.g3tit.leanote.client.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.springframework.boot.jackson.JsonComponent
import java.time.ZonedDateTime

/**
 * @author zhixiao.mzx
 * @date 2019/03/09
 */
@JsonComponent
class JacksonZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ZonedDateTime {
        val str = jsonParser.readValueAs(String::class.java)
        return ZonedDateTime.parse(str)
    }
}