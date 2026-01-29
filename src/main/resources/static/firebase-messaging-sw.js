// Scripts for firebase and firebase messaging
importScripts("https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js");
importScripts(
  "https://www.gstatic.com/firebasejs/8.10.0/firebase-messaging.js",
);

// Firebase 콘솔에서 발급받은 설정값 반영 완료
const firebaseConfig = {
  apiKey: "AIzaSyAhe6Cn1D0XMddJDISJlUBCxI-au6JKrP8",
  authDomain: "studylink-pwa-alert.firebaseapp.com",
  projectId: "studylink-pwa-alert",
  storageBucket: "studylink-pwa-alert.firebasestorage.app",
  messagingSenderId: "738913176894",
  appId: "1:738913176894:web:09b9c5951b2c334d8ef06f",
  measurementId: "G-3952QT505P",
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// 백그라운드 메시지 수신부
messaging.onBackgroundMessage((payload) => {
  console.log("🏢 백그라운드 메시지 수신 (Smart Filtering):", payload);

  const notificationTitle = payload.data.title || "StudyLink 알림";
  const notificationOptions = {
    body: payload.data.body || "",
    icon: "/pwa-192x192.png",
    tag: "studylink-notification", // 중복 알림 방지 태그
  };

  // 📍 스마트 필터링: 현재 사용자가 챗봇 페이지를 보고 있는지 확인
  self.clients
    .matchAll({ type: "window", includeUncontrolled: true })
    .then((windowClients) => {
      const isChatbotActive = windowClients.some((client) => {
        // 챗봇 페이지이면서 포커싱(활성화)되어 있는지 확인
        return (
          client.url.includes("/chatbot") &&
          client.visibilityState === "visible"
        );
      });

      if (isChatbotActive) {
        console.log("🤫 사용자가 챗봇을 보고 있으므로 무음 처리합니다.");
        return;
      }

      // 챗봇을 보고 있지 않을 때만 시스템 알림 표시
      return self.registration.showNotification(
        notificationTitle,
        notificationOptions,
      );
    });
});
