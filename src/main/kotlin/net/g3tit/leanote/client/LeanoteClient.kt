package net.g3tit.leanote.client

import com.alibaba.fastjson.JSON
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.BooleanUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import java.io.File
import java.io.FileInputStream
import java.lang.RuntimeException
import java.nio.file.Paths
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

    @PostConstruct
    private fun init() {
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
            notes.forEach { note ->
                val noteDetail = leanoteService.getNote(note.noteId)
                val remoteContentMd5 = DigestUtils.md5DigestAsHex(noteDetail.content.toByteArray())
                val noteFilename = "${note.title}_${note.noteId}.md"
                val noteFile = Paths.get(notebookPath.toString(), noteFilename).toFile()

                if (localNoteInfoMap.containsKey(note.noteId) && noteFile.exists()) {
                    val localNoteInfo = localNoteInfoMap[note.noteId]!!
                    val localContentMd5 = DigestUtils.md5DigestAsHex(FileInputStream(noteFile))
                    if (remoteContentMd5 == localContentMd5) {
                        localNoteInfo.updateSequenceNum = note.updateSequenceNum
                        logger.warn(
                            "not need update note: ${note.noteId}, " +
                                    "local usn: ${localNoteInfo.updateSequenceNum}, remote usn: ${note.updateSequenceNum}"
                        )
                        return@forEach
                    }

                    if (localNoteInfo.updateSequenceNum == note.updateSequenceNum) {
                        val localNoteContent = FileUtils.readFileToString(noteFile, UTF_8.name())
                        note.content = localNoteContent
//                        note.updateSequenceNum = syncState.lastSyncUsn + 1
                        val updateSequenceNum = leanoteService.updateNote(note)
                        if (0L == updateSequenceNum) {
                            logger.error("update note error, noteId: ${note.noteId}, filepath: ${noteFile.toString()}")
                            return@forEach
                        }

//                        localNoteInfo.updateSequenceNum = updateSequenceNum
                        logger.warn(
                            "update noteId: ${note.noteId}, old usn: ${note.updateSequenceNum}, " +
                                    "new usn: ${localNoteInfo.updateSequenceNum}"
                        )
                    } else if (localNoteInfo.updateSequenceNum < note.updateSequenceNum) {
                        logger.error("please sync from server, filepath: $noteFile")
                    } else {
                        throw RuntimeException("local usn should not be great that server")
                    }
                } else {
                    val localNoteInfo = LocalNoteInfo(
                        noteId = note.noteId,
                        md5 = remoteContentMd5,
                        title = note.title,
                        updateSequenceNum = note.updateSequenceNum
                    )
                    localNoteInfoMap[note.noteId] = localNoteInfo

                    FileUtils.writeStringToFile(noteFile, noteDetail.content, UTF_8)
                    logger.warn("save note: ${note.noteId}")
                }
            }
        }

        FileUtils.writeStringToFile(getLocalDataFile(), JSON.toJSONString(localNoteInfoMap.values), UTF_8.name())
        logger.warn("update local data file")
    }

    private fun getLocalDataFile() = Paths.get(rootDirectory, "data.json").toFile()

    private fun loadLocalNoteInfoMap(): MutableMap<String, LocalNoteInfo> {
        if (BooleanUtils.isFalse(getLocalDataFile().exists())) {
            return mutableMapOf()
        }

        val localData = FileUtils.readFileToString(getLocalDataFile(), UTF_8.name())
        val localNoteInfos = JSON.parseArray(localData, LocalNoteInfo::class.java)
        val localNoteInfoMap = localNoteInfos.map { it.noteId to it }.toMap()
        return localNoteInfoMap as MutableMap<String, LocalNoteInfo>
    }
}