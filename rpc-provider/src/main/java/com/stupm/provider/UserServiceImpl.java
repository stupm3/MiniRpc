package com.stupm.provider;


import com.stupm.common.model.User;
import com.stupm.common.service.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        return user;
    }
}
