package net.g3tit.leanote.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.g3tit.leanote.client.model.LeanoteNote
import net.g3tit.leanote.client.model.LocalNoteInfo
import net.g3tit.leanote.client.service.LeanoteService
import net.g3tit.leanote.client.util.lastModifiedZonedDateTime
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.BooleanUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.annotation.PostConstruct
import kotlin.text.Charsets.UTF_8

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
@Component
class LeanoteClient {
    private val logger = LoggerFactory.getLogger(LeanoteClient::class.java)

    @Value("\${leanote.local.root-directory}")
    private lateinit var rootDirectory: String

    @Autowired
    private lateinit var leanoteService: LeanoteService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @PostConstruct
    private fun init() {
        leanoteService.login4ApiToken()
        leanoteService.login4WebApiCookie()

        FileUtils.forceMkdir(File(rootDirectory))

        val syncState = leanoteService.syncState()

        val localNoteInfoMap = loadLocalNoteInfoMap()

        val notebooks = leanoteService.listNotebook()
        logger.warn("list notebooks: $notebooks")

        notebooks.forEach { notebook ->
            val notebookPath = Paths.get(rootDirectory, notebook.title)
            FileUtils.forceMkdir(notebookPath.toFile())

            val notes = leanoteService.listNote(notebook.notebookId)
            logger.warn("notebookId: ${notebook.notebookId}, notes: $notes")
            notes.forEach { note -> handleNote(note, notebookPath, localNoteInfoMap) }
        }

        val localNoteInfoStr =
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(localNoteInfoMap.values)
        FileUtils.writeStringToFile(getLocalDataFile(), localNoteInfoStr, UTF_8.name())
        logger.warn("update local data file")
    }

    private fun handleNote(note: LeanoteNote, notebookPath: Path, localNoteInfoMap: MutableMap<String, LocalNoteInfo>) {
        val noteFilename = "${note.title}_${note.noteId}.md"
        val noteFile = Paths.get(notebookPath.toString(), noteFilename).toFile()

        if (localNoteInfoMap.containsKey(note.noteId) && noteFile.exists()) {
            val localNoteInfo = localNoteInfoMap[note.noteId]!!

            if (localNoteInfo.updateSequenceNum > note.updateSequenceNum) {
                throw RuntimeException("local usn should not be great that server")
            } else if (localNoteInfo.updateSequenceNum == note.updateSequenceNum ||
                localNoteInfo.updateSequenceNum < note.updateSequenceNum
            ) {
                checkLastUpdateTimeAndUpdateNote(localNoteInfoMap, note, noteFile)
            }
        } else {
            val localNoteInfo = syncNote2Local(note, noteFile)
            localNoteInfoMap[note.noteId] = localNoteInfo
        }
    }

    private fun checkLastUpdateTimeAndUpdateNote(
        localNoteInfoMap: MutableMap<String, LocalNoteInfo>,
        note: LeanoteNote,
        noteFile: File
    ) {
        val localNoteInfo = localNoteInfoMap[note.noteId]!!
        val lastLocalUpdatedTime = noteFile.lastModifiedZonedDateTime()

        if (lastLocalUpdatedTime == localNoteInfo.lastLocalUpdatedTime
            && note.updatedTime == localNoteInfo.lastServerUpdatedTime
        ) {
            logger.warn(
                "not need update note: ${note.noteId}, " +
                        "local usn: ${localNoteInfo.updateSequenceNum}, " +
                        "remote usn: ${note.updateSequenceNum}, " +
                        "file: $noteFile"
            )
            return
        } else if (lastLocalUpdatedTime == localNoteInfo.lastLocalUpdatedTime
            && note.updatedTime > localNoteInfo.lastServerUpdatedTime
        ) {
            // sync to local
            logger.warn("sync to local by updatedTime, noteId: ${note.noteId}")
            val newLocalNoteInfo = syncNote2Local(note, noteFile)
            localNoteInfoMap[note.noteId] = newLocalNoteInfo
        } else if (lastLocalUpdatedTime > localNoteInfo.lastServerUpdatedTime
            && note.updatedTime == localNoteInfo.lastServerUpdatedTime
        ) {
            // sync to server
            syncNote2Server(localNoteInfo, note, noteFile)
        } else if (lastLocalUpdatedTime > localNoteInfo.lastLocalUpdatedTime
            && note.updatedTime > localNoteInfo.lastServerUpdatedTime
        ) {
            // need merge two
            logger.error("need merge local and server, noteId: ${note.noteId}, file: $noteFile")
        } else {
            throw RuntimeException(
                "unknown case, noteId: ${note.noteId}, " +
                        "lastLocalUpdatedTime: $lastLocalUpdatedTime, localInfo: $localNoteInfo, remoteInfo: $note"
            )
        }
    }

    private fun syncNote2Server(localNoteInfo: LocalNoteInfo, note: LeanoteNote, noteFile: File) {
        note.content = FileUtils.readFileToString(noteFile, UTF_8.name())

        val newNoteInfo = leanoteService.updateNote(note)
        if (newNoteInfo.updateSequenceNum <= 0L) {
            logger.error("update note error, noteId: ${note.noteId}, filepath: ${noteFile.toString()}")
            return
        }

        localNoteInfo.updateSequenceNum = newNoteInfo.updateSequenceNum
        localNoteInfo.lastServerUpdatedTime = newNoteInfo.updatedTime
        localNoteInfo.lastLocalUpdatedTime = noteFile.lastModifiedZonedDateTime()

        logger.warn("update noteId: ${note.noteId}, old usn: ${note.updateSequenceNum}, new usn: ${newNoteInfo.updateSequenceNum}")
    }

    private fun syncNote2Local(note: LeanoteNote, noteFile: File): LocalNoteInfo {
        val noteDetail = leanoteService.getNote(note.noteId)
        val remoteContentMd5 = DigestUtils.md5DigestAsHex(noteDetail.content.toByteArray())
        val localNoteInfo = LocalNoteInfo(
            noteId = note.noteId,
            md5 = remoteContentMd5,
            title = note.title,
            updateSequenceNum = note.updateSequenceNum,
            lastServerUpdatedTime = note.updatedTime,
            lastLocalUpdatedTime = ZonedDateTime.now(ZoneOffset.UTC)
        )

        FileUtils.writeStringToFile(noteFile, noteDetail.content, UTF_8)
        localNoteInfo.lastLocalUpdatedTime = noteFile.lastModifiedZonedDateTime()
        logger.warn("save noteId: ${note.noteId}, title: ${note.title}, file: $noteFile")
        return localNoteInfo
    }

    private fun getLocalDataFile() = Paths.get(rootDirectory, "data.json").toFile()

    private fun loadLocalNoteInfoMap(): MutableMap<String, LocalNoteInfo> {
        if (BooleanUtils.isFalse(getLocalDataFile().exists())) {
            return mutableMapOf()
        }

        val localData = FileUtils.readFileToString(getLocalDataFile(), UTF_8.name())
        val localNoteInfos = objectMapper.readValue<List<LocalNoteInfo>>(localData)
        val localNoteInfoMap = localNoteInfos.map { it.noteId to it }.toMap()
        return localNoteInfoMap as MutableMap<String, LocalNoteInfo>
    }
}