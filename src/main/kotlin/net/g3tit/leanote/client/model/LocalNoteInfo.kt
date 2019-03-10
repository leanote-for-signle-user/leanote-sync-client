package net.g3tit.leanote.client.model

import java.time.ZonedDateTime

/**
 * @author zhixiao.mzx
 * @date 2019/03/05
 */
data class LocalData(
    var lastSyncState: LeanoteSyncState,
    var notes: List<LocalNoteInfo>
)

data class LocalNoteInfo(
    var title: String,
    var noteId: String,
    var md5: String,
    var updateSequenceNum: Long,
    var lastServerUpdatedTime: ZonedDateTime,
    var lastLocalUpdatedTime: ZonedDateTime
)
