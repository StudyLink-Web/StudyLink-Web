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
  console.log("🏢 백그라운드 메시지 수신:", payload);

  const notificationTitle = payload.notification.title;
  const notificationOptions = {
    body: payload.notification.body,
    icon: "/pwa-192x192.png", // PWA 아이콘 경로
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
