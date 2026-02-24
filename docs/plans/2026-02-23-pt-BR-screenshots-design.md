# Design: pt-BR Play Store Screenshot Generator

**Date:** 2026-02-23
**Status:** Approved

## Goal

Generate 8 high-res Play Store assets for the Brazilian Portuguese market:
- 7 phone screenshots (1080×1920px portrait)
- 1 feature graphic (1024×500px landscape)

## Approach

Single Node.js script (`scripts/generate-screenshots-pt-BR.mjs`) using Puppeteer to render inline HTML/CSS templates. Same pattern as `chrome-metrics-changer/scripts/generate-screenshots.mjs`.

## App UI Mockup

Faithful HTML/SVG recreation of `BanjoStringCanvas.kt`:
- Background: dark mahogany vertical gradient (`#1A1210 → #141010`)
- Vignette: radial black overlay
- Nut bar (top) + Bridge bar (bottom): `#3D322A`
- Fret lines: `#2E2420`
- 4 vertical strings:
  - D3: idle `#8C7161`, active `#D4956A` (warm terracotta)
  - G3: idle `#6B8490`, active `#5AAFCB` (steel blue)
  - B3: idle `#8C8062`, active `#CBA55A` (warm gold)
  - D4: idle `#907466`, active `#E07850` (orange-red)
- Labels: D3/G3/B3/D4 with Portuguese solfège (Ré/Sol/Si/Ré)

## Typography

Montserrat via Google Fonts `<link>`:
- Primary headlines: ExtraBold 800
- Secondary text: SemiBold 600
- Body: Medium 500

## Color Palette

| Role | Hex |
|---|---|
| Background (dark) | `#1A0A00` |
| Background (gradient end) | `#3D1A00` |
| Primary text | `#FFFFFF` |
| Accent / gold | `#F5C842` |
| Instrument amber | `#C87830` |

## Assets

| # | Filename | Dims | Content |
|---|---|---|---|
| 0 | `feature-graphic-pt-BR.png` | 1024×500 | Cavaquinho SVG + "Afinador de Cavaquinho e Banjo" on mahogany gradient |
| 1 | `ss01-hero-pt-BR.png` | 1080×1920 | "Toque. Ouça. Afine." + app UI (G3 glowing) + sound waves |
| 2 | `ss02-equivalence-pt-BR.png` | 1080×1920 | Cavaquinho + 4-string banjo SVGs + "Mesma afinação Ré-Sol-Si-Ré" |
| 3 | `ss03-sem-microfone-pt-BR.png` | 1080×1920 | Crossed-out mic SVG + "Sem microfone. Funciona no meio da roda." |
| 4 | `ss04-roda-pt-BR.png` | 1080×1920 | Silhouette musician circle + "Chegue afinado na roda. Sempre." |
| 5 | `ss05-offline-pt-BR.png` | 1080×1920 | Airplane mode indicator + "Funciona offline." + boteco context |
| 6 | `ss06-simplicity-pt-BR.png` | 1080×1920 | 4 strings large + "4 botões. 4 cordas. Só apertar e afinar." |
| 7 | `ss07-genres-pt-BR.png` | 1080×1920 | 4 genre colour zones (samba/pagode/choro/forró) + app UI |

## Output Paths

- Screenshots → `fastlane/metadata/android/pt-BR/images/phoneScreenshots/`
- Feature graphic → `fastlane/metadata/android/pt-BR/images/featureGraphic/`

## Script Dependencies

Uses `puppeteer` (already available via `npx puppeteer@24.37.5`). No `package.json` changes needed — script invoked with `node --experimental-vm-modules` or plain `node` since it's ESM.
