package net.g3tit.leanote.client.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
interface LeanoteApi4Web {
    @FormUrlEncoded
    @POST("/doLogin")
    fun login(
        @Field("email") username: String,
        @Field("pwd") password: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("/note/updateNoteOrContent")
    fun updateNoteOrContent(
        @Header("Cookie") cookie: String,
        @Field("NoteId") noteId: String,
        @Field("Content") content: String,
        @Field("ImgSrc") imgSrc: String = ""
    ): Call<ResponseBody>
}