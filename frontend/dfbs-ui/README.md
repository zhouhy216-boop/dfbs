# DFBS UI

Frontend for DFBS (React 18 + Vite + TypeScript + Ant Design 5 + ProComponents).

## Setup

```bash
cd frontend/dfbs-ui && npm install && npm run dev
```

- Dev server: http://localhost:5173
- API proxy: `/api` → http://localhost:8080 (ensure backend is running on port 8080)

## Pages

- **Login** – Username/Password; calls `POST /api/auth/login` (or demo mode if 404)
- **Customer** – ProTable list (keyword search), detail drawer
- **Quote** – My-pending list, detail + items, export (xlsx)
- **Shipment** – ProTable list (status filter), detail, export ticket/receipt
- **Finance** – Payments by Quote ID, detail drawer

## Structure

- `src/utils/request.ts` – Axios with Bearer token, 401 → login
- `src/stores/useAuthStore.ts` – token, userInfo, login, logout
- `src/layouts/BasicLayout.tsx` – ProLayout, AuthGuard
- `src/components/Access.tsx` – Permission-based visibility
- `src/components/AttachmentList.tsx` – File URLs list (download/preview)
