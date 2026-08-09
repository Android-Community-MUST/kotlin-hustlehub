# 💳 M-Pesa Daraja Integration & Setup Guide (Lane 8)

This guide documents the architecture, configuration, security, and step-by-step setup for **M-Pesa STK Push Payments & Subscriptions** in HustleHub.

---

## 🎯 Architecture Overview

```
┌─────────────────┐       1. STK Push Request      ┌─────────────────────┐
│                 ├───────────────────────────────►│                     │
│  Android Client │                                │  Spring Boot        │
│  (Kotlin App)   │◄───────────────────────────────┤  Backend            │
└────────┬────────┘       2. Return Checkout ID    └──────────┬──────────┘
         │                                                    │
         │ 3. Poll GET /payments/status/{checkoutId}          │ 4. POST STK Push
         ▼                                                    ▼
┌─────────────────┐                                ┌─────────────────────┐
│  M-Pesa SIM     │◄───────────────────────────────┤ Safaricom Daraja    │
│  PIN Popup      │    User Enters M-Pesa PIN      │ Sandbox / Prod API  │
└────────┬────────┘                                └──────────┬──────────┘
         │                                                    │
         └────────────────────────────────────────────────────┘
                       5. Webhook Callback (POST)
```

---

## 🔑 1. Obtaining Safaricom Daraja Credentials

1. Go to the [Safaricom Developer Portal](https://developer.safaricom.co.ke).
2. Create an account or log in.
3. Click **My Apps** → **Create New App**.
4. Check the box for **Lipa Na M-Pesa Sandbox**.
5. Once created, copy your:
   - **Consumer Key**
   - **Consumer Secret**

---

## ⚙️ 2. Backend Configuration (`hustlehub-backend`)

### A. Gitignored Local Development File (`application-local.yml`)
Create a file named `src/main/resources/application-local.yml` in `hustlehub-backend`. This file is listed in `.gitignore` and **will never be pushed to GitHub**.

```yaml
# src/main/resources/application-local.yml (GITIGNORED)
hustlehub:
  mpesa:
    consumer-key: "YOUR_SAFARICOM_CONSUMER_KEY"
    consumer-secret: "YOUR_SAFARICOM_CONSUMER_SECRET"
    business-short-code: "174379"
    passkey: "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    callback-url: "http://YOUR_LOCAL_IP:8080/api/v1/payments/mpesa/callback"
    base-url: "https://sandbox.safaricom.co.ke"
```

> **How to find `YOUR_LOCAL_IP` on Linux**:
> Run `ip route get 1.1.1.1 | awk '{print $7}'` in terminal.

---

### B. Production Environment Variables (Cloud / CI/CD)
In production (Cloud Run / AWS / Heroku), do not use `application-local.yml`. Export environment variables instead:

```bash
export MPESA_CONSUMER_KEY="your_production_key"
export MPESA_CONSUMER_SECRET="your_production_secret"
export MPESA_SHORTCODE="your_shortcode"
export MPESA_PASSKEY="your_production_passkey"
export MPESA_CALLBACK_URL="https://api.hustlehub.app/api/v1/payments/mpesa/callback"
export MPESA_BASE_URL="https://api.safaricom.co.ke"
```

---

## 📱 3. Mobile Client Flow (`kotlin-hustlehub`)

### Phone Number Normalization
The Android app normalizes all user input formats into Safaricom's required `2547XXXXXXXX` or `2541XXXXXXXX` 12-digit string:
- `0712345678` → `254712345678`
- `0123456789` → `254123456789`
- `+254712345678` → `254712345678`

### Real-Time Status Polling
While the screen displays *"Waiting for M-Pesa PIN..."*, the app polls `GET /api/v1/payments/status/{checkoutRequestId}` every **3 seconds** for up to **10 attempts** (30 seconds total).
- On **`COMPLETED`**: Unlocks Pro badge immediately and saves expiration date in DataStore `UserPreferences`.
- On **`FAILED`**: Shows error message with retry button.
- On **`TIMEOUT`**: Offers fallback instructions.

---

## 🧪 4. Testing M-Pesa STK Push in Sandbox

When testing with a physical device or emulator on Safaricom Sandbox:
1. Use test phone number **`254708374149`** or **`254711223344`**.
2. If testing with a real phone connected over local Wi-Fi, ensure your phone and laptop are on the **same Wi-Fi network**.
3. Verify that `callback-url` in `application-local.yml` uses your laptop's local IP (e.g. `http://10.238.26.212:8080/api/v1/payments/mpesa/callback`).

---

## 🔒 5. Security & Best Practices Checklist

- [x] **Never commit secrets**: `application-local.yml` is in `.gitignore`.
- [x] **No client-side spoofing**: `AccountReference` and transaction amounts are calculated server-side.
- [x] **Idempotency & Race Condition Guarding**: Every STK push attempt generates a unique `checkoutRequestId` in `payment_transactions` database table.
