# Banjen — Feature Opportunities v3
*May 20, 2026 | Feature Strategist — UX Design Thinking Investigation*

*Evidence base: v3 user-personas.md (5 final personas), v3 pains.md (49 cited pains, 10 cross-persona patterns, 5 inviolable constraints), competitor-analyst.md (11-app teardown), market-analyst.md (TAM expansion via cavaquinho + GDAE), social-analyst.md (referrer-shaped distribution), v2 features.md (continuity baseline). Every feature in this document traces to at least one pain ID and at least one persona. No intuition-based items.*

---

## Executive Summary

- **Top priority is non-negotiable and is a technically precise bug-fix, not a feature.** The single highest-leverage shipping action is reserving the bottom-banner ad slot at first paint so Cumulative Layout Shift cannot occur on the tuning surface. This addresses Pain #1, #2, #3, #5, #12 across 5/5 personas. Marcus diagnosed the exact mechanism; LikeTones already credibly holds the "No Ads" position; every day this remains unfixed, Banjen ships permanent churn. Effort: S. Reach: total.
- **The biggest single growth lever is zero-code and ships in one Play Store deploy: a pt-BR human-translated listing titled "Afinador de Cavaquinho e Banjo."** Market sizing puts the DGBD cavaquinho pool at 5–10x the entire global 4-string banjo population. Lúcia's WhatsApp roda multiplier means one successful share reaches ~1,000 cavaquinhistas in São Paulo within a week. This is not ASO — it is identity. The blocker is metadata, not engineering.
- **The biggest risk to mitigate is recommendation-reputation damage.** Pattern #9 (4/5 personas) makes referrals carry *negative* ROI if shipped before the ad-shift fix. Eileen sits on a recommendation for a full week; Marcus shelved a YouTube episode; Harold won't show Ray for three silent weeks. Inviolable Constraint #1 ("no ad on the tuning surface, ever") must ship before any distribution activation work. Otherwise we burn five distinct trust networks in parallel.
- **The biggest sustainable growth move is GDAE as a first-class home-screen preset.** Eileen's interview hardened from v2 "preference" to v3 binary precondition: "If your app opens to D-G-B-D and there's no obvious way to switch, the conversation's over before it started." The Irish trad community is small but globally connected; one TheSession.org gear-thread post reaches Dublin to Boston to Melbourne overnight. GDAE was already shipped in v2 — v3 sharpens this to *home-screen visibility*, not buried under "alternative tunings."
- **Monetization is fully solved on paper and ready to ship: one-time IAP at $3–5 / R$9.90–14.90 / €3–5, no subscription, no "Pro" terminology, no "free trial" copy.** All 5 personas converge on this exact band. Subscription is the #2 cause of 1-star reviews in the category (competitor-analyst). The pricing decision is made — execution is the only remaining variable.

---

## Scoring Rubric

Each feature is scored:

**Priority Score = (Persona Reach × Pain Severity × Strategic Leverage) ÷ Effort**

- **Persona Reach (1–5):** How many of the 5 v3 personas the feature affects. 5 = all five; 1 = single persona, no multiplier.
- **Pain Severity (1–5):** From pains.md severity matrix. 5 = uninstall trigger / permanent loss. 3 = recoverable friction. 1 = minor.
- **Strategic Leverage (1–5):** Distribution multiplier, competitive moat, market expansion. 5 = unlocks a referrer network or new market category. 1 = local quality improvement.
- **Effort (1–5):** S=1 (≤1 day, no code or trivial), S/M=2 (a sprint of UI/copy), M=3 (1–2 sprints, new asset + UI), M/L=4 (2–3 sprints, system change), L=5 (multi-sprint, new subsystem).

Resulting scale is roughly 1 (forgettable) to 125 (do-it-yesterday). Anything ≥40 is P0/P1 territory.

Tiers map to:
- **Tier 1 — Inviolable / P0:** universal dealbreakers; product malpractice if absent.
- **Tier 2 — High-Leverage / P1:** unlocks a referrer channel, a market, or a converted persona.
- **Tier 3 — Differentiator / P2:** competitive moat, depth, retention.
- **Tier 4 — Speculative / Watch:** monitor; revisit on signal.

---

## Tier 1: Inviolable / P0

These features fix universal dealbreakers. Each is non-negotiable; any one absent makes Banjen structurally unrecommendable to at least one referrer network.

### T1-1. Tuning-Surface No-Mutation Policy (CLS-safe banner)

**Persona reach:** 5/5 (Harold, Lúcia, Eileen, Wendell, Marcus).
**Pain links:** #1 (CLS layout-shift ad), #2 (audio ad mid-tune), #3 (interstitial on cold-start), #5 (mid-interaction layout shift), #12 (thumb-mis-tap into Chrome), #40 (tap-target adjacency to ads/billing).
**Score:** 5 × 5 × 5 ÷ 1 = **125.**
**Effort:** S (AdView reservation + render-order audit; bottom 64dp permanently reserved at first paint; no animation; no audio capability).
**Description:** Reserve the bottom 64dp at first paint on the tuning surface as a static, non-animating, audio-incapable surface that the AdMob banner inhabits but never grows, shrinks, shifts, or sounds. Disable interstitial cold-start ads. Forbid audio ads anywhere in the app by SDK configuration. The tuning surface (active-tuning UI from first tap to manual stop) becomes a no-mutation zone by product policy — no banner, badge, animation, overlay, or late-loading content can fire during active tuning.
**Acceptance criteria:**
- AdView layout slot is reserved in the Compose layout tree before any ad request fires; layout shift score (CLS-equivalent) on the tuning surface measured at 0.00 across 100 cold-start sessions.
- No animated, video, or audio ad creative is requested from AdMob — confirmed by mediation config snapshot in CI.
- No interstitial fires at cold-start, app-resume, or during the tuning interaction (only acceptable interstitial surface is exit, if any).
- Tap-target buffer ≥16dp between any string button and the ad slot edge (Material accessibility spec for older users).
- Manual regression: launch app 20× in airplane mode + 20× online; banner either does not appear or appears in pre-reserved slot with zero layout shift.

