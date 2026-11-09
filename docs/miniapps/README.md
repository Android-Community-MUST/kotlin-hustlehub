# HustleHub Mini-Apps & Super App Architecture Guide

Welcome to the engineering documentation for the **HustleHub Super App & Mini-App Platform**.

This documentation suite serves as the single source of truth for current and future engineers on how HustleHub transitions from a monolithic campus marketplace into an extensible **Super App host platform** (similar to the architectural model used by the **Safaricom M-PESA Super App**, **WeChat**, and **Grab**).

---

## 📚 Documentation Index

| Document | Purpose | Target Audience |
| :--- | :--- | :--- |
| **[01. Options Evaluation & Strategy](file:///home/korryr/StudioProjects/kotlin-hustlehub/docs/miniapps/01_OPTIONS_EVALUATION.md)** | Deep-dive analysis of the 3 mobile mini-app paradigms and why **Option A (Web-Sandbox Runtime)** was selected for HustleHub. | All Engineers, Architects, Technical Leads |
| **[02. Why Multi-Module Architecture](file:///home/korryr/StudioProjects/kotlin-hustlehub/docs/miniapps/02_MULTIMODULE_FOUNDATION.md)** | Explanation of why the current single `:app` monolith must be decomposed, how multi-module works, and the target Gradle module graph. | Android Engineers, Build Engineers |
| **[03. Runtime, Bridge & Security Spec](file:///home/korryr/StudioProjects/kotlin-hustlehub/docs/miniapps/03_RUNTIME_BRIDGE_AND_SECURITY.md)** | Detailed technical specification of the WebView container, JavaScript-to-Native Bridge (`HustleBridge`), M-Pesa mediation, and Google Play compliance. | Android Engineers, Security Engineers, Mini-App Developers |
| **[04. Execution Roadmap & Milestones](file:///home/korryr/StudioProjects/kotlin-hustlehub/docs/miniapps/04_EXECUTION_ROADMAP.md)** | Step-by-step implementation milestones: from monolith decoupling to launching the first pilot campus mini-app. | Project Leads, Core Contributors |

---

## 🎯 Executive Summary & Mental Model

### What is a Super App?
A Super App is **not** an app that simply contains dozens of internal features compiled into one big APK. 

A Super App is an **Operating Environment (Host Shell)**. It acts like a lightweight OS on top of Android that provides:
1. **Verified Campus Identity** (Meru University `@must.ac.ke` Google / Firebase Auth).
2. **Standardized Payment Rail** (Safaricom Daraja M-Pesa STK push).
3. **Hardware & System Capabilities** (Camera, Location, Push Notifications).
4. **Isolated Sandboxes** where third-party sub-applications (**Mini-Apps**) run independently.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          HUSTLEHUB SUPER APP                           │
│                          (Native Host Shell)                           │
│                                                                        │
│   [Core Auth]       [M-Pesa Rail]       [Navigation 3]    [Telemetry]  │
│        │                  │                   │                │       │
│   ═════╪══════════════════╪═══════════════════╪════════════════╪═════  │
│        │            HustleBridge RPC Contract │                │       │
│   ┌────▼──────────┐ ┌─────▼─────────┐ ┌───────▼────────┐ ┌─────▼────┐  │
│   │   Mini-App    │ │   Mini-App    │ │    Mini-App    │ │ Mini-App │  │
│   │  MUST Events  │ │ Campus Mess   │ │  Past Papers   │ │ Printing │  │
│   │  (Web Bundle) │ │ (Web Bundle)  │ │  (Web Bundle)  │ │ (Web Bn) │  │
│   └───────────────┘ └───────────────┘ └────────────────┘ └──────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Why Meru University (MUST) Needs This
In a campus ecosystem, many student clubs, department labs, cafeterias, and student innovators build useful websites and services. However:
* Most of these websites suffer from **low discovery and high drop-off**.
* Students hate creating separate passwords and accounts for every campus website.
* Students want to pay with **M-Pesa in one click** without manually copying till numbers.
* Publishing native Android apps on the Google Play Store is expensive ($25 fee, 14-day 20-tester requirement, strict verification).

By turning HustleHub into a **Super App Host**, any student developer at MUST can build a lightweight web project, plug in the `hustlehub-sdk.js`, and instantly tap into HustleHub’s verified student userbase and frictionless M-Pesa checkout.
