// FirebaseWebConfigController

package com.StudyLink.www.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mentor")
public class FirebaseWebConfigController {

    @Value("${firebase.api-key}")
    private String apiKey;

    @Value("${firebase.auth-domain}")
    private String authDomain;

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.storage-bucket}")
    private String storageBucket;

    @Value("${firebase.messaging-sender-id}")
    private String messagingSenderId;

    @Value("${firebase.app-id}")
    private String appId;

    @GetMapping("/firebase-config")
    public Map<String, String> getConfig() {
        return Map.of(
                "apiKey", apiKey,
                "authDomain", authDomain,
                "projectId", projectId,
                "storageBucket", storageBucket,
                "messagingSenderId", messagingSenderId,
                "appId", appId
        );
    }
}
