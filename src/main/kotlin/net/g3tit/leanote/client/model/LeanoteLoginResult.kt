package net.g3tit.leanote.client.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * UserId : 5368c1aa99c37b029d000001
 * Username : admin
 * Email : admin@leanote.com
 * Verified : false
 * Logo : /images/blog/default_avatar.png
 */
/**
 * @author zhixiao.mzx
 * @date 2019/03/09
 */
data class LeanoteLoginResult(
    @JsonProperty("Ok") var ok: Boolean,
    @JsonProperty("UserId") var userId: String,
    @JsonProperty("Username") var username: String,
    @JsonProperty("Email") var email: String,
    @JsonProperty("Token") var token: String
)
