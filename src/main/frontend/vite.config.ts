import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
// import { VitePWA } from "vite-plugin-pwa";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // VitePWA({
    //   registerType: "autoUpdate",
    //   includeAssets: ["favicon.ico", "apple-touch-icon.png", "mask-lockup.svg"],
    //   manifest: {
    //     name: "StudyLink (스터디링크)",
    //     short_name: "StudyLink",
    //     description: "AI 기반 대학 입시 컨설팅 솔루션",
    //     theme_color: "#0969da",
    //     icons: [
    //       {
    //         src: "pwa-192x192.png",
    //         sizes: "192x192",
    //         type: "image/png",
    //       },
    //       {
    //         src: "pwa-512x512.png",
    //         sizes: "512x512",
    //         type: "image/png",
    //       },
    //       {
    //         src: "pwa-512x512.png",
    //         sizes: "512x512",
    //         type: "image/png",
    //         purpose: "any maskable",
    //       },
    //     ],
    //   },
    // }),
  ],
  build: {
    outDir: "../resources/static/js",
    emptyOutDir: false, 
    cssCodeSplit: false, // CSS도 하나로 통합
    rollupOptions: {
      output: {
        // 단일 파일로 합치기 위한 설정 (manualChunks 비활성화 효과)
        manualChunks: undefined,
        entryFileNames: "react-main.js",
        chunkFileNames: "react-main-chunk.js", // 혹시 조각이 생겨도 이름을 고정
        assetFileNames: (assetInfo) => {
          if (assetInfo.name && assetInfo.name.endsWith('.css')) return 'react-main.css';
          return '[name].[ext]';
        },
      },
    },
  },
});
