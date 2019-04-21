package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.model.Role;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.model.Views;
import fr.spoutnik87.musicbot_rest.reader.UserSignupReader;
import fr.spoutnik87.musicbot_rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
public class UserController {

  @Autowired private UserRepository userRepository;

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @JsonView(Views.Public.class)
  @PostMapping("")
  public ResponseEntity<Object> signup(@RequestBody UserSignupReader userSignupReader) {
    User user =
        new User(
            userSignupReader.getEmail(),
            userSignupReader.getNickname(),
            userSignupReader.getFirstname(),
            userSignupReader.getLastname(),
            bCryptPasswordEncoder.encode(userSignupReader.getPassword()),
            Role.USER);
    this.userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }
}
