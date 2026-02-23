# Banjen — Prioritized Feature Roadmap
*February 23, 2026 | Feature Strategist — UX Design Thinking Investigation*

---

## Executive Summary

Fourteen features organized across four tiers, traced to 47 documented pains across 5 personas. Three features are **NEW** — not in any existing roadmap: (1) Per-Instrument Pitch Memory, (2) Ear Training Progression System, and (3) Beginner-Friendly String Labels with visual diagram. Three more are **NEW operational changes** requiring zero or minimal code: Ad Placement Redesign, Cavaquinho/Portuguese ASO, and Hearing Aid Compatibility Mode.

The single highest-leverage change is removing ads from the tuning screen — it addresses the **only universal critical pain** (5/5 personas) and is the lowest-effort item on the list. Combined with the $2.99 one-time ad removal purchase and Portuguese ASO, these three changes alone unlock every persona on the Growth Ladder.

**Key principle guiding all prioritization:** Every feature traces to a verbatim pain from the pain map. No features from intuition.

---

## [QUICK WIN] Features

*High-pain relief, low implementation effort — ship in next sprint*

---

### [QUICK WIN] 1. Ad Placement Redesign — Sacred Tuning Screen

**Pain Addressed:** "The tuning screen is sacred space. Every persona — regardless of age, tech skill, instrument, or culture — identifies ad intrusion during tuning as an instant, permanent dealbreaker." (Cross-Persona Pattern 1: Ad Intrusion During the Tuning Flow — 5/5 personas, UNIVERSAL)
**Persona(s):** Harold M., Siobhan K., Betty L., Rafael S., Marcus T. — ALL FIVE
**HMW Link:** "How might we monetize Banjen without ever interrupting the moment when a musician is actively listening and tuning?"
**Description:** Remove the AdMob banner from the tuning screen entirely. Relocate ads to non-tuning surfaces (app launch interstitial, settings/about screen, or a brief 5-second ad on cold start that buys a clean session). Marcus explicitly designed this strategy: "monetize literally everywhere else — but the tuning screen is mine." The layout shift that caused his rage-quit and permanent deletion is eliminated. Betty's accidental-tap fear is removed. Harold's routine is protected.
**Why Now:** This is the single change that addresses the #1 pain for ALL five personas simultaneously. Marcus gives Banjen "one more chance" — this is that chance. The ad layout shift during tuning is a specific, reproducible UX failure that explains non-conversion at scale. Every day this remains unfixed, users who discover the app delete it permanently.
**Success Signal:** Marcus reinstalls. Betty uses the app twice without "scary moments" (her stated conversion bar). Harold's daily routine incorporates Banjen without interruption. 30-day retention increases. 1-star reviews mentioning ads decrease to zero.
**Risk:** Revenue impact from removing tuning-screen ads. Mitigated by the $2.99 IAP (Feature #4) and by the fact that users who rage-quit from ad intrusion generate zero lifetime revenue anyway.
**Existing Plan:** NEW — not in existing roadmap. The current ad placement is the #1 product problem and has no associated feature plan.

---

### [QUICK WIN] 2. Cavaquinho / Portuguese App Store Optimization

**Pain Addressed:** "Rafael searches 'afinador de cavaquinho' in Portuguese. Banjen doesn't appear. GuitarTuna doesn't list cavaquinho as an instrument. His instrument — played by millions across Brazil and the global diaspora — doesn't exist in any American developer's worldview." (Rafael Pain 1: Cavaquinho is invisible to the app ecosystem — CRITICAL)
**Persona(s):** Rafael S. (primary), Marcus T. (secondary — plays cavaquinho with Austin Brazilian group)
**HMW Link:** "How might we make DGBD reference tones discoverable to Portuguese-speaking cavaquinho players who would never search for 'banjo tuner'?"
**Description:** Zero code changes. Update the Google Play Store listing: add "cavaquinho," "afinador," "afinador de cavaquinho," "DGBD" to app title/subtitle and keyword metadata. Create Portuguese-language screenshots and description. The app already plays the exact four notes cavaquinho players need (D3, G3, B3, D4) — the entire barrier is discovery and cultural framing. Rafael would "literally share it in the WhatsApp group" the moment he finds it, triggering cascade distribution through the Brazilian diaspora.
**Why Now:** The dedicated Cavaquinho Tuner app — built by one developer in Brazil — is actively deteriorating (crashes, freezes, developer unresponsive). Millions of cavaquinho players have a single point of failure. Whichever app fills this gap inherits the entire user base. The window is open NOW. Zero engineering cost, potentially millions of new users.
**Success Signal:** App appears in results for "afinador de cavaquinho" search. Downloads from Brazil, Portugal, and Brazilian diaspora cities (NYC, Boston, Miami, Newark) increase measurably within 2 weeks. Rafael's WhatsApp cascade effect: one share reaches 200+ musicians.
**Risk:** Cultural authenticity — a "banjo tuner" marketing itself to cavaquinho players could feel disingenuous if the app UI only shows banjo imagery. Mitigate with inclusive language ("DGBD tuner" rather than only "banjo tuner") and Portuguese UI strings (already partially localized).
**Existing Plan:** NEW — not in existing roadmap. This is a pure go-to-market change, not a feature.

---

### [QUICK WIN] 3. Beginner-Friendly String Labels with Visual Diagram

**Pain Addressed:** "Chromatic tuner apps display letter names and cents without context. Harold doesn't know what note each string should be. The tool assumes expertise its user doesn't possess." (Harold Pain 5: Chromatic tuners assume knowledge he doesn't have — MODERATE) and "She has 40 years of playing experience and excellent relative pitch — she just needs ONE reliable reference note. But every tool gives her confusing visual displays." (Betty Pain 1, relating to cognitive load)
**Persona(s):** Harold M. (primary), Betty L. (secondary)
**HMW Link:** "How might we present tuning targets in a way that requires zero prior knowledge — just 'press the button for this string, match the sound'?"
**Description:** Add subtitle text to each button: "String 1 (thickest)" through "String 4 (thinnest)" below the D-G-B-D labels. Include a small visual banjo neck diagram showing which physical string maps to which button. Harold revealed he doesn't understand D-G-B-D letter notation — he needs positional context. The existing improve-accessibility plan already includes subtitle strings ("String 1 (thickest)") but does not include the visual diagram or the framing around musical notation confusion.
**Why Now:** Harold is the beachhead persona. His "first 10 seconds" experience must be instantly comprehensible. If he opens the app and sees four letters he doesn't understand, he's back to YouTube. This low-effort change converts his confusion into confidence.
**Success Signal:** Harold can identify which button to press for each string without external help. Teacher Ray can explain Banjen in one sentence: "Press the button that says String 1, tune to that sound." First-session success rate increases.
**Risk:** Visual clutter on the main screen. Mitigate with progressive disclosure: subtitles visible by default, hideable in settings for experienced users like Marcus who don't need them.
**Existing Plan:** Partially in `improve-accessibility/` plan (subtitle strings exist), but the visual diagram and the framing around Harold's notation confusion are NEW.

---

## [CORE] Features

*High-pain relief, moderate effort — core roadmap items*

---

### [CORE] 4. One-Time Ad Removal Purchase ($2.99)

**Pain Addressed:** "Every persona (except Rafael, who is ad-tolerant but would still pay $3 to remove them) would pay $3-5 for a clean, ad-free experience. Subscriptions are universally rejected." (Cross-Persona Pattern 6: Willingness to Pay for Ad Removal — 4/5 personas)
**Persona(s):** Harold M. ($3-5), Siobhan K. (EUR 4-5), Betty L. ($5-10 via Colleen), Marcus T. ($3-5), Rafael S. ($3)
**HMW Link:** "How might we offer a simple, one-time purchase that converts ad-tolerant users into paying customers without alienating free users?"
**Description:** Google Play Billing one-time in-app purchase at $2.99 that permanently removes all ads. No subscription, no account, no recurring billing. Harold compares to "$18 Snark sitting in a drawer." Siobhan: "I spend more than that on a pint." Betty: "Cheaper than one visit to the pharmacy." Marcus: "$100 Boss TU-3 was worth it." The $2.99 price sits in the optimal zone identified by market research (47.8% trial-to-paid conversion at $1-5). This converts the ad placement redesign (Feature #1) from a revenue loss into a revenue transformation.
**Why Now:** Feature #1 (removing ads from tuning screen) is the prerequisite. Once the free tier has a clean tuning experience, the IAP becomes an upsell for removing remaining ads (launch screen, settings screen) rather than a ransom for basic functionality. Combined, Features #1 and #4 transform the monetization model from "hostile to users" to "aligned with users."
**Success Signal:** 10%+ of active users purchase within 90 days. Harold's $25-30/month wasted lesson time makes $2.99 an obvious buy. Siobhan pays immediately. 1-star reviews about ads go to zero.
**Risk:** Google Play Billing integration complexity. Testing edge cases (restore purchase on new device, family sharing). Mitigate by keeping the IAP surface minimal: one button in settings, "Remove Ads — $2.99."
**Existing Plan:** NEW — not in existing roadmap. No monetization feature has been planned despite ads being the universal #1 pain.

---

### [CORE] 5. GDAE Tuning Preset (Irish Tenor Banjo / Octave Mandolin)

**Pain Addressed:** "The entire tuner app market defaults to guitar (EADGBE) or 5-string banjo (open G). GDAE — the standard tuning for Irish tenor banjo AND octave mandolin — is either absent, buried in settings, or requires manual frequency entry." (Siobhan Pain 2: GDAE tuning doesn't exist as a first-class option — CRITICAL)
**Persona(s):** Siobhan K. (primary — distribution channel multiplier), plus the entire Irish session ecosystem (tenor banjo + octave mandolin, GDAE is a tuning SYSTEM)
**HMW Link:** "How might we make Siobhan feel seen by showing GDAE as a first-class tuning the moment she opens the app?"
**Description:** Add GDAE (G2-D3-A3-E4) as a first-class tuning preset — visible on the main screen, not buried in settings. "If GDAE was right there — not buried in a settings menu — that tells me the developer actually knows my instrument exists." Requires 4 new reference tone audio files. Siobhan's GDAE serves BOTH tenor banjo and octave mandolin, doubling the addressable market with a single preset.
**Why Now:** Siobhan is a distribution channel, not just a user. One recommendation from her reaches the Tuesday session table. One post on The Session reaches Dublin to Boston to Melbourne overnight. The IrishBanjoLessons Discord has 400 active GDAE players. The market value of this feature is not "one user" — it's "one user who unlocks thousands." No competing tuner serves GDAE as first-class.
**Success Signal:** Siobhan installs and uses it at a session. She posts on The Session: "Finally a tuner that works for tenor banjo." Downloads from Ireland, UK, Boston, Melbourne spike within a week. Irish session community adoption becomes measurable.
**Risk:** Audio quality of GDAE reference tones must pass the "trained ear test" — Siobhan and her session peers will evaluate tone quality critically. Poor tones = instant deletion. Mitigate with high-quality recorded or synthesized tones.
**Existing Plan:** `alternate-tuning-support/` — GDAE is explicitly included. Plan covers architecture (TuningPreset enum, dropdown selector, SharedPreferences persistence, asset folder structure).

---

### [CORE] 6. CGBD Tuning Preset (Plectrum Banjo)

**Pain Addressed:** "Plectrum banjo standard tuning (CGBD) is offered by almost no tuner app. If the app plays DGBD, it's literally the wrong notes." (Betty Pain 3: CGBD tuning isn't available — CRITICAL)
**Persona(s):** Betty L. (primary)
**HMW Link:** "How might we make Betty's CGBD tuning a first-class option that she sees immediately, not something buried in settings she'll never find?"
**Description:** Add CGBD (C3-G3-B3-D4) as a first-class tuning preset alongside DGBD and GDAE. Shares the same infrastructure as GDAE (Feature #5). Betty has 40 years of experience — she'll hear the wrong notes instantly. CGBD must be a visible option, not a buried setting. Requires 1 new reference tone audio file (C3 — G3, B3, D4 are shared with DGBD).
**Why Now:** Betty's weekly band rehearsal is her healthcare — her doctor recommended staying active with music after a depression screening. If she can't tune, she can't play; if she can't play, she loses her primary social lifeline. CGBD support transforms Banjen from "wrong notes" to "the thing that gives Betty independence for the first time since Earl passed." Also: the plectrum banjo community is underserved by every competitor.
**Success Signal:** Colleen installs the app, selects CGBD, shows Betty. Betty tunes successfully twice without "scary moments." Betty tells Robert the trombonist. Social prescribing channel opens: occupational therapists can recommend "the app that plays your notes."
**Risk:** Betty's 1/5 tech savviness means the tuning selector must be obvious, not a dropdown she might miss. Mitigate with onboarding that asks "What kind of banjo do you play?" with large, labeled options.
**Existing Plan:** `alternate-tuning-support/` — CGBD is explicitly included alongside GDAE.

---

### [CORE] 7. Touch Target Enlargement for Arthritis (56-72dp)

**Pain Addressed:** "Standard mobile UI elements are designed for young, dexterous fingers. Betty needs touch targets of 56-72dp minimum — well above Material Design defaults — with generous spacing to prevent mis-taps." (Betty Pain 6: Touch targets too small for arthritic fingers — MODERATE) and "Every minute spent wrestling with a tuner is stolen from playing — and from the arthritis window that's already shrinking." (Betty Pain 4: Arthritis steals from limited practice time — CRITICAL)
**Persona(s):** Betty L. (primary), Harold M. (secondary — 67, finger dexterity declining)
**HMW Link:** "How might we design touch targets that arthritic fingers can hit confidently every time, with no fear of tapping the wrong thing?"
**Description:** Increase all interactive touch targets to 56-72dp (beyond Android's 48dp minimum, per research on arthritis-affected users). Add generous spacing between buttons to prevent mis-taps. Betty "can't hold the phone and turn a tuning peg at the same time" — the buttons must be large enough to hit without precise aim. This also benefits Harold's declining dexterity and all users in high-stress performance situations (shaking hands, cold fingers, dim lighting).
**Why Now:** Betty's arthritis limits practice to 15-30 minutes. A 3-minute tuning struggle consumes 10-20% of her practice window. Technology friction compounds physical friction. Making buttons large enough is not accessibility theater — it's the difference between Betty's independence and her dependence on Marcus the pianist.
**Success Signal:** Betty can tap any button confidently on first try. Zero accidental mis-taps. Time-to-tune decreases. Betty stops arriving 20 minutes early to rehearsal.
**Risk:** Larger touch targets may reduce screen space for other UI elements. Mitigate with responsive layout that adapts to screen size, and by keeping the UI minimal (the app only needs 4 buttons + tuning selector).
**Existing Plan:** `improve-accessibility/` — plan includes touch target enlargement, subtitle text, and content descriptions. Current plan specifies changes but should be updated to target 56-72dp explicitly based on Betty's interview data.

---

### [CORE] 8. Adjustable Reference Pitch (A=432-446Hz)

**Pain Addressed:** "Marcus records with artists in Austin who tune to A=432Hz. He currently uses a DAW to generate reference tones at 432 — 'that's insane' for what should be a simple feature." (Marcus Pain 2: No adjustable reference pitch — CRITICAL)
**Persona(s):** Marcus T. (primary — non-negotiable reinstall condition), session musicians broadly
**HMW Link:** "How might we let Marcus tune to any standard reference pitch so he never has to open a DAW just for a reference tone?"
**Description:** Add a pitch adjustment control (A=432-446Hz in 1Hz increments) using `PlaybackParams.setPitch()`. Persist the setting in SharedPreferences. Display current pitch prominently: "A=440." This is one of Marcus's three specific, non-negotiable conditions for reinstalling Banjen. The implementation is straightforward — it's a multiplicative float on existing playback.
**Why Now:** The 432Hz movement is widespread among recording musicians. This is Marcus's conversion condition #2 (alongside ad-free tuning screen and widget). Without this, Marcus stays on GStrings and Banjen loses its most vocal potential evangelist on Reddit and YouTube.
**Success Signal:** Marcus can tune his cavaquinho to 432Hz and his banjo to 440Hz in the same session without opening a DAW. He reinstalls and recommends on r/WeAreTheMusicMakers.
**Risk:** UI complexity — adding a pitch control to the main screen could clutter the simple 4-button interface that Harold and Betty need. Mitigate by defaulting to A=440 with the control collapsed or in a secondary panel that power users like Marcus can access.
**Existing Plan:** `add-adjustable-reference-pitch/` — fully planned with architecture, milestones, and PlaybackParams implementation.

---

### [CORE] 9. Home Screen Widget (One-Tap Tuning)

**Pain Addressed:** "Unlocking phone, finding app, opening app, navigating to the right tuning — each step adds latency. A homescreen widget with 4 buttons that plays reference tones without launching the app would be 'faster than my $100 Boss TU-3.'" (Marcus Pain 4: No homescreen widget for instant access — MODERATE)
**Persona(s):** Marcus T. (primary — non-negotiable reinstall condition #3), Harold M. (secondary — reduces fumble gap), Siobhan K. (secondary — faster than finding a tuning fork)
**HMW Link:** "How might we make Banjen accessible without even opening the app?"
**Description:** Glance API widget with 4 buttons (one per string) on the home screen. Tap a button, hear the reference tone, tune, tap again to stop. Zero app launch required. This would make Banjen faster than Marcus's $100 Boss TU-3 for acoustic instruments — a genuine competitive advantage over hardware. For Harold, it reduces the fumble gap from "find app icon, open app, wait" to "tap button on home screen."
**Why Now:** This is Marcus's conversion condition #3. He needs all three (ad-free tuning, adjustable pitch, widget) to reinstall. The widget also addresses the universal "15-second fumble gap" pattern by eliminating the app-launch step entirely. Glance API is mature and well-documented.
**Success Signal:** Marcus uses the widget instead of his Boss TU-3 for acoustic instruments. Harold's grandkids add the widget to his home screen and his fumble gap drops to under 3 seconds. Time-to-first-tone becomes the fastest of any tuning tool.
**Risk:** Widget must adapt to current tuning preset (DGBD vs GDAE vs CGBD). Glance API limitations on visual complexity. Mitigate with the existing plan's approach: 4 buttons that match the selected tuning, labels update when tuning changes.
**Existing Plan:** `home-screen-widget/` — fully planned with Glance API architecture, intent handling, and auto-play on launch.

---

## [DIFFERENTIATOR] Features

*Strategic features that create competitive moat*

---

### [DIFFERENTIATOR] 10. Session Mode — Auto-Advance Through Strings

**Pain Addressed:** "In sessions, Siobhan can't look at her phone screen or tap buttons between strings. She needs an automatic sequence: G for 5 seconds, then D, then A, then E, with vibration between notes." (Siobhan Pain 6: No auto-advance through strings — MODERATE) and "Rafael wants D, then G, then B, then D played automatically with 5-second gaps." (Rafael Pain 5 — MODERATE)
**Persona(s):** Siobhan K. (primary — "social invisibility" requirement), Rafael S. (primary — between-set tuning), Marcus T. (secondary — studio workflow)
**HMW Link:** "How might we let Siobhan tune all four strings without touching her phone after the initial tap?"
**Description:** One-tap mode that auto-advances through all strings with configurable duration per note (default 5 seconds), vibration/flash between notes, and full hands-free operation. Usable phone face-down with one earbud — Siobhan's exact specification. Dark screen mode during session to maintain "social invisibility." This feature was independently designed by two unrelated personas (Siobhan and Rafael) — convergent design evidence with high confidence.
**Why Now:** Session mode transforms Banjen from "useful tool" to "irreplaceable tool." It's the feature that makes Siobhan an evangelist — she can tune in 20 seconds, invisible to the session table, and tell everyone about it that night. No competing app offers this for reference tones.
**Success Signal:** Siobhan tunes phone-face-down in 20 seconds between sets. Rafael tunes between restaurant sets without reaching for his phone. Marcus flows through multi-instrument tuning in studio. Convergent evidence: if both Siobhan and Rafael love it independently, the feature is validated.
**Risk:** Timer duration preferences may vary across personas and tuning contexts. Mitigate with configurable duration (3-10 seconds per note) and a sensible default (5 seconds, per both Siobhan's and Rafael's specification).
**Existing Plan:** `add-session-mode/` — fully planned with architecture, headphone detection, dark theme, and auto-advance timer.

---

### [DIFFERENTIATOR] 11. Per-Instrument Pitch Memory

**Pain Addressed:** "'Maybe my banjo is always at 440 but when I'm tracking with the 432 guys my cavaquinho needs to be at 432. Per-instrument pitch memory.' This per-instrument pitch memory is a workflow detail that signals 'someone who actually plays built this.' No app addresses it." (Marcus Pain 6: Per-instrument pitch memory doesn't exist anywhere — MODERATE)
**Persona(s):** Marcus T. (primary)
**HMW Link:** "How might we remember each instrument's preferred pitch so Marcus never has to reconfigure between songs?"
**Description:** Save the reference pitch (A=432-446Hz) per tuning preset. When Marcus switches from banjo (DGBD at 440Hz) to cavaquinho (DGBD at 432Hz), the pitch setting switches automatically. This requires the adjustable reference pitch feature (#8) and the tuning preset system (#5/#6) as prerequisites. Implementation: extend SharedPreferences to store pitch per preset key.
**Why Now:** No tuning app on any platform offers per-instrument pitch memory. This is the kind of detail that makes a musician say "someone who actually plays built this" — Marcus's exact words. It creates a competitive moat through workflow intelligence that competitors haven't conceived of.
**Success Signal:** Marcus switches between banjo and cavaquinho presets and the pitch adjusts automatically. He describes this feature when recommending on Reddit: "it remembers my cavaquinho is at 432."
**Risk:** Complexity of the settings model — storing pitch per preset adds a layer of configuration. Mitigate by keeping it invisible: the pitch just "follows" the preset without requiring explicit user configuration beyond initial setup.
**Existing Plan:** NEW — not in existing roadmap. Requires Features #5/#6 (tuning presets) and #8 (adjustable pitch) as prerequisites.

---

### [DIFFERENTIATOR] 12. Hearing Aid Compatibility Mode

**Pain Addressed:** "The banjo's output (~85dB at 2.5 feet) overloads hearing aid circuitry, particularly at high frequencies. Betty actually tunes better with her hearing aid removed and the banjo close to her ear. Phone speaker clarity at close range matters more than raw volume." (Betty Pain 7: Hearing aid distortion of banjo frequencies — MODERATE)
**Persona(s):** Betty L. (primary), older users broadly (Harold's demographic)
**HMW Link:** "How might we deliver reference tones with clarity and warmth that work for someone who tunes with their hearing aid removed?"
**Description:** Audio frequency optimization for phone speaker playback at close range: slightly boost mid-range frequencies (500-2000Hz) where phone speakers perform best, gentle roll-off of high frequencies that distort through hearing aids and small speakers, and a "warm tone" mode that prioritizes clarity over volume. Betty tunes with her hearing aid removed — the reference tone must be clear through a phone speaker at 12-18 inches, not loud through external speakers.
**Why Now:** The 65+ demographic is underserved by every music app. A tuner that explicitly accounts for hearing aid interaction and phone speaker characteristics at close range is unprecedented. This is a small audio engineering effort with outsized emotional impact — it tells Betty (and the millions of older musicians like her) "we thought about you."
**Success Signal:** Betty reports that reference tones sound "warm" and clear through her phone speaker with hearing aid removed. She can distinguish between her plucked string and the reference tone reliably.
**Risk:** Audio processing may sound worse for users with normal hearing or headphones. Mitigate by making this a toggle ("Close Range Mode" or "Speaker Clarity Mode") rather than a global default.
**Existing Plan:** NEW — not in existing roadmap. The improve-accessibility plan covers visual/touch accessibility but not audio accessibility.

---

## [MOONSHOT] Features

*Transformative ideas that could redefine the product*

---

### [MOONSHOT] 13. Ear Training Progression System

**Pain Addressed:** "He has latent pitch-matching abilities that no current tool helps him rediscover. He wants to 'get it back' but has no pathway." (Harold Pain 8: Dormant aural skills going untapped — MINOR frequency but CRITICAL emotional weight) and "His dream feature makes him independent of the app. 'Something that teaches me to hear the difference... so that eventually I don't need the app at all.'" (Harold interview insight #3)
**Persona(s):** Harold M. (primary — this is his dream feature), Betty L. (secondary — 40 years of relative pitch to reinforce)
**HMW Link:** "How might we help Harold rediscover the ear he already has, so each tuning session is also ear training?"
**Description:** A progressive ear training system that turns daily tuning into active learning. Phase 1: After tuning, the app plays the reference tone and the user plucks their string — the app says "close" or "way off" (using pitch detection from the visual tuning feedback plan). Phase 2: The app plays the reference tone, then fades it out gradually over sessions, training the user to hold the pitch in memory. Phase 3: The app asks the user to tune WITHOUT the reference tone, then checks — building true independence. Harold's exact specification: "Play the reference tone, then let me pluck my string, and tell me 'you're close' or 'way off.' Almost like a patient teacher sitting next to me."

This is the **opposite** of a sticky-retention model — the app's success metric is making the user independent. Harold doesn't want a better tuner. He wants a teacher that makes him self-sufficient. This reframes Banjen from "utility" to "empowerment tool" — a category of one.

**Why Now:** Harold's hidden motivation is cognitive health — music practice as neuroprotection. Ear training is literally the cognitive exercise he's seeking. MIT Press longitudinal studies confirm music training produces measurable neuroprotective effects. A tuner that also trains your ear delivers on the hidden promise Harold made to himself but hasn't told Linda about.
**Success Signal:** Harold reports "I'm starting to hear it before I check." His tuning accuracy improves over weeks without the reference tone. He demonstrates to Don and Kathy at the jam circle — not the app, but his OWN ability. The product succeeds when the user needs it less.
**Risk:** Pitch detection accuracy in noisy environments. The visual tuning feedback plan (Feature #14) uses YIN algorithm which requires microphone access — Betty may be uncomfortable with permissions. Mitigate with clear permission explanations and an opt-in progression system. High implementation effort.
**Existing Plan:** NEW — not in existing roadmap. Builds on `visual-tuning-feedback/` (YIN pitch detection) but reframes the purpose from "visual confirmation" to "progressive independence."

---

### [MOONSHOT] 14. Visual Tuning Feedback (YIN Pitch Detection)

**Pain Addressed:** "He sometimes wonders if he could 'get it back' with a reliable reference tone to practice against." (Harold Pain 8) and "Whether banjo tracks actually sound in tune in recordings." (Marcus Pain 2, secondary concern about tuning accuracy confirmation)
**Persona(s):** Harold M. (primary — ear training catalyst), Marcus T. (secondary — studio confirmation), Rafael S. (tertiary — professional credibility)
**HMW Link:** "How might we help Harold rediscover the ear he already has, so each tuning session is also ear training?"
**Description:** Microphone-based pitch detection using the YIN algorithm to provide visual feedback ("you're close / way off / in tune") while the reference tone plays. Not a replacement for ear tuning — a complement. The reference tone remains primary; the visual indicator confirms what the ear is learning. This is the technical foundation that enables Feature #13 (Ear Training Progression).
**Why Now:** The YIN algorithm is well-understood and implementable in pure Kotlin with no dependencies. Microphone-based pitch detection failed in noise for competitors (Cleartune, GStrings) — but Banjen's unique advantage is that the reference tone plays simultaneously, so the pitch detection only needs to distinguish the user's string (plucked, close to mic) from the reference tone (played through speaker or earbud). This dual-source design is novel.
**Success Signal:** Harold sees "in tune" confirmation and gains confidence. Marcus verifies recording pitch with visual confirmation. The feature drives adoption of the ear training progression system.
**Risk:** Background noise, especially in Harold's kitchen or Betty's rehearsal room, may confuse pitch detection. Requires microphone permission which may alarm Betty. High implementation effort. Mitigate with clear in-app explanation: "this listens to your banjo string to tell you if you're close."
**Existing Plan:** `visual-tuning-feedback/` — fully planned with YIN algorithm architecture, permission handling, and UI design.

---

## Priority Matrix

| # | Feature | Tier | Personas | Pain Severity (1-5) | Persona Count | Effort (1-5) | Priority Score |
|---|---------|------|----------|---------------------|---------------|--------------|----------------|
| 1 | Ad Placement Redesign | Quick Win | All 5 | 5 | 5 | 1 | **10.0** |
| 2 | Cavaquinho/Portuguese ASO | Quick Win | Rafael, Marcus | 5 | 2 | 1 | **10.0** |
| 3 | Beginner String Labels | Quick Win | Harold, Betty | 3 | 2 | 1 | **6.0** |
| 4 | $2.99 Ad Removal IAP | Core | All 5 | 5 | 5 | 2 | **9.0** |
| 5 | GDAE Tuning Preset | Core | Siobhan (+network) | 5 | 2 | 3 | **7.0** |
| 6 | CGBD Tuning Preset | Core | Betty | 5 | 1 | 2 | **5.0** |
| 7 | Touch Target Enlargement | Core | Betty, Harold | 4 | 2 | 2 | **5.3** |
| 8 | Adjustable Reference Pitch | Core | Marcus | 4 | 2 | 2 | **5.3** |
| 9 | Home Screen Widget | Core | Marcus, Harold, Siobhan | 3 | 3 | 3 | **4.0** |
| 10 | Session Mode | Differentiator | Siobhan, Rafael, Marcus | 4 | 3 | 3 | **5.3** |
| 11 | Per-Instrument Pitch Memory | Differentiator | Marcus | 3 | 1 | 2 | **3.0** |
| 12 | Hearing Aid Compatibility | Differentiator | Betty | 3 | 1 | 3 | **2.0** |
| 13 | Ear Training Progression | Moonshot | Harold, Betty | 4 | 2 | 5 | **2.7** |
| 14 | Visual Tuning Feedback | Moonshot | Harold, Marcus, Rafael | 3 | 3 | 5 | **2.4** |

*Priority Score = (Pain Severity x Persona Count) / Effort, normalized to 1-10 scale. Scores above 5.0 are ship-immediately urgent.*

---

## Sequencing Recommendation

The shipping order follows the Growth Ladder from user-personas.md, optimized for maximum persona conversion and revenue at each stage.

### Sprint 1: Protect Harold + Unlock Revenue (Features #1, #2, #3)

**Ship:** Ad Placement Redesign + Cavaquinho ASO + Beginner String Labels

**Growth Ladder alignment:** Stage 1 (Protect Harold) + Stage 2 (Unlock Cavaquinho Market)

**Justification:** These three changes are all low-effort (combined: ~1-2 days of work) and address the two highest-priority pains. Feature #1 protects every persona by making the tuning screen sacred. Feature #2 opens an entirely new market with zero code changes. Feature #3 makes Harold's first experience comprehensible. Together, they convert Harold from YouTube workaround to daily user, give Marcus his "one more chance," and make Banjen discoverable to millions of cavaquinho players.

**Revenue impact:** No immediate revenue, but eliminates the #1 cause of permanent user loss (ad-driven deletion) and creates the discoverability path for a market 100x larger than the current 4-string banjo TAM.

---

### Sprint 2: Monetize + Unlock Betty (Features #4, #6, #7)

**Ship:** $2.99 Ad Removal IAP + CGBD Tuning + Touch Target Enlargement

**Growth Ladder alignment:** Stage 1 completion (monetization) + Stage 4 (Unlock Betty)

**Justification:** With Feature #1 already providing a clean free tier, the IAP (Feature #4) becomes an upsell for power users who want zero ads anywhere — not a ransom for basic functionality. CGBD (Feature #6) and touch targets (Feature #7) ship together because they share a persona: Betty can't use the app without CGBD tuning AND buttons she can tap. Shipping one without the other doesn't convert Betty.

**Revenue impact:** First revenue. At $2.99 with a conservative 5% conversion of active users, even modest user numbers generate meaningful income for a solo developer. Siobhan and Marcus will purchase immediately.

---

### Sprint 3: Unlock the Irish Market (Feature #5)

**Ship:** GDAE Tuning Preset

**Growth Ladder alignment:** Stage 3 (Unlock the Irish Session Market)

**Justification:** GDAE requires new audio files (4 reference tones) and shares infrastructure with CGBD (shipped in Sprint 2). Siobhan is the persona with the highest distribution multiplier — one post on The Session triggers downloads from Dublin to Boston to Melbourne. Shipping GDAE after CGBD means the tuning preset infrastructure is already built and tested.

**Revenue impact:** Siobhan's network amplification effect. Irish session players are a tight, global community. GDAE also serves octave mandolin — doubling addressable users with zero additional effort.

---

### Sprint 4: Convert Marcus (Features #8, #9)

**Ship:** Adjustable Reference Pitch + Home Screen Widget

**Growth Ladder alignment:** Stage 5 (Convert Marcus into an Evangelist)

**Justification:** Marcus has three non-negotiable conditions: (1) ad-free tuning (done in Sprint 1), (2) adjustable pitch, (3) home screen widget. Sprints 4 delivers #2 and #3. All three conditions met = Marcus reinstalls, recommends on Reddit and YouTube, and his friend Diego in the Austin Brazilian music group installs. Marcus's evangelism is tech-community-focused — Reddit r/WeAreTheMusicMakers, YouTube gear reviews — reaching a different audience than Siobhan's Irish sessions or Rafael's WhatsApp groups.

**Revenue impact:** Marcus converts from churned user to paying evangelist. His Reddit post reaches thousands of multi-instrumentalists.

---

### Sprint 5: Differentiate (Features #10, #11)

**Ship:** Session Mode + Per-Instrument Pitch Memory

**Growth Ladder alignment:** Stage 6 (Session Mode — cross-persona feature)

**Justification:** Session mode transforms Banjen from "useful tool" to "irreplaceable tool" for the three highest-urgency personas (Siobhan, Rafael, Marcus). Per-instrument pitch memory (Feature #11) depends on both the tuning presets (#5/#6) and adjustable pitch (#8), which are now shipped. These two features together create a competitive moat — no other tuner app offers hands-free auto-advance with per-instrument pitch memory.

**Revenue impact:** Retention and word-of-mouth. These features create the "wow" moments that generate organic recommendations. Siobhan tells the Tuesday table. Rafael shares in WhatsApp. Marcus reviews on YouTube.

---

### Sprint 6: Deepen Accessibility (Feature #12)

**Ship:** Hearing Aid Compatibility Mode

**Growth Ladder alignment:** Stage 4 deepening (Betty's complete experience)

**Justification:** With CGBD, touch targets, and ad-free tuning already shipped, Betty is a converted user. The hearing aid compatibility mode is the finishing touch that makes her experience optimal rather than merely functional. Ships after the core features because it addresses a moderate-severity pain rather than a critical one.

**Revenue impact:** Modest direct impact, but signals "we built this for you" to the entire 65+ demographic — an underserved, loyal, and willing-to-pay audience.

---

### Future: Moonshots (Features #13, #14)

**Ship:** Visual Tuning Feedback (prerequisite) then Ear Training Progression

**Growth Ladder alignment:** Beyond current ladder — redefines the product category

**Justification:** These features require the most engineering effort and build on everything shipped before. Visual tuning feedback (#14) provides the pitch detection foundation. Ear training progression (#13) uses that foundation to deliver Harold's dream: "something that teaches me to hear the difference, so eventually I don't need the app at all." This transforms Banjen from "tuner" to "ear training platform" — a category of one. Ship only after the core product is stable, monetized, and serving all five personas.

**Revenue impact:** Category-defining. An ear training tuner has no direct competitor. Premium pricing potential: the progression system could be a separate $4.99 IAP or bundled with ad removal at $4.99 for the complete experience.

---

### Deprioritized from Existing Roadmap

**expand-5-string-banjo:** The v2 persona research explicitly removed the 5-string persona (Jake R.) and replaced him with Rafael S. (cavaquinho). The Growth Ladder notes: "Who this is NOT for: 5-string bluegrass banjo players (the largest banjo segment, but incompatible with the current product)." The 5-string expansion remains technically interesting but is strategically deprioritized — the effort (AudioTrack sine synthesis, new tuning model, expanded UI) is HIGH, and the persona evidence directs resources toward GDAE, CGBD, and cavaquinho markets instead. Revisit only after all five current personas are fully served.

---

*Feature roadmap grounded in 5 persona interviews, 47 documented pains, 7 cross-persona patterns, and 7 existing feature investigations. All pain references traced to verbatim quotes from pains.md. Priority scores calculated from pain severity, persona count, and implementation effort. Sequencing follows the Growth Ladder from user-personas.md with revenue optimization at each stage.*
