# 🎓 StudyLink

StudyLink는 입시 준비생(STUDENT)과 대학생 멘토(MENTOR)를 연결하는 교육 멘토링 플랫폼입니다.

단순한 게시판이나 매칭 서비스가 아니라
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

## 🏠 메인 화면

StudyLink의 메인 화면입니다.  
AI 상담, 자소서 메이커, 대학생 게시판, 대시보드 등  
핵심 기능으로 빠르게 접근할 수 있도록 설계했습니다.

<img width="1041" height="524" alt="image" src="https://github.com/user-attachments/assets/7228024f-c328-41cd-a64c-d9f9f63c7fe0" />
<img width="1268" height="664" alt="image" src="https://github.com/user-attachments/assets/98303432-4d78-4293-b103-e3595c9d57b1" />
<img width="1266" height="670" alt="image" src="https://github.com/user-attachments/assets/31cb5fa2-8f2e-4e97-96f0-cd8c954437aa" />
<img width="1267" height="668" alt="image" src="https://github.com/user-attachments/assets/07cc1d99-ef4f-40e5-a5d1-20b70bdfed7d" />
<img width="1266" height="669" alt="image" src="https://github.com/user-attachments/assets/ea2009a7-ab17-44e1-84c2-8b8362b926b1" />
<img width="1265" height="601" alt="image" src="https://github.com/user-attachments/assets/7776932e-c4eb-45b3-9a61-5798f8998d41" />

---

## 📱 모바일 화면

모바일 환경에서도 동일한 기능을 제공하도록  
반응형 UI로 구현하였습니다.

- 반응형 레이아웃 설계
- 터치 기반 UX 최적화
- PWA 지원

<table>
  <tr>
    <td>  <img width="266" height="581" alt="image" src="https://github.com/user-attachments/assets/e02444c7-0804-4626-be78-d1324f4274e1" width="100%" />  </td>
    <td>  <img width="260" height="545" alt="image" src="https://github.com/user-attachments/assets/f90da424-73db-4b9c-a0b4-9d53b2ad8340" width="100%" />  </td>
    <td>  <img width="259" height="547" alt="image" src="https://github.com/user-attachments/assets/32779e6c-ce29-4d53-821e-08738b614020" width="100%" />  </td>
    <td>  <img width="245" height="549" alt="image" src="https://github.com/user-attachments/assets/e218d553-6182-452a-89ef-6facbe4780de" width="100%" />  </td>
  </tr>
</table>

<table>
  <tr>
    <td>  <img width="276" height="551" alt="image" src="https://github.com/user-attachments/assets/800242c7-96fd-4be2-b600-aca8921c466d" width="100%" />  </td>
    <td>  <img width="269" height="590" alt="image" src="https://github.com/user-attachments/assets/d2966e0e-d4c5-421d-833f-3da882c0e1dd" width="100%" />  </td>
    <td>  <img width="268" height="593" alt="image" src="https://github.com/user-attachments/assets/534fa5f5-656e-49d3-808c-013cfbc8458d" width="100%" />  </td>
    <td>  <img width="276" height="554" alt="image" src="https://github.com/user-attachments/assets/809ca4b0-8250-4f42-97eb-71583565efa2" width="100%" />  </td>
  </tr>
