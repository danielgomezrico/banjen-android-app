# pt-BR Play Store Screenshot Generator — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Generate 8 high-res Play Store assets (7 screenshots + 1 feature graphic) for the Brazilian Portuguese market using a Puppeteer script.

**Architecture:** Single ESM script at `scripts/generate-screenshots-pt-BR.mjs`. Each asset is an inline HTML/CSS string rendered by Puppeteer at exact pixel dimensions, then saved to the fastlane directories. The app UI (banjo strings) is an SVG component reused across all screenshots.

**Tech Stack:** Node.js ESM, Puppeteer 24, Google Fonts (Montserrat), SVG inline illustrations.

---

### Task 1: Set up Node package for scripts

**Files:**
- Create: `scripts/package.json`

**Step 1: Create minimal package.json**

```json
{
  "type": "module",
  "dependencies": {
    "puppeteer": "^24"
  }
}
```

**Step 2: Install puppeteer**

```bash
cd /Users/dan/projects/banjen/scripts && npm install
```

Expected: `node_modules/puppeteer` created, browser downloaded (~200MB).

**Step 3: Verify**

```bash
cd /Users/dan/projects/banjen/scripts && node -e "import('puppeteer').then(() => console.log('ok'))"
```

Expected: `ok`

---

### Task 2: Write the script scaffold and shared components

**Files:**
- Create: `scripts/generate-screenshots-pt-BR.mjs`

**Step 1: Write the script scaffold with shared constants and the app UI SVG**

The app UI is a faithful SVG recreation of `BanjoStringCanvas.kt`. Colors come directly from the Kotlin source.

```javascript
import puppeteer from 'puppeteer';
import { mkdirSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '..');

const PHONE_W = 1080;
const PHONE_H = 1920;
const FEATURE_W = 1024;
const FEATURE_H = 500;

const screenshotsDir = resolve(root, 'fastlane/metadata/android/pt-BR/images/phoneScreenshots');
const featureDir = resolve(root, 'fastlane/metadata/android/pt-BR/images/featureGraphic');
mkdirSync(screenshotsDir, { recursive: true });
mkdirSync(featureDir, { recursive: true });

// Montserrat font + shared CSS reset
const montserrat = `<link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">`;

const reset = `
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Montserrat', sans-serif; -webkit-font-smoothing: antialiased; overflow: hidden; }
`;

// App UI SVG: faithful recreation of BanjoStringCanvas.kt
// Background: vertical gradient #1A1210 → #141010
// 4 vertical strings: D3 (terracotta), G3 (steel blue), B3 (gold), D4 (orange-red)
// Nut bar (top) + Bridge bar (bottom) + fret lines
// Labels: D3/G3/B3/D4 with Portuguese solfège below
function appUiSvg(width, height, activeString = -1) {
  // String colors from BanjoStringCanvas.kt stringPalette
  const strings = [
    { label: 'D3', sol: 'Ré',  idle: '#8C7161', active: '#D4956A' }, // 4th (leftmost)
    { label: 'G3', sol: 'Sol', idle: '#6B8490', active: '#5AAFCB' }, // 3rd
    { label: 'B3', sol: 'Si',  idle: '#8C8062', active: '#CBA55A' }, // 2nd
    { label: 'D4', sol: 'Ré',  idle: '#907466', active: '#E07850' }, // 1st (rightmost)
  ];

  const pad = 20;
  const nutH = 14;
  const bridgeH = 14;
  const nutY = pad;
  const bridgeY = height - pad - bridgeH;
  const bandW = (width - pad * 2) / 4;

  const stringEls = strings.map((s, i) => {
    const cx = pad + bandW * (i + 0.5);
    const color = activeString === i ? s.active : s.idle;
    const opacity = activeString >= 0 && activeString !== i ? 0.35 : 1;
    const strokeW = activeString === i ? 5 : [4, 3.2, 2.6, 2][i];

    // Glow for active string
    const glow = activeString === i
      ? `<line x1="${cx}" y1="${nutY + nutH}" x2="${cx}" y2="${bridgeY}" stroke="${s.active}" stroke-width="${strokeW + 20}" opacity="0.18"/>`
      : '';

    const labelY = bridgeY - 52;
    return `
      ${glow}
      <line x1="${cx}" y1="${nutY + nutH}" x2="${cx}" y2="${bridgeY}"
        stroke="${color}" stroke-width="${strokeW}" opacity="${opacity}" stroke-linecap="round"/>
      <text x="${cx}" y="${labelY}" text-anchor="middle"
        font-family="Montserrat,sans-serif" font-size="15" font-weight="500"
        letter-spacing="1.5" fill="${color}" opacity="${opacity}">${s.label}</text>
      <text x="${cx}" y="${labelY + 18}" text-anchor="middle"
        font-family="Montserrat,sans-serif" font-size="11" font-weight="400"
        fill="${color}" opacity="${opacity * 0.75}">${s.sol}</text>
    `;
  }).join('');

  // Fret lines at 18%, 33%, 45%, 55%, 65% of string length
  const stringTop = nutY + nutH;
  const stringLen = bridgeY - stringTop;
  const fretPcts = [0.18, 0.33, 0.45, 0.55, 0.65];
  const frets = fretPcts.map(p => {
    const y = stringTop + stringLen * p;
    return `<line x1="${pad}" y1="${y}" x2="${width - pad}" y2="${y}" stroke="#2E2420" stroke-width="1" opacity="0.5"/>`;
  }).join('');

  return `
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
  <defs>
    <linearGradient id="bgGrad" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#1A1210"/>
      <stop offset="35%" stop-color="#231A15"/>
      <stop offset="65%" stop-color="#1E1512"/>
      <stop offset="100%" stop-color="#141010"/>
    </linearGradient>
    <radialGradient id="vignette" cx="50%" cy="50%" r="85%">
      <stop offset="0%" stop-color="transparent"/>
      <stop offset="100%" stop-color="rgba(0,0,0,0.45)"/>
    </radialGradient>
  </defs>
  <!-- Background -->
  <rect width="${width}" height="${height}" fill="url(#bgGrad)"/>
  <rect width="${width}" height="${height}" fill="url(#vignette)"/>
  <!-- Frets -->
  ${frets}
  <!-- Strings -->
  ${stringEls}
  <!-- Nut -->
  <rect x="${pad}" y="${nutY}" width="${width - pad * 2}" height="${nutH}" rx="2" fill="#3D322A"/>
  <rect x="${pad}" y="${nutY}" width="${width - pad * 2}" height="1.5" fill="#5C4A3E" opacity="0.6"/>
  <!-- Bridge -->
  <rect x="${pad}" y="${bridgeY}" width="${width - pad * 2}" height="${bridgeH}" rx="2" fill="#3D322A"/>
  <rect x="${pad}" y="${bridgeY}" width="${width - pad * 2}" height="1.5" fill="#5C4A3E" opacity="0.6"/>
</svg>`;
}