**Risks:** AdMob mediation networks occasionally serve non-conforming creatives — must enforce via creative-policy filters and have a kill-switch RC flag for the banner. Mitigated by RC flag + creative whitelist.
**Dependencies:** None. Ship first.

---

### T1-2. pt-BR Play Store Listing — "Afinador de Cavaquinho e Banjo"

**Persona reach:** 1/5 direct (Lúcia) + WhatsApp roda multiplier (~1,000 cavaquinhistas/week per share) + 2.4M PT-speaking diaspora.
**Pain links:** #7 (English-only listing pre-install gate), #42 (cavaquinho not labeled), B1/B2/B4 (discovery & install category), #28 (recommendation reputation — Lúcia will share *no mesmo segundo* once "afinador de cavaco" is in the title).
**Score:** 2 × 5 × 5 ÷ 1 = **50.** (Strategic leverage scored 5 because this unlocks the largest hidden TAM in the persona set; reach is 2 personas direct — Lúcia + Marcus — but multiplier is order-of-magnitude.)
**Effort:** S (zero code; one Play Store deploy cycle; ~2 days of human translation + screenshot pt-BR variants).
**Description:** Ship a pt-BR Play Store listing with: title containing "Afinador de Cavaquinho e Banjo"; subtitle / short description containing "cavaco," "samba," "choro," "pagode," "afinador"; long description human-translated (not machine-translated — Lúcia explicitly reads quality as a respect signal); screenshots featuring a cavaquinho silhouette; keyword set with all PT-BR variants ("afinador de cavaquinho," "afinador de cavaco," "afinador cavaquinho samba").
**Acceptance criteria:**
- Title field on pt-BR locale literally contains the word "Cavaquinho."
- Long description is human-translated (signed off by a native pt-BR speaker; Cifra Club / Reclame Aqui idiom natural, not Google-translate cadence).
- Listing appears in the top-10 organic results for the query "afinador de cavaquinho" in São Paulo Play Store within 30 days.
- Screenshot #1 carries the top-line claim "Sem microfone necessário" (no mic required).
- One screenshot shows a cavaquinho silhouette + DGBD string labels; cavaquinho is not relegated to keywords-only.

**Risks:** Cultural authenticity. A monolingual American app marketing to cavaquinhistas without a credible PT description will be detected and roasted on Reclame Aqui within a week. Mitigated by paying for native human translation (~US$200) and one round of native review.
**Dependencies:** None. Ship in parallel with T1-1.

---

### T1-3. Home-Screen Tuning Picker — DGBD / GDAE / CGBD First-Class

