package com.StudyLink.www;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class WwwApplication {

	public static void main(String[] args) throws IOException {
		// ✅ Firebase Admin SDK 초기화
		InputStream serviceAccount = WwwApplication.class
				.getClassLoader()
				.getResourceAsStream("firebase-key.json");

		if (serviceAccount != null) {
			GoogleCredentials credentials = GoogleCredentials
					.fromStream(serviceAccount);

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(credentials)
					.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
				System.out.println("✅ Firebase Admin SDK 초기화 완료");
			}
		} else {
			System.out.println("⚠️ firebase-key.json을 찾을 수 없습니다");
		}

		SpringApplication.run(WwwApplication.class, args);
	}

}