// Encode SVG to data URI for embedding in HTML
function svgDataUri(svg) {
  return 'data:image/svg+xml;base64,' + Buffer.from(svg).toString('base64');
}
```

**Step 2: Verify the scaffold parses**

```bash
cd /Users/dan/projects/banjen/scripts && node --input-type=module --eval "
import { readFileSync } from 'fs';
const src = readFileSync('generate-screenshots-pt-BR.mjs', 'utf8');
console.log('Lines:', src.split('\n').length);
"
```

Expected: prints line count without errors.

---

### Task 3: Feature graphic (1024×500px)

**Step 1: Add feature graphic HTML to the script**

Append after the scaffold (before `main()`):

```javascript
// ── Feature Graphic ──────────────────────────────────────────────────────────
// Cavaquinho illustration (SVG) + headline on mahogany gradient
// "Afinador de Cavaquinho e Banjo" + "Afine de ouvido. Sem microfone. Sempre."

const cavaquinhoSvg = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 340" width="420" height="714">
  <!-- Body (pear shape) -->
  <ellipse cx="100" cy="230" rx="75" ry="95" fill="#C87830" opacity="0.95"/>
  <ellipse cx="100" cy="230" rx="60" ry="78" fill="#D4A040" opacity="0.3"/>
  <!-- Sound hole -->
  <circle cx="100" cy="225" r="28" fill="#1A0A00" opacity="0.8"/>
  <circle cx="100" cy="225" r="28" fill="none" stroke="#6B3010" stroke-width="3"/>
  <!-- Waist -->
  <ellipse cx="100" cy="155" rx="40" ry="20" fill="#C87830" opacity="0.95"/>
  <!-- Upper bout -->
  <ellipse cx="100" cy="125" rx="55" ry="48" fill="#C87830" opacity="0.95"/>
  <!-- Neck -->
  <rect x="88" y="28" width="24" height="110" rx="6" fill="#6B3010"/>
  <rect x="91" y="28" width="18" height="110" rx="4" fill="#8B4510" opacity="0.5"/>
  <!-- Headstock -->
  <rect x="84" y="8" width="32" height="28" rx="4" fill="#5A2A08"/>
  <!-- Tuning pegs -->
  <circle cx="88" cy="12" r="4" fill="#C0A060"/>
  <circle cx="100" cy="10" r="4" fill="#C0A060"/>
  <circle cx="112" cy="12" r="4" fill="#C0A060"/>
  <circle cx="88" cy="28" r="4" fill="#C0A060"/>
  <circle cx="112" cy="28" r="4" fill="#C0A060"/>
  <!-- Strings (4) -->
  <line x1="91" y1="28" x2="91" y2="300" stroke="#D4A040" stroke-width="1.5" opacity="0.7"/>
  <line x1="97" y1="28" x2="97" y2="300" stroke="#D4A040" stroke-width="1.2" opacity="0.7"/>
  <line x1="103" y1="28" x2="103" y2="300" stroke="#D4A040" stroke-width="1.0" opacity="0.7"/>
  <line x1="109" y1="28" x2="109" y2="300" stroke="#D4A040" stroke-width="0.8" opacity="0.7"/>
  <!-- Bridge -->
  <rect x="84" y="298" width="32" height="6" rx="2" fill="#5A2A08"/>
  <!-- Fret lines -->
  <line x1="87" y1="55" x2="113" y2="55" stroke="#C0A060" stroke-width="1" opacity="0.4"/>
  <line x1="87" y1="70" x2="113" y2="70" stroke="#C0A060" stroke-width="1" opacity="0.4"/>
  <line x1="87" y1="83" x2="113" y2="83" stroke="#C0A060" stroke-width="1" opacity="0.4"/>
  <line x1="87" y1="95" x2="113" y2="95" stroke="#C0A060" stroke-width="1" opacity="0.4"/>
  <line x1="87" y1="106" x2="113" y2="106" stroke="#C0A060" stroke-width="1" opacity="0.4"/>
  <!-- String labels -->
  <text x="91" y="315" text-anchor="middle" font-size="7" fill="#F5C842" font-family="sans-serif" opacity="0.8">Ré</text>
  <text x="97" y="315" text-anchor="middle" font-size="7" fill="#F5C842" font-family="sans-serif" opacity="0.8">Sol</text>
  <text x="103" y="315" text-anchor="middle" font-size="7" fill="#F5C842" font-family="sans-serif" opacity="0.8">Si</text>
  <text x="109" y="315" text-anchor="middle" font-size="7" fill="#F5C842" font-family="sans-serif" opacity="0.8">Ré</text>
