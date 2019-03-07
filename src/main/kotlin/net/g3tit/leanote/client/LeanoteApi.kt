package net.g3tit.leanote.client

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LeanoteApi {
    @GET("/api/notebook/getNotebooks")
    fun listNotebook(@Query("token") token: String): List<LeanoteNotebook>

    @GET("/api/note/getNotes")
    fun listNoteNote(@Query("token") token: String, @Query("notebookId") notebookId: String): List<LeanoteNote>

    @GET("/api/note/getNoteAndContent")
    fun getNote(@Query("token") token: String, @Query("noteId") noteId: String): LeanoteNote

    @POST("/api/note/updateNote")
    fun updateNote(@Query("token") token: String, @Query("noteId") noteId: String)
}