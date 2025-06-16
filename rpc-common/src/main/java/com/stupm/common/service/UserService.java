package com.stupm.common.service;

import com.stupm.common.model.User;

public interface UserService {

    default short getNumber(){
        return 1;
    }

    public User getUser(User user);
}
