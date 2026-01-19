import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: "../resources/static/js",
    emptyOutDir: false, // 기존 static/js 파일들이 지워지지 않도록 설정
    rollupOptions: {
      output: {
        entryFileNames: "react-main.js",
        chunkFileNames: "react-main-[hash].js",
        assetFileNames: "react-main.[ext]",
      },
    },
  },
});
