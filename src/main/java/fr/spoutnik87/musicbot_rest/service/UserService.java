package fr.spoutnik87.musicbot_rest.service;

import fr.spoutnik87.musicbot_rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;
}
