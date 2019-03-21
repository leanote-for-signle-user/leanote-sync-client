package net.g3tit.leanote.client.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.g3tit.leanote.client.api.LeanoteApi
import net.g3tit.leanote.client.api.LeanoteApi4Web
import net.g3tit.leanote.client.model.LeanoteLoginResult
import net.g3tit.leanote.client.model.LeanoteNote
import net.g3tit.leanote.client.model.LeanoteNotebook
import net.g3tit.leanote.client.model.LeanoteSyncState
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

    @Value("\${leanote.username}")
    private lateinit var leanoteUsername: String

    @Value("\${leanote.password}")
    private lateinit var leanotePassword: String

    @Value("\${http.log-level:NONE}")
    private lateinit var httpLogLevel: String

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var leanoteToken: String

    private lateinit var leanoteCookie: String

    private lateinit var leanoteApi: LeanoteApi

    private lateinit var leanoteApi4Web: LeanoteApi4Web

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

        leanoteApi4Web = Retrofit.Builder()
            .baseUrl(leanoteHost)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
            .create(LeanoteApi4Web::class.java)
    }

    fun login4ApiToken(): LeanoteLoginResult {
        val response = leanoteApi.login(leanoteUsername, leanotePassword).execute()
        val loginResult = response.body()!!
        leanoteToken = loginResult.token
        return loginResult
    }

    fun login4WebApiCookie() {
        val response = leanoteApi4Web.login(leanoteUsername, leanotePassword).execute()
        leanoteCookie = response.headers()["Set-Cookie"]!!
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
        val body = leanoteApi4Web.updateNoteOrContent(leanoteCookie, note.noteId, note.content)
            .execute()
            .body()!!
            .string()
        if ("true" != body) {
            throw RuntimeException("updateNoteOrContentByWebApi error, noteId: ${note.noteId}, result: $body")
        }

        return leanoteApi.getNote(leanoteToken, note.noteId)
            .execute()
            .body()!!
            .updateSequenceNum
    }
}