package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.constant.MimeTypeEnum
import fr.spoutnik87.musicbot_rest.exception.InitialMimeTypeNotFoundException
import fr.spoutnik87.musicbot_rest.model.MimeType
import fr.spoutnik87.musicbot_rest.repository.MimeTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MimeTypeService {

    @Autowired
    private lateinit var mimeTypeRepository: MimeTypeRepository

    val allMimeTypes: List<MimeType>
        get() = mimeTypeRepository.findAll()

    val allInitialMimeTypes
        get() = MimeTypeEnum.values().map { findByValue(it) }

    val IMAGE_PNG
        get() = findByValue(MimeTypeEnum.IMAGE_PNG)

    val IMAGE_JPEG
        get() = findByValue(MimeTypeEnum.IMAGE_JPEG)

    val AUDIO_MPEG
        get() = findByValue(MimeTypeEnum.AUDIO_MPEG)

    @Throws(InitialMimeTypeNotFoundException::class)
    @Transactional(readOnly = true)
    fun findByValue(mimeType: MimeTypeEnum) = mimeTypeRepository.findByValue(mimeType.value)
            ?: throw InitialMimeTypeNotFoundException(mimeType.value)

    @Transactional(readOnly = true)
    fun findByValue(value: String) = mimeTypeRepository.findByValue(value)
}