</svg>`;

const featureGraphic = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${FEATURE_W}px; height: ${FEATURE_H}px;
  background: linear-gradient(135deg, #1A0A00 0%, #2D1500 55%, #3D1A00 100%);
  display: flex; align-items: center; position: relative; overflow: hidden;
}
.text-block {
  position: absolute; left: 52px; top: 50%; transform: translateY(-50%);
  max-width: 540px;
}
.primary {
  font-size: 58px; font-weight: 900; color: #fff;
  line-height: 1.05; letter-spacing: -1px; margin-bottom: 16px;
}
.secondary {
  font-size: 26px; font-weight: 600; color: #F5C842;
  line-height: 1.4; margin-bottom: 14px;
}
.tertiary {
  font-size: 18px; font-weight: 400; color: rgba(255,255,255,0.7);
}
.instrument {
  position: absolute; right: -30px; top: 50%; transform: translateY(-52%);
  opacity: 0.92;
}
</style></head>
<body>
  <div class="text-block">
    <div class="primary">Afinador de<br>Cavaquinho e Banjo</div>
    <div class="secondary">Afine de ouvido. Sem microfone. Sempre.</div>
    <div class="tertiary">100% offline &nbsp;·&nbsp; Grátis &nbsp;·&nbsp; DGBD — Ré-Sol-Si-Ré</div>
  </div>
  <div class="instrument">${cavaquinhoSvg}</div>
</body></html>`;
```

---

### Task 4: Screenshot 1 — Hero

**Step 1: Add ss01-hero HTML**

```javascript
// ── ss01 Hero ─────────────────────────────────────────────────────────────────
const appUiMini = appUiSvg(540, 960, 1); // G3 active (index 1)
const appUriMini = svgDataUri(appUiMini);

// Sound wave rings for active state
function soundWavesSvg(cx, cy, color = '#5AAFCB') {
  return `
<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" style="position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);pointer-events:none">
  <circle cx="150" cy="150" r="60"  fill="none" stroke="${color}" stroke-width="2" opacity="0.25"/>
  <circle cx="150" cy="150" r="90"  fill="none" stroke="${color}" stroke-width="1.5" opacity="0.15"/>
  <circle cx="150" cy="150" r="120" fill="none" stroke="${color}" stroke-width="1" opacity="0.08"/>
</svg>`;
}

