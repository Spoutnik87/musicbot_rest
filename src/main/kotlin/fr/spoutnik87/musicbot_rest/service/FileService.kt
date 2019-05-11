package fr.spoutnik87.musicbot_rest.service

import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class FileService {

    fun getFile(path: String): File = File(path)

    fun existsFile(path: String) = Files.exists(Paths.get(path))

    fun saveFile(path: String, content: ByteArray) = Files.write(Paths.get(path), content)

    fun deleteFile(path: String) = Files.deleteIfExists(Paths.get(path))
}