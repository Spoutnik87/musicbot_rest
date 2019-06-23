package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.exception.InitialContentTypeNotFoundException
import fr.spoutnik87.musicbot_rest.model.ContentType
import fr.spoutnik87.musicbot_rest.repository.ContentTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ContentTypeService {

    @Autowired
    private lateinit var contentTypeRepository: ContentTypeRepository

    val allContentTypes: List<ContentType>
        get() = contentTypeRepository.findAll()

    val allInitialContentTypes
        get() = ContentTypeEnum.values().map { getByValue(it) }


    @Throws(InitialContentTypeNotFoundException::class)
    fun getByValue(contentType: ContentTypeEnum) = contentTypeRepository.findByValue(contentType.value)
            ?: throw InitialContentTypeNotFoundException(contentType.value)


    fun getByValue(value: String) = contentTypeRepository.findByValue(value)

    val EMPTY
        get() = getByValue(ContentTypeEnum.EMPTY)

    val LOCAL
        get() = getByValue(ContentTypeEnum.LOCAL)

    val YOUTUBE
        get() = getByValue(ContentTypeEnum.YOUTUBE)
}