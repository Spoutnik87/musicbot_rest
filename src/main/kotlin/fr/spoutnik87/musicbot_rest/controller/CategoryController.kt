package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.CategoryCreateReader
import fr.spoutnik87.musicbot_rest.reader.CategoryUpdateReader
import fr.spoutnik87.musicbot_rest.repository.CategoryRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.service.CategoryService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.viewmodel.CategoryViewModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("category")
class CategoryController {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var categoryService: CategoryService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/server/{serverId}")
    fun getByServerId(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasServer(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(server.categorySet.map { CategoryViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val category = categoryRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasServer(category.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(CategoryViewModel.from(category), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody categoryCreateReader: CategoryCreateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(categoryCreateReader.serverId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateCategoryPermission(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val category = categoryService.create(categoryCreateReader.name, server)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(CategoryViewModel.from(category), HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(@PathVariable("id") uuid: String, @RequestBody categoryUpdateReader: CategoryUpdateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var category = categoryRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateCategoryPermission(category)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        category = categoryService.update(category, categoryUpdateReader.name)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(CategoryViewModel.from(category), HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val category = categoryRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasDeleteCategoryPermission(category)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}