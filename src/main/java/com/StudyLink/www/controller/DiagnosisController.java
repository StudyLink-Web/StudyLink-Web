package com.StudyLink.www.controller;

import com.google.firebase.FirebaseApp;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    @GetMapping("/firebase")
    public Map<String, Object> checkFirebase() {
        Map<String, Object> result = new HashMap<>();

        // 1. Check Service Account File
        try {
            ClassPathResource resource = new ClassPathResource("service-account.json");
            result.put("file_exists", resource.exists());
            result.put("file_path", resource.getPath());
            if (resource.exists()) {
                result.put("file_length", resource.contentLength());
            }
        } catch (Exception e) {
            result.put("file_error", e.getMessage());
        }

        // 2. Check FirebaseApp
        try {
            boolean hasApps = !FirebaseApp.getApps().isEmpty();
            result.put("has_firebase_apps", hasApps);
            if (hasApps) {
                result.put("apps",
                        FirebaseApp.getApps().stream().map(FirebaseApp::getName).collect(Collectors.toList()));
            } else {
                result.put("apps", "EMPTY");
            }
        } catch (Exception e) {
            result.put("firebase_app_error", e.getMessage());
        }

        return result;
    }
}
