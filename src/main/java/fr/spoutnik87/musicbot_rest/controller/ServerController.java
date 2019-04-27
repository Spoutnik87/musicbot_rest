package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum;
import fr.spoutnik87.musicbot_rest.constant.RoleEnum;
import fr.spoutnik87.musicbot_rest.model.*;
import fr.spoutnik87.musicbot_rest.reader.ServerCreateReader;
import fr.spoutnik87.musicbot_rest.reader.ServerUpdateReader;
import fr.spoutnik87.musicbot_rest.repository.*;
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("server")
public class ServerController {

  @Autowired private UserRepository userRepository;

  @Autowired private ServerRepository serverRepository;

  @Autowired private BotRepository botRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private UserGroupRepository userGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

  @JsonView(Views.Public.class)
  @GetMapping("/{id}")
  public ResponseEntity<Object> getById(@PathVariable("id") long id) {
    Optional<Server> optionalServer = serverRepository.findById(id);
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(optionalServer.get(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/list/{userId}")
  public ResponseEntity<Object> getByUserId(@PathVariable("userId") long userId) {
      Optional<User> optionalAuthenticatedUser =
              userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalAuthenticatedUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    User user = optionalAuthenticatedUser.get();
    if (user.getId() == userId) {
      return new ResponseEntity<>(user.getServerSet().toArray(), HttpStatus.OK);
    }
      if (user.getRole().getName() != RoleEnum.ADMIN.getName()) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    Optional<User> optionalUser = userRepository.findById(userId);
    if (!optionalUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    user = optionalUser.get();
    return new ResponseEntity<>(user.getServerSet().toArray(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @PostMapping("")
  public ResponseEntity<Object> create(@RequestBody ServerCreateReader serverCreateReader) {
      Optional<User> optionalAuthenticatedUser =
              userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!optionalAuthenticatedUser.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    User user = optionalAuthenticatedUser.get();
    Server server = new Server();
    server.setName(serverCreateReader.getName());
    server.setOwner(user);
    user.getServerSet().add(server);
    Bot bot = new Bot();
    bot.setName("Bot " + serverCreateReader.getName());
    bot.setToken(serverCreateReader.getToken());
    server.setBot(bot);

    Group group = new Group();
    group.setName("Default");
    group.setServer(server);
    UserGroup userGroup = new UserGroup();
    userGroup.setGroup(group);
    userGroup.setUser(user);
    Set<Permission> permissionSet = new HashSet<>();
      permissionSet.add(
              permissionRepository.findByValue(PermissionEnum.CREATE_MEDIA.getValue()).get());
      permissionSet.add(
              permissionRepository.findByValue(PermissionEnum.DELETE_MEDIA.getValue()).get());
    permissionSet.add(permissionRepository.findByValue(PermissionEnum.READ_MEDIA.getValue()).get());
      permissionSet.add(
              permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.getValue()).get());
    permissionSet.add(permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.getValue()).get());
    permissionSet.add(permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.getValue()).get());
      permissionSet.add(
              permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.getValue()).get());
      permissionSet.add(
              permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.getValue()).get());
    userGroup.setPermissionSet(permissionSet);
    user.getUserGroupSet().add(userGroup);
    group.getUserGroupSet().add(userGroup);

    serverRepository.save(server);
    botRepository.save(bot);
    groupRepository.save(group);
    userGroupRepository.save(userGroup);
    userRepository.save(user);
    return new ResponseEntity<>(server, HttpStatus.CREATED);
  }

  @JsonView(Views.Public.class)
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
      @PathVariable("id") long id, @RequestBody ServerUpdateReader serverUpdateReader) {
    Optional<Server> optionalServer = serverRepository.findById(id);
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Server server = optionalServer.get();
      Optional<User> optionalAuthenticatedUser =
              userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    User user = optionalAuthenticatedUser.get();
    if (!user.isOwner(server)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    server.setName(serverUpdateReader.getName());
    serverRepository.save(server);
    return new ResponseEntity<>(server, HttpStatus.ACCEPTED);
  }

  @JsonView(Views.Public.class)
  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") long id) {
    Optional<Server> optionalServer = serverRepository.findById(id);
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Server server = optionalServer.get();
      Optional<User> optionalAuthenticatedUser =
              userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    User user = optionalAuthenticatedUser.get();
    if (!user.isOwner(server)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    serverRepository.delete(server);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
