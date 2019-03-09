package net.g3tit.leanote.client.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * NotebookId : 557eab5705fcd14d95000002
 * UserId : 5368c1aa99c37b029d000001
 * ParentNotebookId :
 * Seq : 0
 * Title : Work
 * UrlTitle : Work
 * IsBlog : false
 * CreatedTime : 2015-06-15T10:39:21.399Z
 * UpdatedTime : 2015-06-15T10:39:21.399Z
 * Usn : 200023
 * IsDeleted : false
 */
/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
data class LeanoteNotebook(
    @JsonProperty("NotebookId") var notebookId: String,
    @JsonProperty("Seq") var seq: Int,
    @JsonProperty("Title") var title: String,
    @JsonProperty("Usn") var updateSequenceNum: Int
)
