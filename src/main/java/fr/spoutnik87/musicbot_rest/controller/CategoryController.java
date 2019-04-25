package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.model.Views;
import fr.spoutnik87.musicbot_rest.reader.CategoryCreateReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("category")
public class CategoryController {

  @JsonView(Views.Public.class)
  @PostMapping("")
  public ResponseEntity<Object> create(@RequestBody CategoryCreateReader categoryCreateReader) {
    // TODO
    return new ResponseEntity<>(null);
  }
}
