/**
 * 🔥 Firebase Cloud Messaging (FCM) 초보자 가이드
 * 
 * 1. FCM이란?
 *    - 웹사이트나 앱을 끄고 있어도 사용자에게 실시간 '푸시 알림'을 보내주는 서비스입니다. (일종의 무료 디지털 우체국)
 * 
 * 2. 핵심 개념: '토큰(Token)'
 *    - 토큰은 사용자의 기기를 식별하는 '디지털 주소'입니다.
 *    - 우리 서버(Spring Boot)는 이 주소를 알고 있어야 해당 사용자에게만 정확히 알림을 보낼 수 있습니다.
 * 
 * 3. 전체 동작 흐름 (매우 중요!)
 *    [사용자 브라우저] --- (권한 요청) ---> [사용자 승인]
 *    [사용자 브라우저] --- (토큰 요청) ---> [Firebase 서버]
 *    [사용자 브라우저] <--- (토큰 발급) --- [Firebase 서버]
 *    [사용자 브라우저] --- (토큰 저장) ---> [우리의 Spring Boot 서버]
 *    ... (나중에 알림 보낼 때) ...
 *    [우리 서버] --- (메시지 배달 요청 + 토큰) ---> [Firebase 서버] ---> [사용자 기기 알람!]
 * 
 * 4. 이 파일의 역할
 *    - Firebase 설정을 초기화하고, 브라우저에서 '디지털 주소(토큰)'를 받아오는 일을 담당합니다.
 */

import { initializeApp } from "firebase/app";
import {
  getMessaging,
  getToken,
  onMessage,
  isSupported,
} from "firebase/messaging";

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

// Firebase 앱 초기화
const app = initializeApp(firebaseConfig);

// 메시징 인스턴스 획득
export const messaging = getMessaging(app);

// 푸시 알림 권한 요청 및 토큰 획득 함수
export const requestForToken = async () => {
  // 📍 브라우저 지원 여부 확인 강화
  const supported = await isSupported();
  if (!supported) {
    console.error("❌ 이 브라우저는 알림 기능을 지원하지 않습니다.");
    throw new Error("unsupported-browser-by-firebasesdk");
  }

  if (!("serviceWorker" in navigator)) {
    console.error("❌ 서비스 워커가 지원되지 않는 환경입니다.");
    return null;
  }

  try {
    // 📍 1단계: 서비스 워커 등록 (이미 있으면 기존 것 사용)
    await navigator.serviceWorker.register("/firebase-messaging-sw.js");

    // 📍 2단계: 서비스 워커가 '활성화(Active)' 상태가 될 때까지 기다림
    // 이 코드가 없으면 "no active Service Worker" 에러가 발생할 수 있습니다.
    const registration = await navigator.serviceWorker.ready;

    // 📍 3단계: 토큰 요청
    const currentToken = await getToken(messaging, {
      serviceWorkerRegistration: registration,
      vapidKey:
        "BJWVAmiSrSkQNbCtS4EjgmHJIk5S6qUgnCtqEc-e7YV4r06-3uMvKQ334YtNKsVUb8dIgsMKJSz7WPXFMchWMHY",
    });

    if (currentToken) {
      console.log("✅ 기기 토큰 획득 성공:", currentToken);
      return currentToken;
    } else {
      console.log("❌ 토큰을 획득할 수 없습니다. 권한 승인이 필요합니다.");
      return null;
    }
  } catch (err) {
    console.error("❌ 토큰 획득 중 오류 발생:", err);
    throw err;
  }
};

// 포그라운드 메시지 수신 대기 (앱이 켜져 있을 때)
export const onMessageListener = () =>
  new Promise((resolve) => {
    onMessage(messaging, (payload) => {
      console.log("📩 포그라운드 메시지 수신:", payload);
      resolve(payload);
    });
  });