</table>

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
보안과 남용을 방지했습니다.ㅤ
ㅤㅤ
이메일 인증
![이메일 인증](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증.png)
ㅤ    ㅤ
이메일 전송
![이메일 전송](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증전송.png)
ㅤ 
이메일 수신
![이메일 수신](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/학교이메일수신.png)
ㅤ
인증 성공
![인증 성공](https://raw.githubusercontent.com/StudyLink-Web/StudyLink-Web/dev/src/readme/멘토이메일인증성공.png)
<img width="1268" height="659" alt="image" src="https://github.com/user-attachments/assets/ea4583d8-f062-4609-82bf-eaa40d6df9c3" />

---

## 📱 전화번호 인증 (Firebase)

- Firebase Auth 기반 SMS 인증
- Invisible reCAPTCHA 적용
- 인증 성공 시 사용자 계정과 인증 상태 연동

봇·자동화 요청을 방지하고  
실제 사용자만 서비스 핵심 기능을 사용할 수 있도록 설계했습니다.

멘토 페이지 전화번호 인증
<img width="1028" height="491" alt="image" src="https://github.com/user-attachments/assets/14097758-d680-4cf3-a81b-5d07a406d6d0" />
ㅤ
환경설정 전화번호 인증
<img width="1040" height="499" alt="image" src="https://github.com/user-attachments/assets/092345bb-09bc-4bb5-97a4-a8f6df9af75f" />

<table>
  <tr>
    <td>  <img width="316" height="689" alt="image" src="https://github.com/user-attachments/assets/36dc9961-ff1a-449f-bfb4-03544d722968" width="100%" />  </td>
    <td>  <img width="314" height="688" alt="image" src="https://github.com/user-attachments/assets/1dc00753-0223-4f88-a939-6e237e50f5b9" width="100%" />  </td>
  </tr>
</table>

---

## 👤 환경설정 & 계정 관리

사용자는 환경설정(mypage.html)에서 다음 기능을 직접 관리할 수 있습니다.

- 비밀번호 변경
- 이메일 변경
- 계정 삭제
- 알림 수신 여부 설정
- 프로필 공개 여부 설정

실제 서비스 운영을 고려하여  
**본인 확인 절차 및 예외 처리**를 함께 구현했습니다.

환경설정 비밀번호 변경
<img width="1038" height="499" alt="image" src="https://github.com/user-attachments/assets/415a3bc0-1730-4fba-bce6-9d22c09fca0f" />
ㅤ
환경설정 이메일 변경
<img width="1034" height="494" alt="image" src="https://github.com/user-attachments/assets/353a41fc-bfbe-4ddd-b6d3-2ad14f491b2b" />
ㅤ
환경설정 이메일 변경 수신 메일
<img width="1037" height="497" alt="image" src="https://github.com/user-attachments/assets/54b56534-fa0a-41f5-97b3-ea926eb504df" />
<img width="1040" height="498" alt="image" src="https://github.com/user-attachments/assets/e7a53453-d0c9-4122-ab44-a9ab37b9fb9e" />
ㅤ
환경설정 전화번호 변경
<img width="1036" height="502" alt="image" src="https://github.com/user-attachments/assets/cee40a14-bc4f-4ada-ba01-d9980c8cd8cd" />


---

## 📊 데이터 분석 대시보드

파이썬 관련 코드
https://github.com/yaimnot23/chatbot_withpy.git 

대시보드는 단순 통계 화면이 아니라,  
**입시 데이터를 분석해 전략으로 변환하는 분석 파이프라인의 결과물**입니다.

### 처리 흐름

1. 성적 데이터 수집
2. 멱등성(Atomic Idempotency) 보장 저장
3. QueryDSL 기반 시계열 데이터 조회
4. Python 분석 엔진으로 전달
5. 선형 보간 알고리즘 적용
6. Chart.js 기반 시각화

대시보드 차트 화면
<img width="1265" height="666" alt="image" src="https://github.com/user-attachments/assets/b404288b-3965-425f-8744-39d271f525a3" />
<img width="1031" height="524" alt="image" src="https://github.com/user-attachments/assets/4a1a5103-7d35-4fa1-bc37-72d10385b3de" />

---

## 🤖 AI 대입 상담 컨설턴트

StudyLink는 단순 멘토링 플랫폼을 넘어,  
**공공 입시 데이터 기반 AI 분석 상담 시스템**을 제공합니다.

AI 대입 상담은 일반적인 챗봇이 아니라,  
입시 통계 데이터 + Python 분석 엔진 + 실시간 대화 UI가 결합된  
데이터 기반 전략 분석 서비스입니다.

AI 상담 화면
<img width="1278" height="670" alt="image" src="https://github.com/user-attachments/assets/c3123291-c8bc-4d26-a548-ba4147874fae" />

## 📊 사용 데이터 및 외부 API

AI 상담 기능은 다음 공공 데이터 및 외부 API를 활용합니다.

| 데이터 | 제공 기관 | 활용 내용 |
|--------|------------|------------|
| 대학별 정시 합격 70% Cut | 한국대학교육협의회 | 합격선 기준 분석 |
| 대학 경쟁률 및 모집 인원 | ADIGA (대입정보포털) | 경쟁 강도 분석 |
| 대학 기본 정보 | 대학알리미 | 정원, 계열, 지역 정보 |
| 지도 데이터 | Google Maps / Kakao Maps API | 대학 위치 시각화 |

수집된 공공 데이터를 정제·가공하여  
**대학별 입시 전략 데이터셋을 구축**하였습니다.


## 🧠 AI 분석 처리 흐름

1. 사용자가 대학 또는 학과 입력
2. 서버에서 대학 코드 및 전형 데이터 매핑
3. DB 기반 입시 데이터 조회
4. Python 분석 서버로 데이터 전달
5. 합격 가능성 및 전략 분석 수행
6. 분석 결과를 자연어 응답으로 변환
7. React 채팅 UI에 실시간 렌더링

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
ㅤ
대시보드
<img width="1266" height="667" alt="image" src="https://github.com/user-attachments/assets/9ee0d1a0-c19e-4685-a859-c5b5f0645be0" />
ㅤ
회원 관리
<img width="1264" height="674" alt="image" src="https://github.com/user-attachments/assets/0cee027a-db94-4bdf-adf2-9265b7760d20" />
ㅤ
결제 내역
<img width="1270" height="673" alt="image" src="https://github.com/user-attachments/assets/9fb54551-619d-41be-8e38-4032e71e5732" />
ㅤ
환전 관리
<img width="1268" height="671" alt="image" src="https://github.com/user-attachments/assets/fe5738e1-30fd-4bc8-bc22-863638848dbd" />
ㅤ
알림 / 공지
<img width="1267" height="669" alt="image" src="https://github.com/user-attachments/assets/991a76ff-b9dc-4939-8964-f2c752cb564d" />
ㅤ
문의 게시판 목록    
<img width="1265" height="671" alt="image" src="https://github.com/user-attachments/assets/b0b62c7d-883f-4f3f-9904-5c916ceebda9" />
ㅤ
관리자 문의 상세 + 답변 화면
<img width="1266" height="671" alt="image" src="https://github.com/user-attachments/assets/dc4d5f3b-3ff5-48b2-8fd8-817e6edf2075" />

---

## ✍️ AI 대입 자소서 메이커

사용자의 경험 키워드를 기반으로  
대학 및 학과 맞춤형 자소서 초안을 생성합니다.

### 주요 기능

- 생기부 키워드 자동 추출
- 대학 / 학과 맞춤 문항 적용
- AI 초안 자동 생성
- 생성 결과 저장 및 이력 관리
- 복사 및 재편집 기능 제공

<img width="1032" height="521" alt="image" src="https://github.com/user-attachments/assets/7cae30b1-d9c2-4ebf-b005-c7ada394a76c" />
<img width="1030" height="527" alt="image" src="https://github.com/user-attachments/assets/9daf14e9-b4db-4991-9438-0945e0af6a96" />
<img width="1031" height="527" alt="image" src="https://github.com/user-attachments/assets/7201c7d6-ae91-454d-ba08-b178e15e896c" />

### 📂 자소서 생성 결과 관리

생성된 자소서는 자동 저장되며  
목록에서 언제든지 다시 확인할 수 있습니다.

<img width="1028" height="518" alt="image" src="https://github.com/user-attachments/assets/26b7e9f5-fa3f-42a6-88d7-d2aa2ef52e01" />

---

## 🔔 실시간 알림 시스템 (PWA)

- Firebase Cloud Messaging 기반 푸시 알림
- Service Worker 기반 PWA 지원
- Context-Aware Notification 구현

사용자가 이미 해당 화면을 보고 있다면  
알림을 자동으로 차단하여 UX를 개선했습니다.

푸시 알림 수신 화면 (우측 하단)
<img width="1035" height="526" alt="image" src="https://github.com/user-attachments/assets/bcb3f30f-fe94-4346-b9b6-b9bf07f1c2fb" />

푸시 알림 수신 화면
<img width="1028" height="524" alt="image" src="https://github.com/user-attachments/assets/9e5463bb-0659-4369-a87d-46f6e51548c6" />

<table>
  <tr>
    <td>  <img width="266" height="580" alt="image" src="https://github.com/user-attachments/assets/af5cbe76-ae47-4206-926c-c1ddfd392ce5" width="100%" />  </td>
    <td>  <img width="267" height="586" alt="image" src="https://github.com/user-attachments/assets/7ca0fd5a-c0c3-4ed9-b3d9-b6482c67539e" width="100%" />  </td>
  </tr>
</table>

---

## 🗺️ 입시 지도 (AI 기반 대학 추천 시스템)

사용자의 등급 입력을 기반으로  
지원 가능 대학을 지도 위에 시각화하는 기능입니다.

### 주요 기능

- 국어 / 수학 등급 기반 필터링
- 합격 가능성 분석
- 지역 재검색 기능
- 일반 지도 / 위성 지도 전환
- 학과별 평균 합격 컷 정보 제공

<img width="1037" height="527" alt="image" src="https://github.com/user-attachments/assets/c7824842-70e5-44b3-bf69-bc73b22f5990" />
<img width="1041" height="523" alt="image" src="https://github.com/user-attachments/assets/0d0e74ab-cf55-43d7-8188-c7441e6d5af7" />

---

## 🎨 실시간 협업 그림판

- WebSocket 기반 실시간 통신
- STOMP 프로토콜을 활용한 메시지 라우팅
- 서버 브로드캐스트 방식으로 동기화 유지

여러 사용자가 동시에 그림을 그려도  
모든 화면이 실시간으로 동일하게 유지됩니다.

실시간 협업 그림판 (멘토 화면)
<img width="1037" height="539" alt="image" src="https://github.com/user-attachments/assets/c78d0bce-b3f4-4ea4-8501-23e5d2b12f91" />

실시간 협업 그림판 (우측 상단: 멘토 화면, 하단: 멘티 화면)
<img width="1040" height="565" alt="image" src="https://github.com/user-attachments/assets/d2c05c6d-d7b2-4769-bc15-e07c0af4fd8c" />

---

## 💳 결제 시스템

- Toss Payments 연동
- 서버에서 주문 번호 생성
- DB 기준 금액 검증
- 위·변조 및 이중 결제 방지

요금제 선택   
<img width="752" height="686" alt="image" src="https://github.com/user-attachments/assets/adfcb424-c55a-457f-9523-705c3dbbeb8c" />

결제   
<img width="1264" height="668" alt="image" src="https://github.com/user-attachments/assets/10104d40-ffbc-4a44-8cfb-ec68af3169a3" />
<img width="1279" height="668" alt="image" src="https://github.com/user-attachments/assets/7c41bb48-a762-4627-9349-fcd95b3dbfd8" />

---


## 🧱 사이트 개발 기술 스택

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

## 🏗️ AI 데이터 사이트 기술 스택

### Frontend
- React 기반 채팅 인터페이스
- 비동기 API 통신 (fetch / axios)
- 실시간 UX 최적화 설계

### Backend
- Spring Boot REST API
- QueryDSL 기반 대학 데이터 조회
- Python AI 서버와 HTTP 통신

### AI 분석 서버
- Python 기반 데이터 분석
- 선형 보간 알고리즘 적용
- 통계 기반 합격선 추정 로직 구현

---

## 💡 주요 특징

- 단순 GPT 응답이 아닌 **실제 입시 데이터 기반 분석**
- 학년도 기준 필터링 가능
- 모집군 / 경쟁률 / 70% Cut 기준 전략 제시
- 데이터 변경 시 자동 반영 구조

---

## 📈 확장 가능성

- 수시 전형 데이터 확장
- 내신 기반 합격 확률 예측 모델 적용
- 개인 성적 입력 기반 맞춤 전략 제공
- 머신러닝 모델을 통한 합격 가능성 예측 고도화

