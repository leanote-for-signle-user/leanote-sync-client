package net.g3tit.leanote.client.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
@Component
class BeanConfig {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jacksonModuleMap = applicationContext.getBeansOfType(Module::class.java)
        jacksonModuleMap.values.forEach { objectMapper.registerModule(it) }
//
//        objectMapper.registerModule(ParanamerModule())

        return objectMapper
    }
}