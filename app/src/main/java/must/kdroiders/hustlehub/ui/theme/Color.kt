package must.kdroiders.hustlehub.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
// HustleHub Dark Palette
// Your teammate can update these values freely.
// ─────────────────────────────────────────────

// Backgrounds & Surfaces
val HustleDarkBackground = Color(0xFF0D0D1A)       // Deep navy — main background
val HustleDarkSurface = Color(0xFF1A1A2E)           // Slightly lighter — card surface
val HustleDarkSurfaceVariant = Color(0xFF252540)    // Elevated cards & inputs
val HustleDarkSurfaceBright = Color(0xFF2E2E4A)     // Highlighted containers

// Primary accent (purple)
val HustlePrimary = Color(0xFF7C4DFF)               // Vibrant purple — buttons, toggles
val HustlePrimaryVariant = Color(0xFF9C7CFF)        // Lighter purple — gradient end
val HustleOnPrimary = Color(0xFFFFFFFF)             // Text on primary

// Secondary / Tertiary
val HustleSecondary = Color(0xFF4A3B7C)             // Muted purple — badges bg
val HustleTertiary = Color(0xFF6C63FF)              // Indigo accent

// Text & content
val HustleOnBackground = Color(0xFFFFFFFF)          // Primary text on dark bg
val HustleOnSurface = Color(0xFFFFFFFF)             // Primary text on surfaces
val HustleOnSurfaceVariant = Color(0xFF9E9EB8)      // Secondary / subtitle text
val HustleOutline = Color(0xFF3A3A5C)               // Borders & dividers
val HustleOutlineVariant = Color(0xFF2A2A45)        // Subtle borders

// Status colors
val HustleActiveGreen = Color(0xFF4CAF50)           // Active toggle / online
val HustleOfflineGray = Color(0xFF6E6E8A)           // Offline / disabled
val HustleError = Color(0xFFCF6679)                 // Error states
val HustleWarningAmber = Color(0xFFFFB300)          // Badges (star, etc.)

// Badge chip backgrounds
val HustleBadgeGold = Color(0xFF2E2510)             // Gold badge bg
val HustleBadgeGreen = Color(0xFF102E1A)            // Green badge bg
val HustleBadgeBlue = Color(0xFF101A2E)             // Blue badge bg

// Legacy aliases (kept for backward compat)
val Purple80 = HustlePrimaryVariant
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = HustlePrimary
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)