package net.g3tit.leanote.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
@Component
class BeanConfig {
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

//        val jacksonModuleMap = applicationContext.getBeansOfType(Module::class.java)
//        jacksonModuleMap?.values?.forEach { objectMapper.registerModule(it) }
//
//        objectMapper.registerModule(ParanamerModule())

        return objectMapper
    }
}