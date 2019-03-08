package net.g3tit.leanote.client

import com.alibaba.fastjson.JSONPath
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.annotation.PostConstruct

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
@Service
class LeanoteService {
    private val logger = LoggerFactory.getLogger(LeanoteNote::class.java)

    @Value("\${leanote.host:}")
    private lateinit var leanoteHost: String

    @Value("\${leanote.token}")
    private lateinit var leanoteToken: String

    @Value("\${http.log-level:NONE}")
    private lateinit var httpLogLevel: String

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
            .create(LeanoteApi::class.java)
    }

    fun syncState(): LeanoteSyncState {
        return leanoteApi.syncState(leanoteToken).execute().body()!!
    }

    fun listNotebook(): List<LeanoteNotebook> {
        return leanoteApi.listNotebook(leanoteToken).execute().body()!!
    }

    fun listNote(notebookId: String): List<LeanoteNote> {
        return leanoteApi.listNote(leanoteToken, notebookId).execute().body()!!
    }

    fun getNote(noteId: String): LeanoteNote {
        return leanoteApi.getNote(leanoteToken, noteId).execute().body()!!
    }

    fun updateNote(note: LeanoteNote): Long {
        val body = leanoteApi.updateNote(leanoteToken, note.noteId, note.updateSequenceNum, note.content)
            .execute()
            .body()!!

        return JSONPath.read(body.string(), "$.Usn").toString().toLong()
    }
}