package net.g3tit.leanote.client

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
interface LeanoteApi {
    @GET("/api/notebook/getNotebooks")
    fun listNotebook(@Query("token") token: String): Call<List<LeanoteNotebook>>

    @GET("/api/note/getNotes")
    fun listNote(@Query("token") token: String, @Query("notebookId") notebookId: String): Call<List<LeanoteNote>>

    @GET("/api/note/getNoteAndContent")
    fun getNote(@Query("token") token: String, @Query("noteId") noteId: String): Call<LeanoteNote>

    @POST("/api/note/updateNote")
    fun updateNote(@Query("token") token: String, @Query("noteId") noteId: String)
}