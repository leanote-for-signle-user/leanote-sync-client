package net.g3tit.leanote.client.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * LastSyncTime : 1552093397
 * LastSyncUsn : 200065
 */
/**
 * @author zhixiao.mzx
 * @date 2019/03/09
 */
data class LeanoteSyncState(
    @JsonProperty("LastSyncTime") var lastSyncTime: Long,
    @JsonProperty("LastSyncUsn") var lastSyncUsn: Long
)
