package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.UUID;
import fr.spoutnik87.musicbot_rest.model.*;
import fr.spoutnik87.musicbot_rest.reader.UserSignupReader;
import fr.spoutnik87.musicbot_rest.reader.UserUpdateReader;
import fr.spoutnik87.musicbot_rest.repository.GroupRepository;
import fr.spoutnik87.musicbot_rest.repository.ServerRepository;
import fr.spoutnik87.musicbot_rest.repository.UserRepository;
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("user")
public class UserController {

  @Autowired private UserRepository userRepository;

  @Autowired private ServerRepository serverRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @JsonView(Views.Public.class)
  @GetMapping("")
  public ResponseEntity<Object> getLogged() {
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/{id}")
  public ResponseEntity<Object> getById(@PathVariable("id") long id) {
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (user.getRole().getName() != Role.ADMIN.getName()) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    Optional<User> optionalUser = userRepository.findById(id);
    if (!optionalUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(optionalUser.get(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/list/server/{serverId}")
  public ResponseEntity<Object> getByServerId(@PathVariable("serverId") long serverId) {
    Optional<Server> optionalServer = serverRepository.findById(serverId);
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    Server server = optionalServer.get();
    if (!server.hasUser(user)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(server.getUserList(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/list/group/{groupId}")
  public ResponseEntity<Object> getByGroupId(@PathVariable("groupId") long groupId) {
    Optional<Group> optionalGroup = groupRepository.findById(groupId);
    if (!optionalGroup.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    Group group = optionalGroup.get();
    Server server = group.getServer();
    if (!server.hasUser(user)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(group.getUserList(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @PostMapping()
  public ResponseEntity<Object> signup(@RequestBody UserSignupReader userSignupReader) {

    Role r = Role.USER;
    User user =
        new User(
            UUID.v4(),
            userSignupReader.getEmail(),
            userSignupReader.getNickname(),
            userSignupReader.getFirstname(),
            userSignupReader.getLastname(),
            bCryptPasswordEncoder.encode(userSignupReader.getPassword()),
            Role.USER);
    // this.userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @JsonView(Views.Public.class)
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
      @PathVariable("id") long id, @RequestBody UserUpdateReader userUpdateReader) {
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (user.getId() != id) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    user.setNickname(userUpdateReader.getNickname());
    user.setFirstname(userUpdateReader.getFirstname());
    user.setLastname(userUpdateReader.getLastname());
    userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }
}
