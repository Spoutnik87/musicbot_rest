package fr.spoutnik87.musicbot_rest.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.model.Group;
import fr.spoutnik87.musicbot_rest.model.Server;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.model.Views;
import fr.spoutnik87.musicbot_rest.reader.GroupCreateReader;
import fr.spoutnik87.musicbot_rest.reader.GroupUpdateReader;
import fr.spoutnik87.musicbot_rest.repository.GroupRepository;
import fr.spoutnik87.musicbot_rest.repository.ServerRepository;
import fr.spoutnik87.musicbot_rest.repository.UserRepository;
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("group")
public class GroupController {

  @Autowired private UserRepository userRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private ServerRepository serverRepository;

  @JsonView(Views.Public.class)
  @GetMapping("/{id}")
  public ResponseEntity<Object> getById(@PathVariable("id") long id) {
    Optional<Group> optionalGroup = groupRepository.findById(id);
    if (!optionalGroup.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Group group = optionalGroup.get();
    Server server = group.getServer();
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!server.hasUser(user)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(group, HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/server/{id}")
  public ResponseEntity<Object> getByServerId(@PathVariable("id") long serverId) {
    Optional<Server> optionalServer = serverRepository.findById(serverId);
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Server server = optionalServer.get();
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!server.hasUser(user)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    return new ResponseEntity<>(server.getGroupSet().toArray(), HttpStatus.OK);
  }

  @JsonView(Views.Public.class)
  @PostMapping("")
  public ResponseEntity<Object> create(@RequestBody GroupCreateReader groupCreateReader) {
    Optional<Server> optionalServer = serverRepository.findById(groupCreateReader.getServerId());
    if (!optionalServer.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Server server = optionalServer.get();
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    if (!user.isOwner(server)) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    Group group = new Group();
    group.setName(groupCreateReader.getName());
    group.setServer(server);
    Set<Group> groupSet = new HashSet<>();
    groupSet.add(group);
    server.setGroupSet(groupSet);
    groupRepository.save(group);
    return new ResponseEntity<>(null);
  }

  @JsonView(Views.Public.class)
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
      @PathVariable("id") long id, @RequestBody GroupUpdateReader groupUpdateReader) {
    User user = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail());
    Optional<Group> optionalGroup = groupRepository.findById(id);
    if (!optionalGroup.isPresent()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Group group = optionalGroup.get();
    if (!user.isOwner(group.getServer())) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    group.setName(groupUpdateReader.getName());
    groupRepository.save(group);
    return new ResponseEntity<>(group, HttpStatus.ACCEPTED);
  }
}
