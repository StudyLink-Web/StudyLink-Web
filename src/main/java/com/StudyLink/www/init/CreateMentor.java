package com.StudyLink.www.init;

import com.StudyLink.www.service.MentorProfileService;
import com.StudyLink.www.service.UserService;
import lombok.*;

@RequiredArgsConstructor
public class CreateMentor {
    private final UserService userService;
    private final MentorProfileService mentorProfileService;
}