const ss01 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: linear-gradient(170deg, #1A0A00 0%, #2A1200 40%, #1A0A00 100%);
  display: flex; flex-direction: column; align-items: center;
  position: relative; overflow: hidden;
}
.caption-block {
  margin-top: 140px; text-align: center; z-index: 2; position: relative;
}
.primary {
  font-size: 88px; font-weight: 900; color: #fff;
  letter-spacing: 2px; line-height: 1.1;
}
.app-wrap {
  position: relative; margin-top: 60px;
  width: 540px; height: 960px; border-radius: 32px; overflow: hidden;
  box-shadow: 0 0 80px rgba(90,175,203,0.18), 0 40px 80px rgba(0,0,0,0.6);
}
.app-wrap img { width: 100%; height: 100%; display: block; }
.waves { position: absolute; left: 50%; top: 44%; transform: translate(-50%,-50%); }
</style></head>
<body>
  <div class="caption-block">
    <div class="primary">Toque.<br>Ouça.<br>Afine.</div>
  </div>
  <div class="app-wrap">
    <img src="${appUriMini}" alt="app">
    <div class="waves">${soundWavesSvg(0, 0, '#5AAFCB')}</div>
  </div>
</body></html>`;
```

---

### Task 5: Screenshot 2 — Equivalence

**Step 1: Add ss02-equivalence HTML**

```javascript
// Simplified banjo SVG (4-string, circular body)
const banjoSvg = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 320" width="220" height="440">
  <!-- Drum head -->
  <circle cx="80" cy="220" r="68" fill="#E8D8B0" opacity="0.9"/>
  <circle cx="80" cy="220" r="68" fill="none" stroke="#6B4020" stroke-width="6"/>
  <circle cx="80" cy="220" r="62" fill="none" stroke="#8B5030" stroke-width="2" opacity="0.5"/>
  <!-- Tension hooks (8 around) -->
  ${[0,45,90,135,180,225,270,315].map(deg => {
    const rad = deg * Math.PI / 180;
    const x = 80 + 74 * Math.sin(rad);
    const y = 220 - 74 * Math.cos(rad);
    return `<rect x="${x-2}" y="${y-4}" width="4" height="8" rx="1" fill="#8B6040" transform="rotate(${deg} ${x} ${y})"/>`;
  }).join('')}
  <!-- Bridge -->
  <rect x="72" y="198" width="16" height="5" rx="2" fill="#5A3010"/>
  <!-- Neck -->
  <rect x="70" y="30" width="20" height="165" rx="5" fill="#6B3010"/>
  <rect x="73" y="30" width="14" height="165" rx="4" fill="#8B4510" opacity="0.4"/>
  <!-- Headstock -->
  <rect x="67" y="8" width="26" height="30" rx="4" fill="#5A2A08"/>
  <!-- Tuning pegs (4) -->
  <circle cx="72" cy="14" r="4" fill="#C0A060"/>
  <circle cx="88" cy="14" r="4" fill="#C0A060"/>
  <circle cx="72" cy="30" r="4" fill="#C0A060"/>
  <circle cx="88" cy="30" r="4" fill="#C0A060"/>
  <!-- Strings (4) -->
  <line x1="74" y1="30" x2="74" y2="200" stroke="#D4A040" stroke-width="1.5" opacity="0.7"/>
  <line x1="78" y1="30" x2="78" y2="200" stroke="#D4A040" stroke-width="1.2" opacity="0.7"/>
  <line x1="82" y1="30" x2="82" y2="200" stroke="#D4A040" stroke-width="1.0" opacity="0.7"/>
  <line x1="86" y1="30" x2="86" y2="200" stroke="#D4A040" stroke-width="0.8" opacity="0.7"/>
</svg>`;

const ss02 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: #1A0D00;
  display: flex; flex-direction: column; align-items: center; overflow: hidden;
}
.caption {
  margin-top: 100px; text-align: center; padding: 0 60px; z-index: 2;
}
.primary {
  font-size: 64px; font-weight: 800; color: #fff; line-height: 1.1; margin-bottom: 20px;
}
.tuning {
  font-size: 48px; font-weight: 700; color: #F5C842; letter-spacing: 4px;
}
.instruments {
  display: flex; align-items: flex-end; justify-content: center;
  gap: 40px; margin-top: 60px;
}
.equals {
  font-size: 80px; font-weight: 900; color: #F5C842;
  align-self: center; margin: 0 8px; opacity: 0.9;
}
.inst-label {
  text-align: center; margin-top: 12px;
  font-size: 22px; font-weight: 700; color: rgba(255,255,255,0.7);
}
</style></head>
<body>
  <div class="caption">
    <div class="primary">Cavaquinho. Banjo.<br>Mesma afinação.</div>
    <div class="tuning">Ré · Sol · Si · Ré</div>
  </div>
  <div class="instruments">
    <div>
      ${cavaquinhoSvg.replace('width="420" height="714"', 'width="240" height="408"')}
      <div class="inst-label">Cavaquinho</div>
    </div>
    <div class="equals">=</div>
    <div>
      ${banjoSvg}
      <div class="inst-label">Banjo (4 cordas)</div>
    </div>
  </div>
</body></html>`;
```

---

### Task 6: Screenshot 3 — Sem Microfone

