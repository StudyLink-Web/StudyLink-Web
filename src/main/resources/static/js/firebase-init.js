import { initializeApp, getApps } from "https://www.gstatic.com/firebasejs/12.8.0/firebase-app.js";
import {
    getAuth,
    RecaptchaVerifier,
    signInWithPhoneNumber
} from "https://www.gstatic.com/firebasejs/12.8.0/firebase-auth.js";

// 모듈 스코프(전역 window 말고)에서 보관
let firebaseApp = null;
let firebaseAuth = null;
let recaptchaVerifier = null;
let initDone = false;

async function ensureFirebaseInit() {
    if (initDone && firebaseAuth) return firebaseAuth;

    const res = await fetch("/mentor/firebase-config");
    if (!res.ok) throw new Error("Firebase config fetch failed");
    const config = await res.json();

    firebaseApp = getApps().length ? getApps()[0] : initializeApp(config);
    firebaseAuth = getAuth(firebaseApp);

    // 필요하면 window에 “참조”만 노출(디버깅용)
    window.firebaseApp = firebaseApp;
    window.firebaseAuth = firebaseAuth;

    console.log("🔥 Firebase 초기화 완료:", config.projectId);
    initDone = true;
    return firebaseAuth;
}

async function resetRecaptcha(containerId = "recaptcha-container") {
    try { recaptchaVerifier?.clear?.(); } catch (_) {}
    recaptchaVerifier = null;

    const el = document.getElementById(containerId);
    if (el) el.innerHTML = "";
}

async function ensureRecaptcha(containerId = "recaptcha-container") {
    const auth = await ensureFirebaseInit();

    if (recaptchaVerifier) return recaptchaVerifier;

    const container = document.getElementById(containerId);
    if (!container) throw new Error(`reCAPTCHA container not found: #${containerId}`);

    // 새로 만들기 직전에만 비우기
    container.innerHTML = "";

    // ✅ 핵심: window.firebaseAuth 말고 "auth 변수"를 그대로 넣는다
    recaptchaVerifier = new RecaptchaVerifier(
        auth,
        containerId,
        {
            size: "normal",
            callback: () => console.log("✅ reCAPTCHA 인증 완료 (normal)"),
            "expired-callback": () => console.warn("⚠️ reCAPTCHA 만료됨"),
        }
    );

    await recaptchaVerifier.render();
    console.log("🧩 reCAPTCHA render 완료");
    return recaptchaVerifier;
}

// 외부에서 호출하는 함수들(window에 노출)
window.sendFirebasePhoneCode = async function (phoneNumber) {
    if (!phoneNumber.startsWith("+")) {
        throw new Error("전화번호는 +82 형식으로 입력해야 합니다");
    }

    try {
        const auth = await ensureFirebaseInit();
        const verifier = await ensureRecaptcha();

        console.log("🧪 [SMS 요청 직전] 상태", {
            authExists: !!auth,
            verifierExists: !!verifier,
            authAppName: auth?.app?.name,
            phoneNumber
        });

        const confirmationResult = await signInWithPhoneNumber(auth, phoneNumber, verifier);
        window.confirmationResult = confirmationResult;

        console.log("📨 인증 문자 발송 성공:", phoneNumber);
        return true;

    } catch (error) {
        console.error("❌ 문자 발송 실패", error);

        // 캡챠/credential 계열은 리셋 후 재시도 가능하게
        if (
            error?.code === "auth/invalid-app-credential" ||
            error?.code === "auth/captcha-check-failed" ||
            String(error?.message || "").includes("recaptcha")
        ) {
            await resetRecaptcha();
        }

        throw error;
    }
};

window.verifyFirebasePhoneCode = async function (code) {
    if (!window.confirmationResult) {
        alert("먼저 인증번호를 요청해주세요");
        return { success: false };
    }

    try {
        const result = await window.confirmationResult.confirm(code);
        const user = result.user;
        const idToken = await user.getIdToken();

        console.log("✅ 전화번호 인증 성공:", user.phoneNumber);

        return { success: true, phoneNumber: user.phoneNumber, idToken };

    } catch (error) {
        console.error("❌ 인증 실패", error);
        alert("인증번호가 올바르지 않습니다");
        return { success: false };
    }
};

console.log("🧪 recaptcha-container 존재:", !!document.getElementById("recaptcha-container"));
