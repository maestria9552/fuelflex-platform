package com.fuelflex.platform.auth.service;

import com.fuelflex.platform.auth.dto.RegisterRequest;
import com.fuelflex.platform.auth.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

}