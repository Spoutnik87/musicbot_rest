package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.service.ContentTypeService
import fr.spoutnik87.musicbot_rest.viewmodel.ContentTypeViewModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("content-type")
class ContentTypeController {

    @Autowired
    private lateinit var contentTypeService: ContentTypeService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("")
    fun getAll(): ResponseEntity<Any> {
        return ResponseEntity(contentTypeService.allInitialContentTypes.map { ContentTypeViewModel.from(it) }, HttpStatus.OK)
    }
}