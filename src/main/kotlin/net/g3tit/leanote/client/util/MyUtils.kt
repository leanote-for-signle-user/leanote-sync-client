package net.g3tit.leanote.client.util

import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime


fun File.lastModifiedZonedDateTime() =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.lastModified()), ZoneOffset.UTC)!!
