package org.aman.shardedsagawallet.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aman.shardedsagawallet.entities.User;
import org.aman.shardedsagawallet.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        log.info("Creating the user : {} ",user.getEmail());
        User newUser=userRepository.save(user);
        log.info("User Created with id: {} in database {}",newUser.getId(),(newUser.getId()%2+1));
        return newUser;
    }
}