**Step 1: Add ss03 HTML**

```javascript
const micOffSvg = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" width="200" height="200">
  <!-- Mic body -->
  <rect x="80" y="30" width="40" height="80" rx="20" fill="none" stroke="white" stroke-width="6"/>
  <!-- Mic stand -->
  <path d="M60 110 Q60 155 100 155 Q140 155 140 110" fill="none" stroke="white" stroke-width="6" stroke-linecap="round"/>
  <line x1="100" y1="155" x2="100" y2="175" stroke="white" stroke-width="6" stroke-linecap="round"/>
  <line x1="75" y1="175" x2="125" y2="175" stroke="white" stroke-width="6" stroke-linecap="round"/>
  <!-- Red X cross-out -->
  <line x1="30" y1="30" x2="170" y2="170" stroke="#E53935" stroke-width="10" stroke-linecap="round"/>
  <line x1="170" y1="30" x2="30" y2="170" stroke="#E53935" stroke-width="10" stroke-linecap="round"/>
</svg>`;

const appUiSs03 = appUiSvg(480, 860, 0); // D3 active

const ss03 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: linear-gradient(180deg, #0D0D1A 0%, #1A1210 60%, #141010 100%);
  display: flex; flex-direction: column; align-items: center; overflow: hidden;
}
.top-block { margin-top: 120px; display: flex; flex-direction: column; align-items: center; }
.mic-icon { margin-bottom: 36px; }
.primary {
  font-size: 90px; font-weight: 900; color: #fff;
  text-align: center; line-height: 1.05; letter-spacing: 1px;
}
.secondary {
  font-size: 46px; font-weight: 500; color: rgba(255,255,255,0.85);
  text-align: center; line-height: 1.3; margin-top: 24px; padding: 0 80px;
}
.secondary em { color: #F5C842; font-style: normal; font-weight: 700; }
.app-wrap {
  margin-top: 80px; width: 480px; height: 860px;
  border-radius: 28px; overflow: hidden;
  box-shadow: 0 40px 80px rgba(0,0,0,0.7);
}
.app-wrap img { width: 100%; height: 100%; }
</style></head>
<body>
  <div class="top-block">
    <div class="mic-icon">${micOffSvg}</div>
    <div class="primary">Sem microfone.</div>
    <div class="secondary">Funciona no meio <em>da roda</em>.</div>
  </div>
  <div class="app-wrap">
    <img src="${svgDataUri(appUiSs03)}" alt="app">
  </div>
</body></html>`;
```

---

### Task 7: Screenshot 4 — Roda

**Step 1: Add ss04 HTML**

```javascript
// Silhouette roda scene: musicians in circle around a warm light source
const rodaSvg = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500" width="1080" height="675">
  <defs>
    <radialGradient id="light" cx="50%" cy="60%" r="45%">
      <stop offset="0%" stop-color="#C8720A" stop-opacity="0.6"/>
      <stop offset="100%" stop-color="transparent"/>
    </radialGradient>
  </defs>
  <rect width="800" height="500" fill="#0A0500"/>
  <!-- Warm candlelight glow -->
  <ellipse cx="400" cy="300" rx="320" ry="200" fill="url(#light)"/>
  <!-- Ground circle (roda floor) -->
  <ellipse cx="400" cy="420" rx="280" ry="60" fill="#1A0A00" opacity="0.8"/>
  <!-- Musician silhouettes (6 around circle) -->
  <!-- Left musician with cavaquinho -->
  <g transform="translate(155,200)">
    <ellipse cx="0" cy="80" rx="22" ry="55" fill="#0A0500" opacity="0.9"/>
    <circle cx="0" cy="14" r="18" fill="#0A0500" opacity="0.9"/>
    <ellipse cx="18" cy="70" rx="20" ry="28" fill="#0A0500" opacity="0.7"/>
  </g>
  <!-- Front-left musician -->
  <g transform="translate(260,260)">
    <ellipse cx="0" cy="75" rx="24" ry="52" fill="#0A0500" opacity="0.9"/>
    <circle cx="0" cy="12" r="19" fill="#0A0500" opacity="0.9"/>
    <ellipse cx="20" cy="65" rx="22" ry="30" fill="#0A0500" opacity="0.7"/>
  </g>
  <!-- Front musician (pandeiro) -->
  <g transform="translate(380,290)">
    <ellipse cx="0" cy="72" rx="25" ry="50" fill="#0A0500" opacity="0.95"/>
    <circle cx="0" cy="10" r="20" fill="#0A0500" opacity="0.95"/>
    <circle cx="30" cy="48" r="24" fill="#0A0500" stroke="#2A1500" stroke-width="3" opacity="0.7"/>
  </g>
  <!-- Front-right musician -->
  <g transform="translate(510,265)">
    <ellipse cx="0" cy="74" rx="24" ry="52" fill="#0A0500" opacity="0.9"/>
    <circle cx="0" cy="12" r="19" fill="#0A0500" opacity="0.9"/>
    <ellipse cx="-20" cy="65" rx="22" ry="30" fill="#0A0500" opacity="0.7"/>
  </g>
  <!-- Right musician with banjo -->
  <g transform="translate(620,205)">
    <ellipse cx="0" cy="80" rx="22" ry="55" fill="#0A0500" opacity="0.9"/>
    <circle cx="0" cy="14" r="18" fill="#0A0500" opacity="0.9"/>
    <ellipse cx="-18" cy="70" rx="20" ry="28" fill="#0A0500" opacity="0.7"/>
  </g>
  <!-- Back musicians (smaller, further) -->
  <g transform="translate(310,170)" opacity="0.6">
    <ellipse cx="0" cy="55" rx="16" ry="38" fill="#0A0500"/>
    <circle cx="0" cy="10" r="14" fill="#0A0500"/>
  </g>
  <g transform="translate(490,175)" opacity="0.6">
    <ellipse cx="0" cy="55" rx="16" ry="38" fill="#0A0500"/>
    <circle cx="0" cy="10" r="14" fill="#0A0500"/>
  </g>
  <!-- Candle / warm light center -->
  <ellipse cx="400" cy="380" rx="18" ry="6" fill="#C8720A" opacity="0.6"/>
  <path d="M396 350 Q400 330 404 350 Q406 360 400 365 Q394 360 396 350Z" fill="#F5C842" opacity="0.9"/>
  <path d="M398 352 Q400 342 402 352 Q403 356 400 359 Q397 356 398 352Z" fill="#FFF8DC"/>
</svg>`;

const appUiSs04 = appUiSvg(440, 780, 2); // B3 active

const ss04 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: #0A0500;
  display: flex; flex-direction: column; align-items: center;
  position: relative; overflow: hidden;
}
.caption {
  position: absolute; top: 110px; left: 0; right: 0;
  text-align: center; z-index: 3; padding: 0 80px;
}
.primary {
  font-size: 72px; font-weight: 800; color: #fff;
  line-height: 1.1; letter-spacing: -0.5px;
}
.always {
  font-size: 90px; font-weight: 900; color: #F5C842;
  font-style: italic; margin-top: 12px;
}
.roda-scene {
  position: absolute; bottom: 240px; left: 50%; transform: translateX(-50%);
  opacity: 0.85;
}
.app-wrap {
  position: absolute; bottom: -40px; left: 50%; transform: translateX(-50%);
  width: 440px; height: 780px;
  border-radius: 28px; overflow: hidden;
  box-shadow: 0 -20px 60px rgba(200,114,10,0.15), 0 20px 60px rgba(0,0,0,0.8);
  z-index: 2;
}
.app-wrap img { width: 100%; height: 100%; }
</style></head>
<body>
  <div class="caption">
    <div class="primary">Chegue afinado<br>na roda.</div>
    <div class="always">Sempre.</div>
  </div>
  <div class="roda-scene">${rodaSvg}</div>
  <div class="app-wrap">
    <img src="${svgDataUri(appUiSs04)}" alt="app">
  </div>
