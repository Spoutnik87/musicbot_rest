package fr.spoutnik87.musicbot_rest.service

import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.XMPDM
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.mp3.Mp3Parser
import org.apache.tika.sax.BodyContentHandler
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

@Service
class FileService {

    fun getFile(path: String): File = File(path)

    fun existsFile(path: String) = Files.exists(Paths.get(path))

    fun saveFile(path: String, content: ByteArray) = Files.write(Paths.get(path), content)

    fun deleteFile(path: String) = Files.deleteIfExists(Paths.get(path))

    /**
     * Using Apache Tika to get file mime type.
     */
    fun getMimeType(stream: InputStream): String? {
        return try {
            val config = TikaConfig.getDefaultConfig()
            config.mimeRepository.detect(stream, Metadata()).toString()
        } catch (e: Exception) {
            null
        }
    }

    fun isPNG(mimeType: String?) = mimeType == "image/png"

    fun isJPG(mimeType: String?) = mimeType == "image/jpeg"

    fun isMP3(mimeType: String?) = mimeType == "audio/mpeg"

    fun isPNG(stream: InputStream) = isPNG(getMimeType(stream))

    fun isJPG(stream: InputStream) = isJPG(getMimeType(stream))

    fun isMP3(stream: InputStream) = isMP3(getMimeType(stream))

    fun isImage(stream: InputStream): Boolean {
        val type = getMimeType(stream)
        return isPNG(type) || isJPG(type)
    }

    fun isAudio(stream: InputStream) = isMP3(stream)

    fun getAudioFileDuration(stream: InputStream): Long? {
        return try {
            val handler = BodyContentHandler()
            val metadata = Metadata()
            val parseContext = ParseContext()
            Mp3Parser().parse(stream, handler, metadata, parseContext)
            Math.floor(metadata.get(XMPDM.DURATION).toDouble()).toLong()
        } catch (e: Exception) {
            null
        }
    }
}