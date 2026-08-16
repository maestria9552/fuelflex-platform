package com.fuelflex.platform.user.service;

import com.fuelflex.platform.user.dto.request.UpdateMyProfileRequest;
import com.fuelflex.platform.user.dto.response.MyProfileResponse;

public interface UserProfileService {

    MyProfileResponse getMyProfile(String authenticatedEmail);

    MyProfileResponse updateMyProfile(
            String authenticatedEmail,
            UpdateMyProfileRequest request
    );
}
