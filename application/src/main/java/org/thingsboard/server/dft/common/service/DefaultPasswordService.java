package org.thingsboard.server.dft.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultPasswordService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private static final String DEFAULT_PASSWORD = "12345678";

    @Autowired
    public DefaultPasswordService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public String getDefaultPassword() {
        return bCryptPasswordEncoder.encode(DEFAULT_PASSWORD);
    }

    public String getRawPassword() {
        return DEFAULT_PASSWORD;
    }


}
