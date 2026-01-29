import { StrictMode } from 'react' // 개발 모드에서만 사용하는 환경 설정 도구
import { createRoot } from 'react-dom/client' // 동시성 렌더링 렌더링 과정 간 우선순위를 주어 사용자 경험 극대화
import './index.css'
import App from './App.tsx'

// 동시성 렌더링 기반 마련(무거운 데이터 처리를 앱의 끊김 없이 제공하기 위해)
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
