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
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        // 📍 모든 동적 임포트를 인라인으로 합쳐서 진짜 '파일 1개'로 만듦
        inlineDynamicImports: true, 
        manualChunks: undefined,
        entryFileNames: "react-main.js",
        assetFileNames: (assetInfo) => {
          if (assetInfo.name && assetInfo.name.endsWith('.css')) return 'react-main.css';
          return '[name].[ext]';
        },
      },
    },
  },
});
