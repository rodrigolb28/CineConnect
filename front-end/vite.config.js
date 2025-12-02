import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

import fs from 'fs';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
    plugins: [tailwindcss(),
    react()],
    server: {
        allowedHosts: ['cineconnect.com'],
        host: true,
        https: {
            key: fs.readFileSync(path.resolve(__dirname, '../cineconnect.com-key.pem')),
            cert: fs.readFileSync(path.resolve(__dirname, '../cineconnect.com.pem')),
        },
        strictPort: false,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            }
        }
    },
});
