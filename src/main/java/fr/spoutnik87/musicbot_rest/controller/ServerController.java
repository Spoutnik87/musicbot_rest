package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.model.Views;
import fr.spoutnik87.musicbot_rest.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/server")
public class ServerController {

  @Autowired private ServerRepository serverRepository;

  @JsonView(Views.Public.class)
  @PostMapping("")
  public ResponseEntity<Object> create() {
    // TODO
    return new ResponseEntity<>(null);
  }
}
