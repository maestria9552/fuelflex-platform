package com.fuelflex.platform.user.service;

import com.fuelflex.platform.user.dto.request.ChangeMyPasswordRequest;
import com.fuelflex.platform.user.dto.response.MyAccountResponse;

public interface UserAccountService {

    MyAccountResponse getMyAccount(String authenticatedEmail);

    MyAccountResponse changeMyPassword(
            String authenticatedEmail,
            ChangeMyPasswordRequest request
    );
}
