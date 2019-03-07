package net.g3tit.leanote.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*
import javax.annotation.PostConstruct

@Service
class LeanoteService {
    private val logger = LoggerFactory.getLogger(LeanoteNote::class.java)

    @Value("\${leanote.host:}")
    private lateinit var leanoteHost: String

    @Value("\${leanote.token}")
    private lateinit var leanoteToken: String

    @Value("\${http.log-level:NONE}")
    private lateinit var httpLogLevel: String

    private lateinit var leanoteApi: LeanoteApi

    @PostConstruct
    private fun init() {
        var httpLogLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE
        try {
            httpLogLevel = HttpLoggingInterceptor.Level.valueOf(this.httpLogLevel)
        } catch (e: Exception) {
            logger.error("init http log level error, level: $httpLogLevel")
        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = httpLogLevel
        val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        leanoteApi = Retrofit.Builder()
            .baseUrl(leanoteHost)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(LeanoteApi::class.java)
    }

    fun listNotebook(token: String): List<LeanoteNotebook> {

        return Collections.emptyList()
    }

    fun listNoteNote(token: String, notebookId: String): List<LeanoteNote> {

        return Collections.emptyList()
    }

    fun getNote(token: String, noteId: String): LeanoteNote {

        return LeanoteNote()
    }

    fun updateNote(token: String, noteId: String) {

    }
}