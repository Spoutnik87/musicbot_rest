package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.UUID;
import fr.spoutnik87.musicbot_rest.constant.RoleEnum;
import fr.spoutnik87.musicbot_rest.model.*;
import fr.spoutnik87.musicbot_rest.reader.UserSignupReader;
import fr.spoutnik87.musicbot_rest.reader.UserUpdateReader;
import fr.spoutnik87.musicbot_rest.repository.GroupRepository;
import fr.spoutnik87.musicbot_rest.repository.RoleRepository;
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

  @Autowired private RoleRepository roleRepository;

  @Autowired private UUID uuid;

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @JsonView(Views.Public.class)
  @GetMapping("")
  public ResponseEntity<Object> getLogged() {
    Optional<User> optionalUser =
        userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(optionalUser.get(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/{id}")
  public ResponseEntity<Object> getById(@PathVariable("id") String uuid) {
    Optional<User> optionalAuthenticatedUser =
        userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalAuthenticatedUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    if (optionalAuthenticatedUser.get().getRole().getName() != RoleEnum.ADMIN.getName()) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    Optional<User> optionalUser = userRepository.findByUuid(uuid);
    if (!optionalUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(optionalUser.get(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/list/server/{serverId}")
  public ResponseEntity<Object> getByServerId(@PathVariable("serverId") String serverUuId) {
    Optional<Server> optionalServer = serverRepository.findByUuid(serverUuId);
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Optional<User> optionalAuthenticatedUser =
            userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalAuthenticatedUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Server server = optionalServer.get();
    if (!server.hasUser(optionalAuthenticatedUser.get())) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(server.getUserList(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/list/group/{groupId}")
  public ResponseEntity<Object> getByGroupId(@PathVariable("groupId") String groupUuid) {
    Optional<Group> optionalGroup = groupRepository.findByUuid(groupUuid);
    if (!optionalGroup.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Optional<User> optionalAuthenticatedUser =
            userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalAuthenticatedUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Group group = optionalGroup.get();
    Server server = group.getServer();
    if (!server.hasUser(optionalAuthenticatedUser.get())) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(group.getUserList(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @PostMapping()
  public ResponseEntity<Object> signup(@RequestBody UserSignupReader userSignupReader) {
    Optional<Role> optionalUserRole = roleRepository.findByName(RoleEnum.USER.getName());
    if (!optionalUserRole.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    User user =
        new User(
            uuid.v4(),
            userSignupReader.getEmail(),
            userSignupReader.getNickname(),
            userSignupReader.getFirstname(),
            userSignupReader.getLastname(),
            bCryptPasswordEncoder.encode(userSignupReader.getPassword()),
            optionalUserRole.get());
    this.userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @JsonView(Views.Public.class)
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
          @PathVariable("id") String uuid, @RequestBody UserUpdateReader userUpdateReader) {
    Optional<User> optionalAuthenticatedUser =
            userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalAuthenticatedUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    User user = optionalAuthenticatedUser.get();
    if (!user.getUuid().equals(uuid)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    if (userUpdateReader.getNickname() != null) {
      user.setNickname(userUpdateReader.getNickname());
    }
    if (userUpdateReader.getFirstname() != null) {
      user.setFirstname(userUpdateReader.getFirstname());
    }
    if (userUpdateReader.getLastname() != null) {
      user.setLastname(userUpdateReader.getLastname());
    }
    userRepository.save(user);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }
}