</body></html>`;
```

---

### Task 8: Screenshot 5 — Offline

**Step 1: Add ss05 HTML**

```javascript
const appUiSs05 = appUiSvg(500, 880, 3); // D4 active

const ss05 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: linear-gradient(180deg, #0D0A05 0%, #1A1005 50%, #0D0A05 100%);
  display: flex; flex-direction: column; align-items: center; overflow: hidden;
}
.top { margin-top: 130px; text-align: center; padding: 0 80px; }
.primary {
  font-size: 90px; font-weight: 900; color: #fff; line-height: 1.05;
}
.secondary {
  font-size: 40px; font-weight: 500; color: rgba(255,255,255,0.8);
  line-height: 1.4; margin-top: 20px;
}
.secondary em { color: #F5C842; font-style: normal; }
.context-pills {
  display: flex; gap: 20px; margin-top: 36px; justify-content: center; flex-wrap: wrap;
}
.pill {
  background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.18);
  border-radius: 40px; padding: 10px 24px;
  font-size: 26px; font-weight: 600; color: rgba(255,255,255,0.75);
}
.airplane {
  margin-top: 10px; opacity: 0.6;
}
.app-wrap {
  margin-top: 50px; width: 500px; height: 880px;
  border-radius: 28px; overflow: hidden;
  box-shadow: 0 40px 80px rgba(0,0,0,0.7);
  position: relative;
}
.app-wrap img { width: 100%; height: 100%; }
.status-bar {
  position: absolute; top: 8px; right: 16px;
  font-size: 18px; color: rgba(255,255,255,0.5);
  font-family: Montserrat, sans-serif;
}
</style></head>
<body>
  <div class="top">
    <div class="primary">Funciona offline.</div>
    <div class="secondary">No <em>boteco</em>, na roda,<br>em qualquer lugar.</div>
    <div class="context-pills">
      <div class="pill">✈ Sem WiFi</div>
      <div class="pill">Sem sinal</div>
      <div class="pill">Em qualquer lugar</div>
    </div>
  </div>
  <div class="app-wrap">
    <img src="${svgDataUri(appUiSs05)}" alt="app">
    <div class="status-bar">✈</div>
  </div>
</body></html>`;
```

---

### Task 9: Screenshot 6 — Simplicity

**Step 1: Add ss06 HTML**

```javascript
// Large close-up of just the 4 strings (the whole app UI fills the lower 3/4)
const appUiLarge = appUiSvg(1080, 1300, -1); // all strings idle

