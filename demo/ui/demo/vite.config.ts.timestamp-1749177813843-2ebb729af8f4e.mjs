// vite.config.ts
import { defineConfig } from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/vite@4.4.6_@types+node@20.4.4_sass@1.64.1/node_modules/vite/dist/node/index.js";
import vue from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/@vitejs+plugin-vue@4.2.3_vite@4.4.6_vue@3.3.4/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import AutoImport from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/unplugin-auto-import@0.17.5_rollup@2.79.1/node_modules/unplugin-auto-import/dist/vite.js";
import Components from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/unplugin-vue-components@0.26.0_rollup@2.79.1_vue@3.3.4/node_modules/unplugin-vue-components/dist/vite.js";
import { ElementPlusResolver } from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/unplugin-vue-components@0.26.0_rollup@2.79.1_vue@3.3.4/node_modules/unplugin-vue-components/dist/resolvers.js";
import { UndrawUiResolver } from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/undraw-ui@1.2.3/node_modules/undraw-ui/es/resolvers/index.js";
import UnoCSS from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/unocss@0.53.6_postcss@8.4.27_rollup@2.79.1_vite@4.4.6/node_modules/unocss/dist/vite.mjs";
import transformerDirectives from "file:///D:/code/Java/cool/demo/ui/demo/node_modules/.pnpm/@unocss+transformer-directives@0.53.6/node_modules/@unocss/transformer-directives/dist/index.mjs";
import { resolve } from "path";

// package.json
var package_default = {
  name: "test",
  private: true,
  version: "0.0.0",
  type: "module",
  scripts: {
    dev: "vite",
    build: "vue-tsc && vite build",
    preview: "vite preview",
    format: "prettier --write **/*.{ts,js,json,vue}",
    commit: "git add . && git-cz && git push"
  },
  config: {
    commitizen: {
      path: "./node_modules/cz-git"
    }
  },
  dependencies: {
    "@element-plus/icons-vue": "^2.1.0",
    axios: "1.9.0",
    "element-plus": "2.6.0",
    nprogress: "^0.2.0",
    pinia: "^2.1.4",
    "undraw-ui": "^1.2.3",
    vue: "^3.3.4",
    "vue-router": "^4.2.4"
  },
  devDependencies: {
    "@types/nprogress": "^0.2.0",
    "@typescript-eslint/eslint-plugin": "^5.18.0",
    "@typescript-eslint/parser": "^5.18.0",
    "@unocss/transformer-directives": "^0.53.4",
    "@vitejs/plugin-vue": "^4.2.3",
    commitizen: "^4.2.4",
    "cz-git": "^1.3.5",
    eslint: "^8.12.0",
    "eslint-config-prettier": "^8.5.0",
    "eslint-plugin-prettier": "^4.0.0",
    "eslint-plugin-vue": "^8.6.0",
    prettier: "^2.6.2",
    rollup: "2.79.1",
    sass: "^1.49.7",
    typescript: "^5.0.2",
    unocss: "^0.53.4",
    "unplugin-auto-import": "^0.17.5",
    "unplugin-vue-components": "^0.26.0",
    vite: "^4.4.5",
    "vue-tsc": "^1.8.5"
  }
};

