package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;

public interface UserService {

    Users findByIdentifier(String identifier);

    Long findUserIdByIdentifier(String identifier);
}
