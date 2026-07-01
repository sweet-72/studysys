import { defineConfig } from '@rsbuild/core';
import { pluginVue } from '@rsbuild/plugin-vue';
import path from 'path';

export default defineConfig({
  source: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
    entry: {
      index: './src/main.ts',
    },
  },
  plugins: [pluginVue()],
  html: {
    mountId: 'app',
    title: 'AI 赋能的教育系统',
    favicon: './public/logo.svg',
  },
  tools: {
    bundlerChain: (chain) => {
      const env = process.env;
      const envKeys = Object.keys(env).filter((key) => key.startsWith('VITE_'));

      envKeys.forEach((key) => {
        const definePluginArgs = chain.plugin('define').get('args');
        if (definePluginArgs && definePluginArgs[0]) {
          definePluginArgs[0][`import.meta.env.${key}`] = JSON.stringify(env[key]);
        }
      });
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8101',
        changeOrigin: true,
        secure: false,
      },
    },
    port: 8001,
    host: 'localhost',
    strictPort: true,
  },
});