// vite.config.ts
var __vite_injected_original_dirname = "D:\\code\\Java\\cool\\demo\\ui\\demo";
var vite_config_default = defineConfig({
  plugins: [
    AutoImport({
      resolvers: [ElementPlusResolver()]
    }),
    Components({
      resolvers: [ElementPlusResolver(), UndrawUiResolver]
    }),
    UnoCSS({
      transformers: [transformerDirectives()]
    }),
    vue()
  ],
  resolve: {
    alias: [
      {
        find: "@",
        replacement: resolve("src")
      }
    ]
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "src/assets/styles/index.scss" as *;`
      }
    }
  },
  server: {
    host: "0.0.0.0",
    port: 3004,
    proxy: {
      // http://localhost:7032/file/download?filePath=/upload/test2.xlsx
      "/api": {
        target: "http://localhost:7032",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, "")
      }
    }
  },
  // 打包
  build: {
    // 打包输出目录
    outDir: package_default.name,
    minify: true,
    //是否进行压缩
    chunkSizeWarningLimit: 1e3,
    // 设置警告阈值为 1000 kb
    rollupOptions: {
      input: {
        main: resolve(__vite_injected_original_dirname, "index.html")
      },
      output: {
        entryFileNames: `assets/js/[name]-[hash].js`,
        chunkFileNames: `assets/js/[name]-[hash].js`,
        assetFileNames: `assets/css/[name]-[hash].[ext]`,
        banner: `/*! Version: v${package_default.version} */`,
        manualChunks(id) {
          if (id.includes("node_modules/.pnpm")) {
            return id.split("node_modules/.pnpm")[1].split("/")[3];
          }
          if (id.includes("iconpark.js")) {
            return "iconpark";
          }
          if (id.includes(".svg")) {
            return "svg/svg";
          }
        }
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiLCAicGFja2FnZS5qc29uIl0sCiAgInNvdXJjZXNDb250ZW50IjogWyJjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZGlybmFtZSA9IFwiRDpcXFxcY29kZVxcXFxKYXZhXFxcXGNvb2xcXFxcZGVtb1xcXFx1aVxcXFxkZW1vXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ZpbGVuYW1lID0gXCJEOlxcXFxjb2RlXFxcXEphdmFcXFxcY29vbFxcXFxkZW1vXFxcXHVpXFxcXGRlbW9cXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0Q6L2NvZGUvSmF2YS9jb29sL2RlbW8vdWkvZGVtby92aXRlLmNvbmZpZy50c1wiO2ltcG9ydCB7IGRlZmluZUNvbmZpZyB9IGZyb20gJ3ZpdGUnXHJcbmltcG9ydCB2dWUgZnJvbSAnQHZpdGVqcy9wbHVnaW4tdnVlJ1xyXG5pbXBvcnQgQXV0b0ltcG9ydCBmcm9tICd1bnBsdWdpbi1hdXRvLWltcG9ydC92aXRlJ1xyXG5pbXBvcnQgQ29tcG9uZW50cyBmcm9tICd1bnBsdWdpbi12dWUtY29tcG9uZW50cy92aXRlJ1xyXG5pbXBvcnQgeyBFbGVtZW50UGx1c1Jlc29sdmVyIH0gZnJvbSAndW5wbHVnaW4tdnVlLWNvbXBvbmVudHMvcmVzb2x2ZXJzJ1xyXG5pbXBvcnQgeyBVbmRyYXdVaVJlc29sdmVyIH0gZnJvbSAndW5kcmF3LXVpL2VzL3Jlc29sdmVycydcclxuaW1wb3J0IFVub0NTUyBmcm9tICd1bm9jc3Mvdml0ZSdcclxuaW1wb3J0IHRyYW5zZm9ybWVyRGlyZWN0aXZlcyBmcm9tICdAdW5vY3NzL3RyYW5zZm9ybWVyLWRpcmVjdGl2ZXMnXHJcbmltcG9ydCB7IHJlc29sdmUgfSBmcm9tICdwYXRoJ1xyXG5pbXBvcnQgcGtnIGZyb20gJy4vcGFja2FnZS5qc29uJ1xyXG5cclxuLy8gaHR0cHM6Ly92aXRlanMuZGV2L2NvbmZpZy9cclxuZXhwb3J0IGRlZmF1bHQgZGVmaW5lQ29uZmlnKHtcclxuICBwbHVnaW5zOiBbXHJcbiAgICBBdXRvSW1wb3J0KHtcclxuICAgICAgcmVzb2x2ZXJzOiBbRWxlbWVudFBsdXNSZXNvbHZlcigpXVxyXG4gICAgfSksXHJcbiAgICBDb21wb25lbnRzKHtcclxuICAgICAgcmVzb2x2ZXJzOiBbRWxlbWVudFBsdXNSZXNvbHZlcigpLCBVbmRyYXdVaVJlc29sdmVyXVxyXG4gICAgfSksXHJcbiAgICBVbm9DU1Moe1xyXG4gICAgICB0cmFuc2Zvcm1lcnM6IFt0cmFuc2Zvcm1lckRpcmVjdGl2ZXMoKV1cclxuICAgIH0pLFxyXG4gICAgdnVlKClcclxuICBdLFxyXG4gIHJlc29sdmU6IHtcclxuICAgIGFsaWFzOiBbXHJcbiAgICAgIHtcclxuICAgICAgICBmaW5kOiAnQCcsXHJcbiAgICAgICAgcmVwbGFjZW1lbnQ6IHJlc29sdmUoJ3NyYycpXHJcbiAgICAgIH1cclxuICAgIF1cclxuICB9LFxyXG4gIGNzczoge1xyXG4gICAgcHJlcHJvY2Vzc29yT3B0aW9uczoge1xyXG4gICAgICBzY3NzOiB7XHJcbiAgICAgICAgYWRkaXRpb25hbERhdGE6IGBAdXNlIFwic3JjL2Fzc2V0cy9zdHlsZXMvaW5kZXguc2Nzc1wiIGFzICo7YFxyXG4gICAgICB9XHJcbiAgICB9XHJcbiAgfSxcclxuICBzZXJ2ZXI6IHtcclxuICAgIGhvc3Q6ICcwLjAuMC4wJyxcclxuICAgIHBvcnQ6IDMwMDQsXHJcbiAgICBwcm94eToge1xyXG4gICAgICAvLyBodHRwOi8vbG9jYWxob3N0OjcwMzIvZmlsZS9kb3dubG9hZD9maWxlUGF0aD0vdXBsb2FkL3Rlc3QyLnhsc3hcclxuICAgICAgJy9hcGknOiB7XHJcbiAgICAgICAgdGFyZ2V0OiAnaHR0cDovL2xvY2FsaG9zdDo3MDMyJyxcclxuICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXHJcbiAgICAgICAgcmV3cml0ZTogcGF0aCA9PiBwYXRoLnJlcGxhY2UoL15cXC9hcGkvLCAnJylcclxuICAgICAgfVxyXG4gICAgfVxyXG4gIH0sXHJcbiAgLy8gXHU2MjUzXHU1MzA1XHJcbiAgYnVpbGQ6IHtcclxuICAgIC8vIFx1NjI1M1x1NTMwNVx1OEY5M1x1NTFGQVx1NzZFRVx1NUY1NVxyXG4gICAgb3V0RGlyOiBwa2cubmFtZSxcclxuICAgIG1pbmlmeTogdHJ1ZSwgLy9cdTY2MkZcdTU0MjZcdThGREJcdTg4NENcdTUzOEJcdTdGMjlcclxuICAgIGNodW5rU2l6ZVdhcm5pbmdMaW1pdDogMTAwMCwgLy8gXHU4QkJFXHU3RjZFXHU4QjY2XHU1NDRBXHU5NjA4XHU1MDNDXHU0RTNBIDEwMDAga2JcclxuICAgIHJvbGx1cE9wdGlvbnM6IHtcclxuICAgICAgaW5wdXQ6IHtcclxuICAgICAgICBtYWluOiByZXNvbHZlKF9fZGlybmFtZSwgJ2luZGV4Lmh0bWwnKVxyXG4gICAgICB9LFxyXG4gICAgICBvdXRwdXQ6IHtcclxuICAgICAgICBlbnRyeUZpbGVOYW1lczogYGFzc2V0cy9qcy9bbmFtZV0tW2hhc2hdLmpzYCxcclxuICAgICAgICBjaHVua0ZpbGVOYW1lczogYGFzc2V0cy9qcy9bbmFtZV0tW2hhc2hdLmpzYCxcclxuICAgICAgICBhc3NldEZpbGVOYW1lczogYGFzc2V0cy9jc3MvW25hbWVdLVtoYXNoXS5bZXh0XWAsXHJcbiAgICAgICAgYmFubmVyOiBgLyohIFZlcnNpb246IHYke3BrZy52ZXJzaW9ufSAqL2AsXHJcbiAgICAgICAgbWFudWFsQ2h1bmtzKGlkKSB7XHJcbiAgICAgICAgICAvLyBcdTY3MDBcdTVDMEZcdTUzMTZcdTYyQzZcdTUyMDZcdTUzMDVcclxuICAgICAgICAgIGlmIChpZC5pbmNsdWRlcygnbm9kZV9tb2R1bGVzLy5wbnBtJykpIHtcclxuICAgICAgICAgICAgcmV0dXJuIGlkLnNwbGl0KCdub2RlX21vZHVsZXMvLnBucG0nKVsxXS5zcGxpdCgnLycpWzNdXHJcbiAgICAgICAgICB9XHJcbiAgICAgICAgICBpZiAoaWQuaW5jbHVkZXMoJ2ljb25wYXJrLmpzJykpIHtcclxuICAgICAgICAgICAgLy8gXHU5NzAwXHU4OTgxXHU1MzU1XHU3MkVDXHU1MjA2XHU1MjcyXHU5MEEzXHU0RTlCXHU4RDQ0XHU2RTkwIFx1NUMzMVx1NTE5OVx1NTIyNFx1NjVBRFx1OTAzQlx1OEY5MVx1NUMzMVx1ODg0Q1xyXG4gICAgICAgICAgICByZXR1cm4gJ2ljb25wYXJrJ1xyXG4gICAgICAgICAgfVxyXG4gICAgICAgICAgaWYgKGlkLmluY2x1ZGVzKCcuc3ZnJykpIHtcclxuICAgICAgICAgICAgLy8gXHU5NzAwXHU4OTgxXHU1MzU1XHU3MkVDXHU1MjA2XHU1MjcyXHU5MEEzXHU0RTlCXHU4RDQ0XHU2RTkwIFx1NUMzMVx1NTE5OVx1NTIyNFx1NjVBRFx1OTAzQlx1OEY5MVx1NUMzMVx1ODg0Q1xyXG4gICAgICAgICAgICByZXR1cm4gJ3N2Zy9zdmcnXHJcbiAgICAgICAgICB9XHJcbiAgICAgICAgICAvLyBpZiAoaWQuaW5jbHVkZXMoJ3N0eWxlLmNzcycpKSB7XHJcbiAgICAgICAgICAvLyAgIC8vIFx1OTcwMFx1ODk4MVx1NTM1NVx1NzJFQ1x1NTIwNlx1NTI3Mlx1OTBBM1x1NEU5Qlx1OEQ0NFx1NkU5MCBcdTVDMzFcdTUxOTlcdTUyMjRcdTY1QURcdTkwM0JcdThGOTFcdTVDMzFcdTg4NENcclxuICAgICAgICAgIC8vICAgcmV0dXJuICdzcmMvc3R5bGUuY3NzJ1xyXG4gICAgICAgICAgLy8gfVxyXG4gICAgICAgICAgLy8gaWYgKGlkLmluY2x1ZGVzKCdIZWxsb1dvcmxkLnZ1ZScpKSB7XHJcbiAgICAgICAgICAvLyAgIC8vIFx1NTM1NVx1NzJFQ1x1NTIwNlx1NTI3MmhlbGxvIHdvcmxkLnZ1ZVx1NjU4N1x1NEVGNlxyXG4gICAgICAgICAgLy8gICByZXR1cm4gJ3NyYy9jb21wb25lbnRzL0hlbGxvV29ybGQudnVlJ1xyXG4gICAgICAgICAgLy8gfVxyXG4gICAgICAgIH1cclxuICAgICAgfVxyXG4gICAgfVxyXG4gIH1cclxufSlcclxuIiwgIntcbiAgXCJuYW1lXCI6IFwidGVzdFwiLFxuICBcInByaXZhdGVcIjogdHJ1ZSxcbiAgXCJ2ZXJzaW9uXCI6IFwiMC4wLjBcIixcbiAgXCJ0eXBlXCI6IFwibW9kdWxlXCIsXG4gIFwic2NyaXB0c1wiOiB7XG4gICAgXCJkZXZcIjogXCJ2aXRlXCIsXG4gICAgXCJidWlsZFwiOiBcInZ1ZS10c2MgJiYgdml0ZSBidWlsZFwiLFxuICAgIFwicHJldmlld1wiOiBcInZpdGUgcHJldmlld1wiLFxuICAgIFwiZm9ybWF0XCI6IFwicHJldHRpZXIgLS13cml0ZSAqKi8qLnt0cyxqcyxqc29uLHZ1ZX1cIixcbiAgICBcImNvbW1pdFwiOiBcImdpdCBhZGQgLiAmJiBnaXQtY3ogJiYgZ2l0IHB1c2hcIlxuICB9LFxuICBcImNvbmZpZ1wiOiB7XG4gICAgXCJjb21taXRpemVuXCI6IHtcbiAgICAgIFwicGF0aFwiOiBcIi4vbm9kZV9tb2R1bGVzL2N6LWdpdFwiXG4gICAgfVxuICB9LFxuICBcImRlcGVuZGVuY2llc1wiOiB7XG4gICAgXCJAZWxlbWVudC1wbHVzL2ljb25zLXZ1ZVwiOiBcIl4yLjEuMFwiLFxuICAgIFwiYXhpb3NcIjogXCIxLjkuMFwiLFxuICAgIFwiZWxlbWVudC1wbHVzXCI6IFwiMi42LjBcIixcbiAgICBcIm5wcm9ncmVzc1wiOiBcIl4wLjIuMFwiLFxuICAgIFwicGluaWFcIjogXCJeMi4xLjRcIixcbiAgICBcInVuZHJhdy11aVwiOiBcIl4xLjIuM1wiLFxuICAgIFwidnVlXCI6IFwiXjMuMy40XCIsXG4gICAgXCJ2dWUtcm91dGVyXCI6IFwiXjQuMi40XCJcbiAgfSxcbiAgXCJkZXZEZXBlbmRlbmNpZXNcIjoge1xuICAgIFwiQHR5cGVzL25wcm9ncmVzc1wiOiBcIl4wLjIuMFwiLFxuICAgIFwiQHR5cGVzY3JpcHQtZXNsaW50L2VzbGludC1wbHVnaW5cIjogXCJeNS4xOC4wXCIsXG4gICAgXCJAdHlwZXNjcmlwdC1lc2xpbnQvcGFyc2VyXCI6IFwiXjUuMTguMFwiLFxuICAgIFwiQHVub2Nzcy90cmFuc2Zvcm1lci1kaXJlY3RpdmVzXCI6IFwiXjAuNTMuNFwiLFxuICAgIFwiQHZpdGVqcy9wbHVnaW4tdnVlXCI6IFwiXjQuMi4zXCIsXG4gICAgXCJjb21taXRpemVuXCI6IFwiXjQuMi40XCIsXG4gICAgXCJjei1naXRcIjogXCJeMS4zLjVcIixcbiAgICBcImVzbGludFwiOiBcIl44LjEyLjBcIixcbiAgICBcImVzbGludC1jb25maWctcHJldHRpZXJcIjogXCJeOC41LjBcIixcbiAgICBcImVzbGludC1wbHVnaW4tcHJldHRpZXJcIjogXCJeNC4wLjBcIixcbiAgICBcImVzbGludC1wbHVnaW4tdnVlXCI6IFwiXjguNi4wXCIsXG4gICAgXCJwcmV0dGllclwiOiBcIl4yLjYuMlwiLFxuICAgIFwicm9sbHVwXCI6IFwiMi43OS4xXCIsXG4gICAgXCJzYXNzXCI6IFwiXjEuNDkuN1wiLFxuICAgIFwidHlwZXNjcmlwdFwiOiBcIl41LjAuMlwiLFxuICAgIFwidW5vY3NzXCI6IFwiXjAuNTMuNFwiLFxuICAgIFwidW5wbHVnaW4tYXV0by1pbXBvcnRcIjogXCJeMC4xNy41XCIsXG4gICAgXCJ1bnBsdWdpbi12dWUtY29tcG9uZW50c1wiOiBcIl4wLjI2LjBcIixcbiAgICBcInZpdGVcIjogXCJeNC40LjVcIixcbiAgICBcInZ1ZS10c2NcIjogXCJeMS44LjVcIlxuICB9XG59XG4iXSwKICAibWFwcGluZ3MiOiAiO0FBQTBSLFNBQVMsb0JBQW9CO0FBQ3ZULE9BQU8sU0FBUztBQUNoQixPQUFPLGdCQUFnQjtBQUN2QixPQUFPLGdCQUFnQjtBQUN2QixTQUFTLDJCQUEyQjtBQUNwQyxTQUFTLHdCQUF3QjtBQUNqQyxPQUFPLFlBQVk7QUFDbkIsT0FBTywyQkFBMkI7QUFDbEMsU0FBUyxlQUFlOzs7QUNSeEI7QUFBQSxFQUNFLE1BQVE7QUFBQSxFQUNSLFNBQVc7QUFBQSxFQUNYLFNBQVc7QUFBQSxFQUNYLE1BQVE7QUFBQSxF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