const ss06 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: #141010;
  display: flex; flex-direction: column; align-items: center; overflow: hidden;
}
.caption { margin-top: 110px; text-align: center; padding: 0 70px; }
.primary {
  font-size: 96px; font-weight: 900; color: #fff;
  letter-spacing: -1px; line-height: 1.0;
}
.dot { color: #F5C842; }
.secondary {
  font-size: 52px; font-weight: 700; color: rgba(255,255,255,0.85);
  margin-top: 20px; line-height: 1.2;
}
.tertiary {
  font-size: 34px; font-weight: 500; color: #F5C842;
  margin-top: 16px; font-style: italic;
}
.app-wrap {
  margin-top: 50px; width: 1080px; height: 1300px; overflow: hidden;
}
.app-wrap img { width: 100%; height: 100%; object-fit: cover; object-position: top; }
</style></head>
<body>
  <div class="caption">
    <div class="primary">4 botões<span class="dot">.</span><br>4 cordas<span class="dot">.</span></div>
    <div class="secondary">Só apertar e afinar.</div>
    <div class="tertiary">Do jeito que os músicos gostam.</div>
  </div>
  <div class="app-wrap">
    <img src="${svgDataUri(appUiLarge)}" alt="app">
  </div>
</body></html>`;
```

---

### Task 10: Screenshot 7 — Genres

**Step 1: Add ss07 HTML**

```javascript
const appUiSs07 = appUiSvg(400, 700, -1);

const ss07 = `<!DOCTYPE html>
<html><head>
${montserrat}
<style>
${reset}
body {
  width: ${PHONE_W}px; height: ${PHONE_H}px;
  background: #0A0500;
  display: flex; flex-direction: column; align-items: center;
  position: relative; overflow: hidden;
}
/* Genre colour zones as angled bands */
.zone-samba  { position:absolute; top:0; left:0; width:100%; height:50%; background:linear-gradient(135deg,#D4830A22 0%,transparent 70%); }
.zone-pagode { position:absolute; top:0; right:0; width:60%; height:60%; background:linear-gradient(225deg,#A0522D22 0%,transparent 70%); }
.zone-choro  { position:absolute; bottom:0; left:0; width:60%; height:55%; background:linear-gradient(45deg,#C8A20022 0%,transparent 70%); }
.zone-forro  { position:absolute; bottom:0; right:0; width:55%; height:50%; background:linear-gradient(315deg,#8B1A1A22 0%,transparent 70%); }

.caption { margin-top: 100px; text-align: center; padding: 0 60px; z-index: 2; position:relative; }
.primary {
  font-size: 62px; font-weight: 800; color: #fff; line-height: 1.15;
}
.genre-names {
  display: flex; flex-wrap: wrap; justify-content: center; gap: 16px;
  margin-top: 36px; padding: 0 40px; z-index: 2; position: relative;
}
.genre-badge {
  padding: 12px 28px; border-radius: 40px;
  font-size: 30px; font-weight: 800;
  border: 2px solid currentColor;
}
.samba  { color: #D4830A; background: rgba(212,131,10,0.12); }
.pagode { color: #C87830; background: rgba(200,120,48,0.12); }
.choro  { color: #C8A200; background: rgba(200,162,0,0.12); }
.forro  { color: #D44040; background: rgba(212,64,64,0.12); }

.instrument-icons {
  display: flex; justify-content: space-around; width: 100%;
  padding: 0 60px; margin-top: 32px; z-index: 2; position: relative;
  opacity: 0.55;
}
.inst-icon { font-size: 56px; }
.app-wrap {
  margin-top: 28px; width: 400px; height: 700px;
  border-radius: 24px; overflow: hidden;
  box-shadow: 0 40px 80px rgba(0,0,0,0.8);
  z-index: 2; position: relative;
}
.app-wrap img { width: 100%; height: 100%; }
</style></head>
<body>
  <div class="zone-samba"></div>
  <div class="zone-pagode"></div>
  <div class="zone-choro"></div>
  <div class="zone-forro"></div>

  <div class="caption">
    <div class="primary">Para samba, pagode,<br>choro e forró.</div>
  </div>
  <div class="genre-names">
    <div class="genre-badge samba">Samba</div>
    <div class="genre-badge pagode">Pagode</div>
    <div class="genre-badge choro">Choro</div>
    <div class="genre-badge forro">Forró</div>
  </div>
  <div class="instrument-icons">
    <div class="inst-icon">🥁</div>
    <div class="inst-icon">🎵</div>
    <div class="inst-icon">🎸</div>
    <div class="inst-icon">🪗</div>
  </div>
  <div class="app-wrap">
    <img src="${svgDataUri(appUiSs07)}" alt="app">
  </div>
</body></html>`;
```

---

### Task 11: Wire up Puppeteer runner and run

**Step 1: Add the assets array and main() function**

```javascript
const assets = [
  { name: 'feature-graphic-pt-BR.png', html: featureGraphic, w: FEATURE_W, h: FEATURE_H, dir: featureDir },
  { name: 'ss01-hero-pt-BR.png',        html: ss01, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
  { name: 'ss02-equivalence-pt-BR.png', html: ss02, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
  { name: 'ss03-sem-microfone-pt-BR.png', html: ss03, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
  { name: 'ss04-roda-pt-BR.png',        html: ss04, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
  { name: 'ss05-offline-pt-BR.png',     html: ss05, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
  { name: 'ss06-simplicity-pt-BR.png',  html: ss06, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
  { name: 'ss07-genres-pt-BR.png',      html: ss07, w: PHONE_W, h: PHONE_H, dir: screenshotsDir },
];

async function main() {
  const browser = await puppeteer.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });

  for (const { name, html, w, h, dir } of assets) {
    const page = await browser.newPage();
    await page.setViewport({ width: w, height: h, deviceScaleFactor: 1 });
    await page.setContent(html, { waitUntil: 'networkidle0' });
    const outPath = resolve(dir, name);
    await page.screenshot({ path: outPath, type: 'png' });
    console.log(`✓ ${name}`);
    await page.close();
  }

  await browser.close();
  console.log('\nDone! All 8 assets generated.');
}

main().catch(err => { console.error(err); process.exit(1); });
```

**Step 2: Run the script**

```bash
cd /Users/dan/projects/banjen/scripts && node generate-screenshots-pt-BR.mjs
```

Expected output:
```
✓ feature-graphic-pt-BR.png
✓ ss01-hero-pt-BR.png
✓ ss02-equivalence-pt-BR.png
✓ ss03-sem-microfone-pt-BR.png
✓ ss04-roda-pt-BR.png
✓ ss05-offline-pt-BR.png
✓ ss06-simplicity-pt-BR.png
✓ ss07-genres-pt-BR.png

Done! All 8 assets generated.
```

**Step 3: Verify files exist at correct sizes**

```bash
ls -lh /Users/dan/projects/banjen/fastlane/metadata/android/pt-BR/images/phoneScreenshots/*.png
ls -lh /Users/dan/projects/banjen/fastlane/metadata/android/pt-BR/images/featureGraphic/*.png
```

Expected: 7 + 1 PNG files, each >100KB.

**Step 4: Spot-check one image**

```bash
file /Users/dan/projects/banjen/fastlane/metadata/android/pt-BR/images/phoneScreenshots/ss01-hero-pt-BR.png
# Expected: PNG image data, 1080 x 1920, 8-bit/color RGB
```

---

### Task 12: Commit

**Step 1: Stage and commit**

```bash
cd /Users/dan/projects/banjen
git add scripts/package.json scripts/package-lock.json scripts/generate-screenshots-pt-BR.mjs
git add fastlane/metadata/android/pt-BR/images/
git add docs/plans/
```

```bash
git commit -m "$(cat <<'EOF'
feat: add pt-BR Play Store screenshot generator

Puppeteer script generates 7 phone screenshots (1080x1920) and
feature graphic (1024x500) targeting Brazilian cavaquinho players.
Warm mahogany palette, Montserrat typography, faithful app UI SVG.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Quick Reference

| Asset | Dims | Active string |
|---|---|---|
| feature-graphic | 1024×500 | — |
| ss01-hero | 1080×1920 | G3 (index 1, steel blue) |
| ss02-equivalence | 1080×1920 | — |
| ss03-sem-microfone | 1080×1920 | D3 (index 0, terracotta) |
| ss04-roda | 1080×1920 | B3 (index 2, gold) |
| ss05-offline | 1080×1920 | D4 (index 3, orange-red) |
| ss06-simplicity | 1080×1920 | all idle |
| ss07-genres | 1080×1920 | all idle |
