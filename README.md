# 🎓 StudyLink

StudyLink는 **입시 준비생(STUDENT)**과 **대학생 멘토(MENTOR)**를 연결하는  
교육 멘토링 플랫폼입니다.

단순한 게시판이나 매칭 서비스가 아니라,  
**역할 기반 인증·권한 시스템**,  
**데이터 분석 대시보드**,  
**실시간 협업 및 알림**,  
**운영자 중심 관리 기능**까지 포함한  
실제 서비스 운영을 목표로 설계된 풀스택 웹 애플리케이션입니다.

---

## 🔍 프로젝트 개요

- **Frontend**: React + TypeScript (Vite)
- **Backend**: Spring Boot + Spring Security
- **Database**: MySQL, MongoDB
- **Infra / Etc**: Firebase, WebSocket, Docker, PWA
- **Architecture**: 역할 기반 인증(RBAC) + 모듈화된 서비스 구조

---

## 🧩 전체 시스템 구조

- React 기반 SPA 프론트엔드
- Spring Boot 기반 API 서버
- Python 기반 AI 분석 서버 (컨테이너 분리)
- Firebase 기반 인증 및 푸시 알림
- WebSocket 기반 실시간 협업 기능

![시스템 매크로 아키텍처](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/시스템매크로아키텍처.png)
![백엔드 기술 스택 및 구조](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/백엔드기술스택및구조.png)
![프론트 엔지니어링](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/프론트엔지니어링.png)
![데이터베이스 모델링](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/데이터베이스모델링.png)
![ERD 구조](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/ERD.png)

---

## 🔐 사용자 인증 & 보안 구조

### 인증 방식

- **Form Login** (아이디 / 비밀번호)
- **OAuth2 Google Login**
- **Firebase 전화번호 인증 (SMS)**

모든 인증 요청은 Spring Security Filter Chain을 통과하며,  
인증 성공 시 `SecurityContext`에 인증 정보가 저장됩니다.

JWT를 사용하지 않고 **서버 세션 + SecurityContext 기반 인증 유지** 방식을 채택했습니다.

---

### 역할 기반 접근 제어 (RBAC)

사용자는 다음 역할 중 하나를 가집니다.

- `STUDENT`
- `MENTOR`
- `ADMIN`

요청 시 서버는 사용자 역할을 확인하여  
기능 접근 여부를 제어합니다.

권한이 없는 접근은 **404 페이지**로 처리됩니다.
<img width="1277" height="661" alt="image" src="https://github.com/user-attachments/assets/4fdd8de5-5ca3-4cdd-afe2-4c6f55bd61ce" />


---

## 🎓 대학생 인증 & 멘토 전환

STUDENT 사용자는 **학교 이메일 인증**을 통해  
실제 대학생임을 검증받을 수 있습니다.

### 인증 흐름

1. 학교 이메일 입력
2. 인증 토큰 발급 (만료 시간 포함)
3. 이메일 링크 클릭
4. 토큰 유효성 검증
5. 역할 변경: `STUDENT → MENTOR`

인증 메일 재전송에는 **쿨다운 제한**을 두어  
보안과 남용을 방지했습니다.

![이메일 인증](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증.png)
![이메일 전송](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증전송.png)
![이메일 수신](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/학교이메일수신.png)
![인증 성공](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증성공.png)
![인증 실패](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증실패.png)

---

## 📱 전화번호 인증 (Firebase)

- Firebase Auth 기반 SMS 인증
- Invisible reCAPTCHA 적용
- 인증 성공 시 사용자 계정과 인증 상태 연동

봇·자동화 요청을 방지하고  
실제 사용자만 서비스 핵심 기능을 사용할 수 있도록 설계했습니다.

<스크린샷: 전화번호 인증 UI>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 👤 마이페이지 & 계정 관리

사용자는 마이페이지에서 다음 기능을 직접 관리할 수 있습니다.

- 비밀번호 변경
- 이메일 변경
- 계정 삭제
- 알림 수신 여부 설정
- 프로필 공개 여부 설정

실제 서비스 운영을 고려하여  
**본인 확인 절차 및 예외 처리**를 함께 구현했습니다.

<스크린샷: 마이페이지 설정 화면>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 📊 데이터 분석 대시보드

대시보드는 단순 통계 화면이 아니라,  
**입시 데이터를 분석해 전략으로 변환하는 분석 파이프라인의 결과물**입니다.

### 처리 흐름

1. 성적 데이터 수집
2. 멱등성(Atomic Idempotency) 보장 저장
3. QueryDSL 기반 시계열 데이터 조회
4. Python 분석 엔진으로 전달
5. 선형 보간 알고리즘 적용
6. Chart.js 기반 시각화

<스크린샷: 대시보드 차트 화면>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 💬 문의 게시판 & 관리자 운영 기능

### 사용자 문의

- 문의 등록 / 조회
- 공개 / 비공개 문의 구분
- 비공개 문의 비밀번호 검증

### 관리자 기능

- 관리자 전용 문의 관리 페이지
- 카테고리 / 상태 / 기간 검색
- 문의 답변 등록
- 답변 상태 자동 변경

실제 고객센터(CS) 운영을 고려한 구조입니다.

<스크린샷: 문의 게시판 목록>  
<스크린샷: 관리자 문의 상세 + 답변 화면>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 🔔 실시간 알림 시스템 (PWA)

- Firebase Cloud Messaging 기반 푸시 알림
- Service Worker 기반 PWA 지원
- Context-Aware Notification 구현

사용자가 이미 해당 화면을 보고 있다면  
알림을 자동으로 차단하여 UX를 개선했습니다.

<스크린샷: 푸시 알림 수신 화면>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 🎨 실시간 협업 그림판

- WebSocket 기반 실시간 통신
- STOMP 프로토콜을 활용한 메시지 라우팅
- 서버 브로드캐스트 방식으로 동기화 유지

여러 사용자가 동시에 그림을 그려도  
모든 화면이 실시간으로 동일하게 유지됩니다.

<스크린샷: 실시간 협업 그림판>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 💳 결제 시스템

- Toss Payments 연동
- 서버에서 주문 번호 생성
- DB 기준 금액 검증
- 위·변조 및 이중 결제 방지

<스크린샷: 결제 화면 또는 결제 완료 화면>
![인증 결과 화면](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/email-verification2.png)

---

## 🧱 기술 스택

### Backend
- Java 17
- Spring Boot
- Spring Security
- JPA / Hibernate
- QueryDSL
- MySQL
- MongoDB

### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS

### Infra / Etc
- Firebase Auth & FCM
- WebSocket (STOMP)
- PWA (Service Worker)
- Docker
- CI/CD

---

## 🌿 브랜치 전략

- `main` : 배포
- `dev` : 개발
- `feature/*` : 기능 단위

커밋 메시지 규칙:
- `feat:` 기능 추가
- `fix:` 버그 수정
- `refactor:` 리팩토링
- `docs:` 문서 수정

---

## 📷 README 이미지 경로