**Persona reach:** 3/5 (Eileen — gating; Wendell — gating; Lúcia — labels matter even where the pitches happen to match).
**Pain links:** #6 (no visible alt-tuning preset), #43 (CGBD absent), #44 (GDAE buried), #46 (onboarding/signup before tuning), F1/F2/F3 (cross-instrument category).
**Score:** 3 × 5 × 5 ÷ 2 = **37.5.**
**Effort:** S/M (existing TuningModel already supports GDAE/CGBD/DGBE — work is *visibility*, not code: replace the "alternative tunings" submenu with a first-screen picker, surface localized labels including instrument context like "Plectrum / Chicago," "Irish tenor," "Cavaquinho").
**Description:** The home screen becomes (a) a horizontal/visible tuning picker showing DGBD / GDAE / CGBD with localized instrument-context labels ("Banjo / Cavaquinho — DGBD," "Tenor banjo / Mandolin — GDAE," "Plectrum / Chicago — CGBD"), and (b) four big buttons that play the selected tuning's notes. No "Alternative tunings" submenu. No settings dive. The picker IS the home screen. v2 shipped GDAE/CGBD presets but buried under instrument selector — v3 elevates to first-class home-screen surface.
**Acceptance criteria:**
- All three tunings (DGBD/GDAE/CGBD) visible within the first 3 seconds of cold-start, without any tap or scroll.
- Each tuning preset carries an instrument-context label in the user's locale (e.g., pt-BR: "Cavaquinho / Banjo — DGBD"; en-IE: "Irish Tenor / Mandolin — GDAE").
- Selected preset persists in SharedPreferences across cold-starts; user's last selection is the default.
- Tuning picker is keyboard / TalkBack accessible (Wendell's Camille may set up via accessibility shortcuts).
- No "Alternative tunings" label exists in the UI hierarchy — Eileen's exact failure mode.

**Risks:** Picker UI complexity competes with Harold's "four buttons, no choice paralysis" mental model. Mitigated by defaulting to user's last-selected tuning (Harold lands on DGBD permanently after first tap), and visually grouping picker as a compact pill row above the four buttons rather than a dropdown.
**Dependencies:** T1-1 (don't ship picker before tuning surface is sacred).

---

### T1-4. Zero-Permission, Zero-Onboarding First Launch

**Persona reach:** 5/5 (Harold, Lúcia, Eileen, Wendell, Marcus).
**Pain links:** #8 (mic permission for reference-tone app), #46 (onboarding carousel/signup before tuning), #48 (registration/login required), #29 (permission-dialog fear), C1/C5/E7 (trust & permissions, cognitive load).
**Score:** 5 × 5 × 4 ÷ 1 = **100.**
**Effort:** S (mostly already implemented; harden by audit + Play Store screenshot claim).
**Description:** Banjen never requests microphone permission, never shows an onboarding carousel, never requires registration, login, or email. First tap on app icon produces a looping reference tone within 2 seconds. Surface "No microphone required" as the top-line Play Store screenshot claim (en/es/pt/it/en-IE).
**Acceptance criteria:**
- App manifest contains no `RECORD_AUDIO` permission declaration.
- No first-launch dialog, modal, sheet, carousel, or signup screen appears before the tuning picker is interactive.
- Time-to-first-tone (cold-start → looping D playing) ≤ 2 seconds on a Galaxy A54 (Lúcia's device) and a 5-year-old Samsung Galaxy (Wendell-class device).
- Play Store screenshot #1 in all locales contains the localized claim "No microphone required" / "Sem microfone necessário" / "Sin micrófono" / "Senza microfono" / "No mic, no signup."
- ProGuard/R8 build verifies no mic-path code is reachable from any UI surface.

**Risks:** Future feature creep (e.g., visual tuning feedback / YIN pitch detection) would violate this. Treat as a policy decision: if mic ever enters the app, it must be an explicit opt-in inside a clearly-labeled separate flow, never on the default tuning surface.
**Dependencies:** None.

---

### T1-5. One-Time IAP Ad-Removal — $2.99 / R$12.90 / €2.99

**Persona reach:** 5/5 (all converge on one-time IAP at this band).
**Pain links:** #4 (accidental subscription dark pattern), #9 (subscription pricing model), #30 ("free trial" copy trigger), #31 (settings drawer upsell), #47 ("Pro" terminology), C2/C3/C4 (trust & permissions category).
**Score:** 5 × 5 × 4 ÷ 2 = **50.**
**Effort:** S/M (Google Play Billing v6 one-time IAP, ~3 days; harder than it looks because of restore-purchase + family-sharing + per-locale price).
**Description:** Single Google Play Billing one-time IAP at $2.99 (or per-locale equivalent: R$12.90, €2.99, £2.49) that permanently removes the bottom banner across all surfaces. No subscription cadence. No "Pro" terminology — copy reads "Remove ads — one-time" or "Tirar os anúncios — uma vez só." No "free trial." No upsell modal anywhere except a single button in settings labeled "Remove ads — $2.99."
**Acceptance criteria:**
- Purchase flow contains zero references to "free trial," "subscription," "renew," or "Pro tier."
- Locale-specific price bands enforced: US $2.99, BR R$12.90, EU €2.99, UK £2.49, IE €2.99.
- Restore-purchase flow works on a new device for same Google account.
- Settings drawer has exactly one upsell-related entry: "Remove ads — $2.99" (or equivalent). No badges, no animations, no modal interrupts.
- Once purchased, the bottom 64dp reserved slot collapses to 0dp (or shows nothing — never an alternate ad).

**Risks:** Google Play Billing testing complexity. Mitigated by feature-flagging the IAP behind RC until verified across 3 device classes.
**Dependencies:** T1-1 (don't monetize before the tuning surface is sacred — otherwise the IAP reads as ransom, not as fairness).

---

## Tier 2: High-Leverage / P1

Each unlocks a referrer channel, an underserved market, or a converted persona.

### T2-1. Looping Tone Architecture — Hold-to-Stop, Two-Handed Tuning

**Persona reach:** 4/5 (Harold, Lúcia, Eileen, Wendell — Marcus tolerates either).
**Pain links:** #10 (one-shot tones), #24 (two-handed tuning impossible), D5 (physical/motor — two-handed), D6 (friction-peg overshoot), Pattern #6 (looping playback enables two-handed tuning).
**Score:** 4 × 4 × 4 ÷ 1 = **16.** (Already largely shipped — v3 work is verification + tone duration tuning + stop-affordance design.)
**Effort:** S (already in production via `SoundPlayer.kt` `setStreamMute()` workaround; v3 work is to ensure loop length ≥ 10s by default, stop affordance is large + obvious, and the pop/click-free behavior remains intact through any future MediaPlayer refactor).
**Description:** Tone loops for ≥10 seconds (configurable up to 30s) per single tap; a single visible "stop" affordance ends playback cleanly. No pop/click on stop (already invisibly nailed via `setStreamMute()` — comparable apps NETIGEN/T4A get complaints for it). Two hands remain on the instrument throughout.
**Acceptance criteria:**
- Default loop length ≥10 seconds.
- Single tap on string button starts loop; second tap on same button stops it.
- No audible pop/click on stop, verified on 3 device classes (modern flagship, mid-tier, 5-yr-old).
- Stop affordance is ≥60dp (Wendell's thumb-pad target) and visually distinct from string buttons.
- Auto-timeout at 30 seconds to prevent forgotten-running state (battery, attention).

**Risks:** Modern Android MediaPlayer deprecations (HIGH severity per CLAUDE.md). Migration to `AudioTrack` or `ExoPlayer` for sustained tones risks regressing the pop/click handling. Mitigated by golden audio test in CI that fails on detectable stop-artifact above -60dB.
**Dependencies:** T1-1.

---

### T2-2. "No Microphone Required" Listing Promotion

**Persona reach:** 5/5 (Pattern #2 — mic permission is brand-trust signal across all personas).
**Pain links:** #8 (mic permission for reference-tone app), C1 (trust & permissions), Pattern #2.
**Score:** 5 × 4 × 4 ÷ 1 = **20.**
**Effort:** S (Play Store screenshot + short-description copy in all 5 locales).
**Description:** Surface "No microphone required" as the **top-line** value claim on screenshot 1 in all locales. Currently a hidden technical detail; v3 elevates to listing-grade copy. Pairs with the architectural fact (T1-4): a mic-asking reference-tone tuner is, in Eileen's words, "either incompetent or up to something."
**Acceptance criteria:**
- Screenshot 1 in en, es, pt-BR, it, en-IE all contain the localized "No microphone required" claim as the dominant text element.
- Short description in all locales mentions "no mic / sem microfone / sin micrófono / senza microfono."
- A/B test (where Play Store allows): "No microphone required" claim vs. baseline — measure 7-day install conversion.

**Risks:** None material.
**Dependencies:** T1-2 (pt-BR listing pipeline must be in place to ship pt-BR variant).

---

### T2-3. en-IE Locale + GDAE Cultural Framing

**Persona reach:** 1/5 direct (Eileen) + TheSession.org / WhatsApp session multiplier ("Dublin to Boston to Melbourne overnight").
**Pain links:** #44 (GDAE buried), B3 (sponsored top-of-search), G2 (no persona-specific referral asset), Pattern #10 (language/instrument identity gate pre-install).
**Score:** 2 × 5 × 4 ÷ 1 = **40.**
**Effort:** S (en-IE locale variant of the listing + one TheSession.org gear-thread post by the developer or a trusted Irish trad contact).
**Description:** Ship an en-IE Play Store locale variant with: "Irish tenor banjo," "GDAE," "octave mandolin," "tenor banjo tuning fork replacement," "session-ready" surfaced. Prepare a TheSession.org gear-thread-ready feature summary (one paragraph + screenshot) for Eileen-shaped users to copy-paste. Mention the no-mic + ad-free + €2.99 one-time IAP triple.
**Acceptance criteria:**
- en-IE locale exists with idiom-correct copy ("trad," "session," "set," not "song"; "fork," not "tuning device").
- Listing surfaces GDAE in screenshot 1 alongside DGBD/CGBD.
- TheSession.org-ready 200-word post template lives at `docs/product/v3/distribution/thesession-post.md`.
- At least one TheSession.org gear thread mentions Banjen organically within 60 days post-ship.

**Risks:** Trad community gatekeeping; a perceived "American tuner app pretending to know Irish trad" will be roasted. Mitigated by having an Irish trad player (Eileen-class) read the listing before ship.
**Dependencies:** T1-3 (GDAE must be home-screen-visible before this becomes credible).

---

### T2-4. Touch Targets ≥60dp + ≥16dp Spacing + 14sp Minimum Type

**Persona reach:** 2/5 explicit (Wendell, Harold) + ambient across the over-60 cohort that defines Banjen's primary acquisition channel.
**Pain links:** #15 (touch targets too small), #16 (reading glasses required), D1/D2/D3 (physical/motor category), #40 (tap-target adjacency).
**Score:** 2 × 4 × 4 ÷ 2 = **16.**
**Effort:** S/M (Compose `Modifier.size(64.dp)` + spacing audit; text size ≥14sp; contrast ratio ≥4.5:1 verified).
**Description:** All interactive touch targets ≥60dp (Jin et al. 2007 — 70% error-rate reduction in older users). All tap-target spacing ≥16dp. All readable text ≥14sp with contrast ≥4.5:1. No tap surface within 24dp of the reserved ad slot. Warm banjo timbre verified through speaker at 12-inch listening distance (Wendell's hearing-aid-removed use case).
**Acceptance criteria:**
- All buttons on home screen ≥60dp measured.
- Spacing between adjacent buttons ≥16dp.
- All labels readable at arm's length without reading glasses, verified by 3 testers age 65+.
- Contrast ratio verified ≥4.5:1 via Material Color tools.
- TalkBack reads each button's localized instrument-context label.

**Risks:** Larger targets reduce visual density. Mitigated by minimal UI — Banjen only needs 4 buttons + a picker pill row.
**Dependencies:** T1-3.

---

### T2-5. Web-Tuner Landing Page (banjen.app/tune) with Install Handoff

**Persona reach:** Indirect — captures users *before* they install (Harold's YouTube path, anonymous "online banjo tuner" Googlers).
**Pain links:** #19 (YouTube-as-tuner), B6 (web tuner SEO captures users before app store), G3 (no web tuner landing handoff).
**Score:** 1 × 4 × 5 ÷ 2 = **10.**
**Effort:** S/M (static HTML/JS web tuner with the 4 DGBD mp3 loops + a "tune on the go — install Banjen" CTA; ~3 days).
**Description:** Build a static web page at `banjen.app/tune` (or equivalent) that plays the same 4 DGBD loops via the Web Audio API. The page is the marketing surface for "online banjo tuner" search traffic (currently captured by guitartuna.com/online-banjo-tuner). The page has one CTA: "Install Banjen for offline tuning, widget, and alt tunings — €2.99 ad-free unlock." Captures the SEO surface Banjen does not own and converts to install.
**Acceptance criteria:**
- Static page loads in <1 second; plays first tone within 1 tap of arrival.
- No tracking other than basic install-attribution.
- Page ranks top-5 for "online banjo tuner" within 90 days, measured via Search Console.
- 10% of page visitors click the install CTA within 30 days of launch.

**Risks:** Builds a free web competitor to the app. Mitigated by leaving alt-tunings, widget, session mode, and the ad-free experience as app-only.
**Dependencies:** T1-1, T1-3.

---

### T2-6. Settings Drawer Zero-Upsell Audit

**Persona reach:** 1/5 explicit (Marcus — "The settings screen tells you what the developer actually cares about"), 5/5 ambient.
**Pain links:** #31 (settings drawer full of upsell), #47 ("Pro" terminology), C6 (trust & permissions).
**Score:** 1 × 4 × 4 ÷ 1 = **4.** (Low score but free to ship and Marcus-cohort gating.)
**Effort:** S (audit + cleanup).
**Description:** Audit the settings/about surfaces for any of: "Pro," "Upgrade," "Premium," animated badges, "Try free," "Subscribe," "Get more" copy. Remove or replace with neutral language. Keep exactly one ad-removal entry. Marcus's 5-minute test fails on settings-screen upsell prompts.
**Acceptance criteria:**
- Settings screen contains: language picker, about/credits, ad-removal IAP entry (if not yet purchased), feedback link. Nothing else commercial.
- No "Pro" or "Premium" string in any localized strings.xml.
- Manual review by Marcus-shaped tester (skeptical Reddit power user) passes the 5-minute install test.

**Risks:** None.
**Dependencies:** T1-5.

---

## Tier 3: Differentiator / P2

Competitive moat. Depth. Retention. Each is built *only after Tier 1 ships clean*.

### T3-1. Adjustable Reference Pitch (A=432–446Hz) — already shipped, expose

**Persona reach:** 1/5 direct (Marcus, non-negotiable reinstall condition) + signals "real musician built this" to all.
**Pain links:** #26 (no adjustable reference pitch), F4 (cross-instrument category).
**Score:** 1 × 3 × 4 ÷ 1 = **3.** (Code already in production per v2 features.md; v3 work is UI polish + Marcus-cohort discoverability.)
**Effort:** S (already shipped; v3 polishes the control surface — collapsed pitch indicator on home screen showing "A=440," tap to expand a slider 432–446Hz in 1Hz increments).
**Description:** v2 already shipped `PitchControl` in `EarActivity.kt`. v3 elevates discoverability: a small pitch indicator on the home screen ("A=440") that taps into a slider. Defaults to 440 to preserve Harold's "I shouldn't have to think about this" experience. Adjusted pitch persists per-tuning-preset (see T3-2).
**Acceptance criteria:**
- Pitch indicator visible on home screen, non-intrusive (≤24sp text in a corner).
- Slider range 432–446Hz, 1Hz increments.
- Default A=440 for new users.
- Marcus can confirm 432Hz playback via spectrum analyzer test.

**Risks:** UI clutter for Harold/Wendell. Mitigated by collapsed-by-default UX.
**Dependencies:** Already in production.

---

### T3-2. Per-Instrument Pitch Memory

**Persona reach:** 1/5 (Marcus) + signal effect.
**Pain links:** #26 sub, F5 (no multi-instrument profile / preset memory), Marcus Q11.
**Score:** 1 × 3 × 4 ÷ 2 = **6.**
**Effort:** S/M (extend SharedPreferences to store pitch-per-preset key, ~2 days).
**Description:** Each tuning preset (DGBD/GDAE/CGBD) remembers its own reference pitch. When Marcus switches from banjo (DGBD @ 440Hz) to cavaquinho (DGBD @ 432Hz — same preset notes, different pitch reference for an A=432 session), the pitch follows automatically. No competing tuner has this. Marcus's exact words: "it remembers my cavaquinho is at 432."
**Acceptance criteria:**
- Each preset stores its last pitch independently.
- Switching presets restores last-used pitch for that preset.
- Default 440 for all presets on first install.

**Risks:** Edge case complexity for users who don't want this — handle by simply defaulting to 440 globally; per-preset behavior only diverges if the user explicitly changes it within a preset context.
**Dependencies:** T3-1.

---

### T3-3. Home-Screen Widget — Tap-to-Tone Without App Launch

**Persona reach:** 1/5 direct (Marcus, non-negotiable reinstall condition) + 2/5 secondary (Harold's 7AM fumble gap, Eileen's session-bag-grab speed).
**Pain links:** #25 (no widget / homescreen access), #45 (slow time-to-first-tone), Marcus Q5/Q11.
**Score:** 3 × 3 × 4 ÷ 3 = **12.** (Marcus reinstall-gating elevates this above pure score implies.)
**Effort:** M (Glance API, ~5 days; v2 marked as shipped — v3 work is to ensure the widget honors the active tuning preset and renders 4 buttons matching the current selection).
**Description:** Glance API widget with 4 buttons (one per string of currently-selected tuning) on the home screen. Tap a button → reference tone loops in-place without launching the app. Tap again or wait 30s → stops. Widget label updates when active tuning changes in-app.
**Acceptance criteria:**
- Widget shows 4 buttons matching active tuning preset.
- Tap → tone plays within 500ms, no app launch animation.
- Widget honors the active pitch (per T3-2).
- No ads visible in the widget surface.
- Time-to-first-tone via widget ≤ 1 second.

**Risks:** Glance API limitations on visual complexity, background playback constraints. Mitigated by keeping widget visual minimal (4 labeled circles).
**Dependencies:** T1-3, T3-1.

---

### T3-4. Session Mode — Auto-Advance Through Strings

**Persona reach:** 3/5 (Eileen — gated; Marcus — workflow; Lúcia — between-set tuning).
**Pain links:** #24 (two-handed tuning), Pattern #6, Eileen Q3.
**Score:** 3 × 3 × 3 ÷ 3 = **3.** (Already shipped per v2 — v3 work is dark-screen mode + invisibility, headphone-detection.)
**Effort:** M (refinement of existing `sessionModeActive` in `EarActivity.kt`).
**Description:** v2 shipped session mode. v3 sharpens to Eileen's social-invisibility spec: usable phone face-down with one earbud, dark screen, vibration between notes. Auto-advance through all 4 strings of active preset with configurable per-string duration (default 8s).
**Acceptance criteria:**
- Phone-face-down operation works (vibration cue between notes; no requirement to look at screen).
- Dark mode auto-engages when session mode active.
- Headphone detection: if earbuds connected, tones go to earbuds at lower default volume.
- Eileen-shaped user can tune all 4 GDAE strings in ≤20 seconds, phone in case.

**Risks:** Headphone-detection edge cases on older Androids. Mitigated by Bluetooth + wired detection via AudioManager.
**Dependencies:** T1-3.

---

### T3-5. Localized "Tuning Friction = Stake Threat" Marketing Copy

**Persona reach:** 3/5 (Harold, Wendell, Eileen — Pattern #7).
**Pain links:** Pattern #7 (tuning friction is a stake threat), H6 (lifecycle).
**Score:** 3 × 3 × 4 ÷ 1 = **9.**
**Effort:** S (copy + screenshot reshoot, ~3 days).
**Description:** Replace "tune faster" / "easy tuning" generic copy with stake-threat-acknowledging copy *that does not surface Harold's hidden cognitive-health motivation*. Examples: "Don't lose the morning." / "Tune in 30 seconds — get back to playing." / "Built for daily practice." For Eileen / en-IE: "Session-ready in 20 seconds." For Lúcia / pt-BR: "Pronto para a roda em 30 segundos."
**Acceptance criteria:**
- Listing copy in all locales reframes value as habit/discipline/community, not speed.
- No copy surface mentions cognitive health, dementia, brain training, or aging — protects Harold's clinical disclosure.
- Eileen-shaped reviewer reads en-IE copy and confirms it lands without cringe.

**Risks:** Overly emotional copy reads as manipulative. Mitigated by understated tone.
**Dependencies:** T1-2.

---

### T3-6. Hearing-Aid-Friendly Audio Profile (Speaker-Clarity Mode)

**Persona reach:** 1/5 explicit (Wendell) + millions in the over-60 hearing-aid cohort.
**Pain links:** #23 (hearing-aid distortion of upper-string D4), D4 (physical/motor).
**Score:** 1 × 3 × 3 ÷ 3 = **1.** (Niche but emotionally outsized; small audio work.)
**Effort:** M (audio engineering: gentle high-frequency roll-off above 4kHz, mid-range emphasis 500–2000Hz where phone speakers excel; ~3 days).
**Description:** Optional "Speaker Clarity" toggle in settings. Optimizes tones for phone-speaker playback at 12 inches with hearing aid removed (Wendell's exact use case). Slightly boost mid-range, gentle roll-off of high frequencies that hearing aids and small speakers distort. Default: off (no behavior change for headphone users).
**Acceptance criteria:**
- Toggle in settings, off by default.
- Audio engineer A/B test: 4 testers with hearing aids prefer the on-state 3/4 trials at 12-inch listening distance.
- No regression for headphone listening.

**Risks:** Audio processing complexity. Mitigated by EQ preset baked into the assets rather than runtime processing.
**Dependencies:** T2-1.

---

## Tier 4: Speculative / Watch

Monitor signal; revisit when conditions trigger.

### T4-1. Visual Tuning Feedback (YIN Pitch Detection)

**Persona reach:** 2/5 (Harold — ear-training catalyst; Marcus — studio confirmation).
**Pain links:** Harold Pain 8 (dormant aural skills), Marcus Q5 secondary.
**Score:** 2 × 3 × 3 ÷ 5 = **3.6.**
**Effort:** L (YIN algorithm, mic permission, in-noise tuning).
**Watch condition:** Defer until T1-1 through T2-6 ship. Adding mic permission breaks T1-4's "no microphone required" claim — must be implemented as a clearly-labeled opt-in flow in a separate surface, never on the default tuning home screen. Likely never a default feature; possibly a separate "Banjen Ear" app.

### T4-2. Ear-Training Progression System

**Persona reach:** 1/5 direct (Harold's dream feature, but he asked for *clinical privacy* on the underlying motivation).
**Pain links:** Pain #39 (ear-training value invisible), Harold Pain 8.
**Score:** 1 × 3 × 4 ÷ 5 = **2.4.**
**Effort:** L (depends on T4-1).
**Watch condition:** Highest emotional ceiling in the persona set but highest privacy risk (cannot surface "cognitive health" framing per Harold's clinical disclosure). Defer until Banjen has 50K+ DAU and a verified Harold-shaped cohort. Consider as a separate companion product, not a Banjen-core feature.

### T4-3. Social-Prescribing Channel — Doctor/OT-Facing Materials

**Persona reach:** 1/5 (Wendell — his doctor said "that's medicine").
**Pain links:** #32 (acquisition-via-family-install gate), G4 (no social-prescribing channel).
**Score:** 1 × 3 × 4 ÷ 4 = **0.75.**
**Effort:** M/L (clinical-credibility materials, a one-pager for GPs / occupational therapists, possibly NHS / Medicare partnership exploration).
**Watch condition:** Promising long-horizon channel; depends on Banjen demonstrating sustained ad-free experience + clinical evidence base for music-tuner-as-habit-scaffold. Revisit in 2027 if there is sustained interest from at least one published clinical pilot.

### T4-4. 5-String Banjo (gDGBD) Support

**Persona reach:** 0/5 in v3 personas (intentionally deprioritized in v2; Jake R. retired).
**Pain links:** Market-analyst.md confirms 5-string is the largest *banjo* segment (~200k–350k globally) but cavaquinho adjacency is higher-leverage.
**Score:** Not applicable — out of v3 scope.
**Watch condition:** Bug exists in current build (`FIVE_STRING_BANJO` in selector but `BanjoStringCanvas` hardcoded to 4 strings). Either fix the bug + ship cleanly, or remove the selector entry. Do not ship as a half-broken feature — that violates trust patterns. Lowest priority. Possibly never ship.

---

## Sequencing Plan — 6 Sprints (≈3 months)

Each sprint is ~2 weeks. Engineering capacity assumed: 1 developer at part-time / full-time equivalent on this product.

### Sprint 1 — Make the Tuning Surface Sacred (Weeks 1–2)
- **Ship:** T1-1 (CLS-safe banner, no-mutation policy), T1-4 (zero-permission audit + listing screenshot 1 update).
- **Dependencies cleared:** None — these are the foundation.
- **Success metric:** CLS score on tuning surface measured at 0.00 across 100 cold-starts; Marcus-shaped re-install test passes the 5-minute filter on a manual reviewer pass.
- **Distribution unlock:** Marcus's reinstall precondition #1 met.

### Sprint 2 — Open the Cavaquinho Door (Weeks 3–4)
- **Ship:** T1-2 (pt-BR listing — "Afinador de Cavaquinho e Banjo"), T1-3 (home-screen tuning picker with localized labels), T2-2 ("No microphone required" listing claim).
- **Dependencies cleared:** Tuning surface sacred (Sprint 1) → safe to share with Lúcia's WhatsApp roda.
- **Success metric:** Banjen appears in top-10 organic for "afinador de cavaquinho" in São Paulo Play Store within 30 days; pt-BR install rate measurably up week-over-week.
- **Distribution unlock:** Lúcia's WhatsApp roda multiplier active (~1,000 cavaquinhistas/share/week).

### Sprint 3 — Monetize Cleanly + Unlock Eileen (Weeks 5–6)
- **Ship:** T1-5 ($2.99 / R$12.90 / €2.99 one-time IAP), T2-6 (settings drawer zero-upsell audit), T2-3 (en-IE locale + GDAE cultural framing + TheSession.org post template).
- **Dependencies cleared:** Tuning surface sacred + tuning picker visible → ad-removal IAP reads as fairness, not ransom; GDAE home-screen → en-IE listing has substance.
- **Success metric:** First revenue. ≥5% of pt-BR users convert IAP within 60 days. At least one TheSession.org gear thread organic mention within 60 days.
- **Distribution unlock:** Eileen's TheSession.org / Hughes' WhatsApp channel; Banjen reputation-safe for sharing.

### Sprint 4 — Reach the Body (Weeks 7–8)
- **Ship:** T2-4 (touch targets ≥60dp, ≥16dp spacing, ≥14sp type, ≥4.5:1 contrast), T2-1 (loop verification + tone duration polish), T3-6 (hearing-aid-friendly speaker-clarity mode).
- **Dependencies cleared:** Settings drawer clean → adding a toggle is safe.
- **Success metric:** 3 testers age 65+ complete a successful tuning session without reading glasses; Wendell-shaped tester (or Camille-shaped install simulator) completes setup in ≤5 minutes.
- **Distribution unlock:** Camille-installable surface; clinical-utility evidence base.

### Sprint 5 — Workflow Depth for Marcus (Weeks 9–10)
- **Ship:** T3-1 (adjustable pitch UI polish), T3-2 (per-instrument pitch memory), T3-3 (home-screen widget — verified honoring active tuning + pitch).
- **Dependencies cleared:** Per-preset pitch requires presets visible (T1-3) + adjustable pitch shipped.
- **Success metric:** Marcus-shaped tester (a Reddit / r/banjo skeptic) reinstalls; widget time-to-first-tone ≤1 second; Banjen mentioned in at least one Reddit thread within 60 days.
- **Distribution unlock:** Marcus's three reinstall conditions all met → potential "best tuner apps for banjo" YouTube episode unlocks.

### Sprint 6 — Distribute, Capture, Hold (Weeks 11–12)
- **Ship:** T2-5 (web-tuner landing page banjen.app/tune), T3-4 (session mode polish — face-down operation, dark mode, headphone-detect), T3-5 (stake-threat-acknowledging copy in all locales).
- **Dependencies cleared:** All Tier 1 / 2 features shipped; safe to draw users in via web SEO.
- **Success metric:** banjen.app/tune ranks top-5 for "online banjo tuner" within 60 days post-launch; session-mode usage measurable in product analytics; install-conversion rate from web page ≥10%.
- **Distribution unlock:** Full referrer-shaped distribution active across all 5 personas.

---

## Effort vs Impact Matrix

```
                              IMPACT (Score 1-125)
                              Low (1-10)    Medium (10-30)   High (30-50)   Critical (50+)
EFFORT
  S (≤1 day)                  T4-3, T4-4    T2-2, T2-6       T2-3 (40)      T1-1 (125)
                                                              T2-4 (16)     T1-2 (50)
                                                                            T1-4 (100)
  S/M (~3-5 days)             T3-2 (6)      T1-5 (50)        T1-3 (37.5)   T1-5 (50)
                              T3-1 (3)      T3-5 (9)
  M (~1-2 weeks)              T3-3 (12)     T3-6 (1)         —              —
                              T3-4 (3)
  L (≥2 weeks)                T4-1 (3.6)    —                —              —
                              T4-2 (2.4)
```

**Read:** The top-right quadrant — high impact, low effort — is dense and dominated by Tier 1 work. T1-1 (CLS-safe banner) and T1-2 (pt-BR listing) are the two highest-impact, lowest-effort actions in the entire roadmap. Everything else compounds *after* these two ship. The bottom-left quadrant is correctly populated with Tier 4 watchlist items — high effort, low impact, defer.

---

## Distribution Activations

Each persona's distribution channel is gated by a specific feature. Ship the feature → activate the channel.

| Persona | Channel | Gating feature(s) | Estimated reach per activation |
|---|---|---|---|
| **Lúcia G.** | WhatsApp roda (60+) → downstream WhatsApp groups → BR YouTubers (Professor Damiro, Cavaquinho na Veia) | **T1-2 (pt-BR listing) + T1-1 (ad-free tuning) + T1-5 (one-time IAP).** All three required: pt-BR listing for discovery, ad-free tuning so she can share without reputation cost, one-time IAP because Cifra Club trauma makes subscription disqualifying. | ~500–1,000 cavaquinhistas in São Paulo per share within 1 week; CAC effectively zero. |
| **Eileen B.** | TheSession.org gear thread + session WhatsApp (12) + Enda Scahill / Lisa Canny mention | **T2-3 (en-IE locale + GDAE framing) + T1-3 (GDAE home-screen visible) + T1-1.** GDAE buried = closed tab; GDAE first-class = installable; one Tuesday at Hughes' ad-flash-free = shareable on TheSession. | ~200+ Irish tenor players globally within 30 days; saturates GDAE segment within a quarter. |
| **Marcus T.** | Reddit (r/banjo, r/WeAreTheMusicMakers) + 2,800-sub YouTube channel + Austin gig-musician network | **T1-1 (CLS-safe banner) + T3-3 (widget) + T3-1 (adjustable pitch).** All three are his explicit reinstall conditions. T3-2 (per-instrument pitch memory) is bonus that he will name when recommending. | ~2,000–5,000 working-musician views per recovered recommendation; cohort migrates as a bloc. |
| **Harold M.** | Ray (banjo teacher) → Thursday jam circle → Banjo Hangout / Old Town School demographic | **T1-1 + T2-4 (touch + readability) + T3-5 (stake-threat copy: "don't lose the morning").** Harold's three-week silence threshold + Ray's bar for credible-tool-to-show-students requires sustained reliability, not a single feature. | ~10–20 per converted teacher over multi-year tenure; high trust, slow ramp, lifetime retention. |
| **Wendell P.** | Camille (daughter) → bandmates (Robert, Marcus the pianist) → emerging social-prescribing channel | **T1-3 (CGBD on home screen) + T1-4 (zero permissions) + T2-4 (touch targets) + T1-5 (one-time IAP simple enough for Camille on a Sunday).** T4-3 (social-prescribing materials) is the long-horizon multiplier. | ~3–6 per Camille-installed instance via bandmates + family + clinical referral; saturates older-plectrum niche. |

**Implication:** Ship Tier 1 + the en-IE locale + the widget and all 5 distribution multipliers activate in parallel. Ship none and Banjen ships into Play Store editorial search — the surface none of these users uses.

---

## Changes from v2

### Carried Over (same intent, refined evidence)
- **Ad placement redesign / sacred tuning screen.** v2 had this as feature #1 with score 10.0; v3 keeps it #1 but tightens to *render-time-reserved banner* (CLS-safe), not just "remove ads." Marcus's named CLS bug is the v3 deliverable.
- **Cavaquinho / Portuguese ASO.** v2 feature #2 (score 10.0); v3 sharpens to a literal title change ("Afinador de Cavaquinho e Banjo") and a human-translated description. Lúcia's interview makes this a precondition, not an ASO experiment.
- **One-time IAP at $2.99–4.99 band.** v2 feature #4; v3 confirms 5/5 personas on this exact band, adds locale variants (R$12.90, €2.99).
- **GDAE preset.** v2 marked shipped; v3 sharpens to *home-screen visibility*, not buried under instrument selector.
- **CGBD preset.** v2 marked shipped; v3 confirms via Wendell.
- **Touch target enlargement.** v2 feature #7; v3 specifies ≥60dp + ≥16dp spacing explicitly (Jin et al. 2007).
- **Adjustable reference pitch.** v2 marked shipped; v3 keeps as discoverability polish.
- **Home-screen widget.** v2 marked shipped; v3 requires it to honor active tuning preset + pitch memory.
- **Session mode.** v2 marked shipped; v3 sharpens to Eileen's social-invisibility spec (face-down, dark mode, headphone-detect).

### Elevated
- **Tuning picker visibility.** v2 had alt-tunings shipped but buried; v3 elevates to a Tier 1 inviolable (Eileen's binary precondition).
- **en-IE locale.** New in v3 as a discrete Tier 2 item (v2 implicitly bundled with GDAE).
- **Web-tuner landing page (banjen.app/tune).** New Tier 2 item in v3 (competitor-analyst gap finding; not present in v2).
- **Settings-drawer zero-upsell audit.** New Tier 2 item (Marcus Q12: "settings screen tells you what the developer cares about").
- **Hearing-aid speaker-clarity mode.** v2 had as Tier 3 differentiator #12; v3 keeps Tier 3 but specifies audio engineering approach (EQ baked into assets, not runtime).
- **Per-instrument pitch memory.** v2 Tier 3 #11; v3 keeps but explicitly maps to T3-1 dependency.

### Demoted / Retired
- **5-string banjo (gDGBD) support.** v2 noted as bugged in production (`FIVE_STRING_BANJO` shows but `BanjoStringCanvas` hardcoded to 4). v3 demotes to Tier 4 watch — either fix or remove the broken selector entry; do not ship half-functional. v3 personas have no 5-string player (Jake retired in v2).
- **Ear training progression.** v2 Moonshot #13; v3 demotes to Tier 4 watch because Harold's clinical disclosure on cognitive health forbids surfacing the underlying motivation in copy — the feature exists in tension with the privacy constraint. Defer until a separate companion-product framing emerges.
- **Visual tuning feedback / YIN.** v2 Moonshot #14; v3 demotes to Tier 4 watch because mic permission breaks T1-4 architectural claim ("no microphone required"). Cannot be a default feature; possibly a separate app.

### New in v3 (no v2 antecedent)
- **CLS-safe banner spec.** v2 said "no ads on tuning screen"; v3 says "reserve bottom 64dp at first paint with audio-incapable banner." Different deliverable.
- **pt-BR listing with literal title containing 'Cavaquinho.'** v2 said "Portuguese ASO"; v3 says "title literally contains 'Cavaquinho.'" Different deliverable.
- **en-IE locale.** New.
- **Web-tuner landing page.** New (competitor-analyst gap finding).
- **TheSession.org post template + WhatsApp share asset + Camille-facing setup mini-guide.** Persona-specific referral assets — new in v3 (Pattern G2).
- **Social-prescribing channel materials.** Tier 4 watch — new in v3 (Wendell's clinical disclosure).
- **Stake-threat copy ("don't lose the morning").** New in v3 (Pattern #7).

---

## Acceptance Criteria for "Ready for Design Brief"

A feature is ready to flow into Phase 3 (design brief) when all of the following are true:

1. **Pain trace is unambiguous.** Feature names at least one Pain ID from `pains.md` and one persona from `user-personas.md`. No intuition-only features.
2. **Persona reach is verified.** The feature's claimed reach matches the pain's reach in the severity matrix — if a feature claims to serve 5/5 personas, the underlying pain must have reach 5/5.
3. **Effort is sized in S/M/L with a justification.** S = ≤1 day or no code. M = 1–2 sprints. L = ≥2 sprints or new subsystem.
4. **Inviolable Constraints are checked.** The feature does not violate any of pains.md's 5 inviolable constraints (no ad on tuning surface; no mic permission; no subscription; tuning picker visible on home screen; no registration/onboarding before first tone).
5. **Acceptance criteria are testable.** Each feature lists ≥3 acceptance criteria written as observable behaviors (timings, measurable thresholds, presence/absence of UI elements), not aspirations.
6. **Risks name a specific assumption to validate.** No "may not work for some users" hand-waving — each risk names a specific user segment, environment, or technical constraint with a mitigation strategy.
7. **Dependencies are explicit.** Each feature names its prerequisite features by ID. The sequencing plan must respect these.
8. **Localization plan is named.** The feature's strings exist (or will exist) in en, es, pt-BR, it, en-IE. No English-only shipping for any user-facing change.
9. **Distribution implication is named.** Which persona's referrer channel does this feature unlock or protect? If the answer is "none," the feature is probably Tier 3 or below.
10. **Reversibility is named.** Can this feature be rolled back via Remote Config / feature flag in <1 hour? If not, ship behind an RC flag.

A feature passing all 10 is ready for a Phase 3 design brief. A feature failing any one of these returns to the strategist for refinement.

---

STATUS: complete
OUTPUT: /Users/dan/projects/banjen/docs/product/v3/features.md
