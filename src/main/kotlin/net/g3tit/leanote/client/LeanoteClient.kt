package net.g3tit.leanote.client

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
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
        val notebooks = leanoteService.listNotebook()
        logger.warn("list notebooks: $notebooks")

        notebooks.forEach { notebook ->
            val notebookPath = Paths.get(rootDirectory, notebook.title)
            FileUtils.forceMkdir(notebookPath.toFile())

            val notes = leanoteService.listNote(notebook.notebookId)
            logger.warn("notebookId: ${notebook.notebookId}, notes: $notes")
            notes.forEach { note ->
                val noteFilename = "${note.title}_${note.noteId}_${note.updateSequenceNum}.md"
                val notePath = Paths.get(notebookPath.toString(), noteFilename)

                val noteContent = leanoteService.getNote(note.noteId)
                FileUtils.writeStringToFile(notePath.toFile(), noteContent.content, UTF_8)
                logger.warn("save note: ${note.noteId}")
            }

        }

    }


}