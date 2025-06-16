package com.stupm.consumer;

import com.stupm.common.model.User;
import com.stupm.common.service.UserService;
import com.stupm.core.proxy.ServiceProxyFactory;


public class ConsumerStarter {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("stupm");
        User newUser = userService.getUser(user);
        if(newUser != null) {
            System.out.println(newUser.getName());
        }else{
            System.out.println("user not found");
        }

    }
}
