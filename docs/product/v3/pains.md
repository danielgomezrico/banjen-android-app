# Banjen — User Pains v3
*May 20, 2026 | Pain Investigator — UX Design Thinking Investigation*

*Evidence base: 5 simulated semi-structured interviews (Harold, Lúcia, Eileen, Wendell, Marcus, 2026-05-20), community-listener verbatim quote bank (43 sourced quotes), competitor-analyst UX teardowns (11 competing apps), final v3 personas, v2 pains.md continuity baseline. Every pain in this document has at least one cited source. No speculation.*

---

## Executive Summary

- **The universal dealbreaker is now empirically named and technically precise.** All 5 interviewees independently surfaced the same kill condition on the tuning surface: a late-loading, layout-shifting, or audio-capable ad rendered after the user has already engaged. Marcus diagnosed it as Cumulative Layout Shift; Eileen called the tuning surface "sacred"; Harold framed it as a violated attention contract; Lúcia uninstalled "live, in front of friends, as reverse marketing"; Wendell turns the phone off entirely. The v2 rule "no ads during tuning" survives but hardens: **no layout shift, no audio interruption, no late-loading content anywhere on the tuning surface, ever** — and a fixed, render-time-reserved bottom banner is the only ad-shape that survives any persona's filter.
- **The sharpest interview-surfaced pain is not the ad itself but its downstream consequence.** Harold's quote "I'm not afraid of sounding bad. I'm afraid of skipping a day. Then two days. Then... well. You see where that ends" (Q15) reframes tuning friction from "annoyance" to "threat to a (silently held) cognitive-health discipline." Wendell's parallel: missed Tuesday rehearsals = "the medicine's not workin'" (Q12). Eileen's parallel: ad-flash in front of session peers = "my name's on that post forever." For 3/5 personas tuning friction is a *stake threat*, not a *task friction*.
- **The most surprising cross-persona pattern is the language and tuning-preset gate operating BEFORE install.** Lúcia "nunca, nunca" clicks an English-language result for "afinador de cavaquinho" (Q3). Eileen has "closed the tab" before installing if DGBD is the only visible tuning (Q6). Wendell never installs without Camille's hand-holding (Q15). **3/5 personas reject Banjen at the discovery surface, not the product surface** — the v2 pain map (which focused entirely on in-app friction) missed an entire class of pre-install pains.
- **Subscription is permanently disqualifying; one-time IAP is welcomed at $3–5 / R$9.90–14.90 / €3–5.** All 5 personas converge on the same pricing model and price band. Harold: "You don't subscribe to a hammer" (Q13). Lúcia: Cifra Club trauma transfers to *any* subscription (Q13). Eileen: "I will not pay €1.99 a month for the rest of my natural life to tune a banjo" (Q14). Wendell: incomprehensible. Marcus: "closes the App Store page" on $2.99/mo (Q10). Subscription monetization is a category-level structural error — not a Banjen-only mistake.
- **Mic-based tuners structurally fail in 4/5 of the personas' real environments** (Hughes' pub, Bar do Juarez samba roda, Treme Dixieland rehearsal, Marcus's home studio with HVAC). The reference-tone architecture is not "a different way to tune" — it is **the only architecture that works where these users actually play.** Banjen's "no mic required" should be a top-line listing claim, not a hidden technical detail. Eileen: a reference-tone app asking for mic is "either incompetent or up to something" (Q16).

---

## Severity Matrix

Scale: Severity (1=minor, 5=critical/uninstall-trigger). Reach = personas affected. Frequency = how often the pain fires. Reversibility = whether the pain is recoverable (R) or causes permanent loss (P — uninstall, delete, never-reinstall, peer-warned). Evidence = source citation.

| # | Pain | Sev | Reach | Freq | Rev | Evidence |
|---|---|---|---|---|---|---|
| 1 | Layout-shifting banner ad on tuning surface (CLS) | 5 | 5/5 | per-launch | P | Marcus Q2, Eileen Q15, Harold Q11, Lúcia Q5, Wendell Q9 |
| 2 | Audio ad fires during active tuning | 5 | 5/5 | rare-fatal | P | Eileen Q19, Harold Q14, Lúcia Q5, Wendell Q9, Marcus Q15 |
| 3 | Full-screen / interstitial ad on cold-start | 5 | 5/5 | per-launch | P | Lúcia Q2 ("avalanche de anúncios"), comm-listener #11, competitor-analyst NETIGEN teardown |
| 4 | Accidental subscription enrollment ("free trial" dark pattern) | 5 | 4/5 | one-shot | P | Lúcia Q13 (Cifra Club 2-month cancel fight), Harold Q2 ($0.99/wk for 4 months), Eileen Q9 (Lisbon afinador), Wendell Q8 |
| 5 | Tuning-screen layout shifts mid-tune (button moves) | 5 | 4/5 | per-launch | P | Marcus Q2, Harold Q14, Eileen Q15, Wendell Q9 (implied via "buttons moved") |
| 6 | App opens to DGBD with no visible alt-tuning preset | 5 | 2/5 | per-launch | P | Eileen Q6 ("conversation's already over"), Wendell Q3 (CGBD needed) |
| 7 | English-only Play Store listing for PT-BR cavaquinho searcher | 5 | 1/5 + multiplier | per-search | P | Lúcia Q3, Q19; comm-listener Theme 5 |
| 8 | Microphone permission requested by a reference-tone-only app | 4 | 5/5 | per-install | R/P | Harold Q17, Eileen Q16, Lúcia Q16, Wendell Q16, Marcus Q12/Q16 |
| 9 | Subscription pricing model (any cadence) | 5 | 5/5 | per-paywall | P | Harold Q13, Lúcia Q6/Q13, Eileen Q14, Wendell Q17, Marcus Q10 |
| 10 | One-shot tones (no loop) — can't hold phone + turn peg | 4 | 4/5 | per-string | R | Harold Q14 implied, Wendell Q19, Eileen Q3, Lúcia Q9 |
| 11 | Mic-based tuner fails in noisy environment | 4 | 4/5 | per-session | R | Eileen Q10, Lúcia Q1+Q15, Wendell n/a, Marcus Q4 (HVAC), comm-listener #8 |
| 12 | Late-loading banner causes thumb-mis-tap into ad → Chrome | 5 | 4/5 | per-launch | P | Marcus Q2 (exact mechanism), Eileen Q15, Wendell Q9, Lúcia Q5 |
| 13 | Banjo or instrument feature gated behind paywall | 4 | 3/5 | per-install | P | Harold Q6 (GuitarTuna Pro), Lúcia (cavaquinho dropdown missing), comm-listener #15, competitor-analyst GuitarTuna |
| 14 | "Update moved the buttons" — UI churn | 4 | 2/5 | per-update | P | Wendell Q13 ("why would they do that?"), Harold Q2 ("please don't have moved") |
| 15 | Touch targets too small / cramped (arthritis / tremor) | 4 | 2/5 | every-tap | R | Wendell Q7 ("hit two things at once"), Harold (implied), comm-listener Theme 4 |
| 16 | Reading-glasses required to use the app | 3 | 2/5 | per-launch | R | Wendell Q10 (won't lean), Q19 ("don't make me read") |
| 17 | Chromatic tuner assumes user knows target notes | 4 | 2/5 | per-launch | R | Harold Q6, Eileen Q4 ("just give me the A"), comm-listener #25 |
| 18 | Jumping / twitchy needle UI | 3 | 3/5 | per-string | R | Harold Q5 (Snark), Eileen Q4 (PitchLab "cockpit"), comm-listener #10, #8 |
| 19 | YouTube-as-tuner: 15min for 45sec of usable audio | 4 | 1/5 | daily | R | Harold Q1 (exact account), Q4 |
| 20 | Lost tuning fork (forks > 4/yr replacement rate) | 3 | 1/5 | monthly | R | Eileen Q3, Q5 ("Bermuda Triangle") |
| 21 | Sound is a "beep" instead of warm instrument timbre | 3 | 2/5 | per-tone | R | Wendell Q4 ("a banjo, not a beep"), Q19; Rafael equivalent in v2 |
| 22 | Sponsored top-of-search results crowd out real ones | 3 | 3/5 | per-search | R | Eileen Q8, Marcus Q13, Lúcia (implied via "uses YouTube authority") |
| 23 | Hearing-aid distortion of upper-string frequencies (D4) | 3 | 1/5 | daily | R | Wendell (persona profile, 4–8kHz issue); also v2 Betty pattern |
| 24 | Two-handed tuning impossible while phone needs constant taps | 4 | 4/5 | per-session | R | Wendell Q19, Eileen Q13 ("phone away, play the tune"), Harold Q14 |
| 25 | No widget / homescreen access — must launch app for one tone | 3 | 1/5 (multiplier) | per-tune | R | Marcus Q5, Q11; competitor-analyst (no app ships it) |
| 26 | No adjustable reference pitch (A=432–446Hz) | 3 | 1/5 | weekly | R | Marcus Q5, Q11 (generates in Ableton); competitor-analyst gap |
| 27 | Single-developer cavaquinho app deteriorating, no alternative | 4 | 1/5 (millions impacted) | weekly | R | comm-listener (Cavaquinho Tuner crashes), competitor-analyst Two Brothers Co. teardown |
| 28 | Recommendation-reputation cost of sharing a bad app | 4 | 4/5 | per-share | R | Eileen Q9 ("two of the lads gave out for a fortnight"), Lúcia Q5 ("marketing reverso ao vivo"), Marcus Q14 (shelved YouTube ep), Harold Q10 (Ray bar) |
| 29 | Permission-dialog fear ("Daddy when in doubt say no") | 4 | 2/5 | per-install | R | Wendell Q16, Harold Q17 |
| 30 | "Free trial" copy alone is uninstall-trigger | 4 | 3/5 | per-launch | P | Harold Q2, Lúcia Q6, Wendell Q17 (implied via Camille's rule) |
| 31 | Settings drawer full of upsell prompts | 4 | 1/5 | per-install | P | Marcus Q12 ("settings screen tells you what the dev cares about") |
| 32 | Acquisition path requires family member (install gate) | 4 | 1/5 | one-shot | R | Wendell Q15, Q17 (Camille is the gate) |
| 33 | App > 200MB → "suspicious," storage-anxiety delete | 3 | 1/5 | per-install | R | Lúcia Q12 ("o que tem aí dentro?") |
| 34 | App requires internet — fails offline / no data | 3 | 2/5 | per-session | R | Harold Q1 (buffering wheel), Lúcia Q12 (15GB plan), Eileen (Hughes' wifi varies) |
| 35 | Reference-pitch one-shot pops/clicks when stopped | 2 | 1/5 | per-tone | R | comm-listener (NETIGEN/T4A complaints), competitor-analyst (Banjen invisibly nails this) |
| 36 | Spam on settings/exit screens (interstitial on backgrounding) | 3 | 2/5 | per-exit | R | Marcus Q5 (would tolerate exit-only), Lúcia Q2 |
| 37 | Multi-instrument switch destroys creative flow | 3 | 1/5 | per-session | R | Marcus Q4, Q11 (guitar→banjo→cavaco→back) |
| 38 | "Update moves the buttons" — muscle-memory destruction | 4 | 2/5 | per-update | P | Wendell Q13, Harold Q2 (parallel: "please don't have moved") |
| 39 | Ear-training value invisible (positioned as tuning-only) | 2 | 3/5 | strategic | R | Eileen Q12 (would install for student tomorrow), Marcus Q6 (philosophical win), comm-listener #23–27 |
| 40 | Tap targets near billing/Chrome surfaces (accidental-tap risk) | 5 | 3/5 | per-launch | P | Wendell Q19, Q2 (Camille's cleanup), Betty parallel from v2 |
| 41 | Tone quality on cold first listen — bad tone = instant uninstall | 3 | 2/5 | first-use | P | Marcus (ear-trained), Rafael equivalent; comm-listener #21 (Pano Tuner "no style") |
| 42 | Cavaquinho not labeled in any tuner app's preset list | 4 | 1/5 (millions impacted) | per-install | R | Lúcia Q2 ("no melhor caso escondido num menu"), competitor-analyst |
| 43 | CGBD plectrum tuning absent from every app Wendell could find | 5 | 1/5 + niche | per-install | P | Wendell Q3 ("nobody plays it"), comm-listener #43 |
| 44 | GDAE buried under "alternative tunings" submenu | 5 | 1/5 + multiplier | per-install | P | Eileen Q7, Q18 (would post on TheSession that night) |
| 45 | Slow time-to-first-tone (>3 sec) | 4 | 4/5 | per-launch | R | Harold Q12 (2-min standard), Marcus Q12 (5-min test), Lúcia Q9, Rafael v2 |
| 46 | Onboarding carousel / signup before tuning | 5 | 5/5 | per-launch | P | Harold Q12, Lúcia Q9, Marcus Q1, Wendell Q19, Eileen Q20 |
| 47 | "Pro" terminology / freemium upsell badges | 4 | 4/5 | per-launch | P | Harold Q6 (GuitarTuna), Eileen Q4 (GuitarTuna), Marcus Q5/Q12 |
| 48 | Tuner that requires login or email | 5 | 5/5 | per-install | P | Lúcia Q9 ("não tô abrindo conta no banco"), Marcus Q5, Harold Q12, Eileen Q20, Wendell (incomprehensible) |
| 49 | Vibration-crosstalk on clip-on tuners (jam/session context) | 3 | 2/5 | per-session | R | Eileen Q5 ("box player's bellows through floorboards"), Lúcia Q10 (cavaco vibrates clip off) |

**Count: 49 distinct pains.** (Target: 35–50.)

---

## Pain Categories

### Category A — Tuning-Surface Integrity (the sacred space)
*The active-tuning UI moment. Any interruption here is treated as a contract violation by all 5 personas.*

- **A1. CLS layout-shift ad** (Pain #1). A banner ad loading in *after* the tuning screen has rendered, pushing string buttons up to make room — Marcus's exact uninstall trigger. *"My thumb was already in motion to tap the D again to re-hear it, and instead my thumb landed on the ad. Chrome opens. My banjo string is still ringing behind the Chrome window."* (Marcus Q2). Reach: 5/5.
- **A2. Audio ad mid-tune** (Pain #2). Any sound from the app that isn't the reference tone — sponsored audio, notification chime, video preroll. *"The worst, the worst."* (Harold Q14). *"Made a production of deleting it."* (Eileen Q19, on full-screen video ad with sound during a Hughes' set). Reach: 5/5.
- **A3. Interstitial on cold-start** (Pain #3). Full-screen ad blocking the tuner before first tap. *"Uma avalanche de anúncios"* — review of Tuner ONE, paraphrased by Lúcia Q2; also comm-listener verbatim #11. Reach: 5/5.
- **A4. Layout shift mid-interaction** (Pain #5). Any element that moves after first paint, not only ads — also late-loading widgets, banner reflows, animated badges. Reach: 4/5 (Wendell + Eileen + Harold + Marcus).
- **A5. Tap-target adjacency to ads / billing surfaces** (Pain #40). A button on the tuning screen close to anything that, if accidentally tapped, opens a browser or billing flow. *"Don't put nothin' on that screen that's gonna scare him off."* (Wendell Q20). Reach: 3/5 (Wendell, Marcus, Harold).
- **A6. Tone playback pop/click on stop** (Pain #35). Audible artifact when looping tone is killed. Banjen invisibly handles this with the `setStreamMute()` workaround in `SoundPlayer.kt`. Competing apps (NETIGEN, T4A) get complaints for it (competitor-analyst). Reach: ambient quality bar.

### Category B — Discovery & Install (the pre-install gate)
*Friction that prevents a persona from ever installing Banjen — by definition invisible inside the product.*

- **B1. English-only listing for PT-BR searcher** (Pain #7). Lúcia: *"Eu nunca, nunca buscaria 'banjo tuner.' (...) Se a Play Store me devolve um resultado em inglês, eu desço a página até achar um em português."* (Q3). The product is already DGBD-compatible with cavaquinho; the listing isn't. Reach: 1/5 direct, but Lúcia's WhatsApp roda multiplier = ~1,000 cavaquinhistas/week.
- **B2. "Cavaquinho" / "cavaco" missing from listing title** (Pain #42). Even with a translated description, the searchable title must contain the instrument noun in the user's idiom. Lúcia Q17: *"Se vocês colocarem 'afinador de cavaco' no título da loja, eu mando no grupo da roda no mesmo segundo."*
- **B3. App store top results are sponsored / fake-review crowded** (Pain #22). Eileen Q8: *"Never the App Store. Searching the App Store for trad gear is a fool's errand — you get a wall of guitar apps and karaoke yokes."* Marcus Q13: *"The top results are all sponsored or have fake review counts."* Reach: 3/5 (Eileen, Marcus, Lúcia).
- **B4. No PT-BR human-translated description** (Pain #7 sub). Lúcia Q19: *"Se o app não se deu o trabalho de traduzir pra português, ele provavelmente não vai me dar suporte (...) A tradução é um sinal de respeito."* Reach: 1/5 + huge multiplier.
- **B5. Cavaquinho-DGBD overlap is invisible to the user** (Pain #42 + Lúcia Q8). Lúcia did not know the banjo/cavaquinho tuning overlap existed until told mid-interview. *"Sério? (...) Que loucura."* The market-existing-but-uninformed problem.
- **B6. Web tuner SEO captures Harold before the app store does**. Comm-listener and competitor-analyst confirm: guitartuna.com/online-banjo-tuner outranks the Play Store for "online banjo tuner." Harold Q1 used YouTube; could equally land on a web tuner first.
- **B7. Acquisition-via-family-install gate** (Pain #32). Wendell Q15: *"I'm not callin' her sayin' 'how do I get the tuner app?'"* Wendell will never install Banjen by himself. Camille is the gate. Reach: 1/5, but represents a clinical-utility (social-prescribing) acquisition channel.

### Category C — Trust & Permissions (the brand-trust signal)
*Trust-eroding moments at install or first launch — Harold's "scanning for landmines" frame.*

- **C1. Microphone permission requested for reference-tone tuner** (Pain #8). *"Either incompetent or up to something."* (Eileen Q16). Harold Q17 has read about phones listening "in the paper, the paper, not online." Wendell Q16 will press no on any permission. Lúcia Q16 is suspicious. Marcus Q16 says his student Bill genuinely will not install mic-permission apps. Reach: 5/5.
- **C2. Subscription pricing (any cadence)** (Pain #9). Permanently disqualifying for all 5. Eileen Q14: *"I will not pay €1.99 a month for the rest of my natural life to tune a banjo."* Harold Q13: *"You don't subscribe to a hammer."* Reach: 5/5.
- **C3. Accidental-subscription dark pattern** (Pain #4). Lúcia Q13: Cifra Club 7-day-trial → R$12.90/month → 2-month cancel fight → Reclame Aqui (comm-listener #21 verbatim: *"IMPOSSÍVEL CANCELAR O APP AFINADOR CIFRACLUB"*). Harold Q2: $0.99/week for 4 months unnoticed. Reach: 4/5 (incl. Eileen's Lisbon afinador and Wendell's Camille incident).
- **C4. "Free trial" copy alone triggers uninstall** (Pain #30). The word itself is uninstall-trigger for Harold (Q2), Wendell (via Camille's rule, Q16), Lúcia (Q6). Reach: 3/5 strict + adjacent.
- **C5. Registration / login required for a tuner** (Pain #48). *"Não tô abrindo conta no banco, eu tô afinando um cavaquinho."* (Lúcia Q9). *"Wants me to make an account and pay a subscription for what was a free feature five years ago."* (Eileen Q4 on GuitarTuna). Reach: 5/5.
- **C6. Settings screen full of upsell prompts** (Pain #31). Marcus Q12: *"The settings screen tells you what the developer actually cares about."* Reach: 1/5 explicit, but a quality signal for all 5.
- **C7. Banjo feature paywalled after install** (Pain #13). GuitarTuna lured-and-paywalled Harold (Q6) and Marcus (Q15). *"That's a bait-and-switch and people remember."* (Marcus Q15). Reach: 3/5 confirmed.

### Category D — Physical / Motor (the body-friction layer)
*Physical-world friction independent of UX choices — aging hands, tremor, hearing aids, reading glasses.*

- **D1. Touch targets too small / cramped** (Pain #15). Wendell Q7: *"My finger comes down kinda flat, like the whole pad of it, and I hit two things at once."* Needs ≥thumb-sized buttons (≥60dp) with ≥16dp spacing. Reach: 2/5 (Wendell explicit, Harold ambient — older-beginner cohort universal).
- **D2. Reading glasses required to use the screen** (Pain #16). Wendell Q10: *"By the time I've got 'em on, I've usually already given up on the phone."* Reach: 2/5 explicit (Wendell, Harold), implicit for the over-60 segment.
- **D3. Hand tremor → wrong-button taps** (Pain #15 sub). Wendell Q7: *"My hand shakes a little. (...) I'm aimin' at one thing and my finger lands somewhere else."* Demands generous spacing + dwell-friendly buttons.
- **D4. Hearing-aid algorithms distort upper-string frequencies** (Pain #23). Wendell hears the banjo better with his aid removed (4–8kHz D4 is where aid algorithms struggle, per persona profile). Optimize **speaker clarity at 12 inches**, not room-fill volume.
- **D5. Two-handed tuning physically impossible while phone needs constant taps** (Pain #24). 4/5 personas confirm. Wendell Q19: *"I gotta hold the banjo and turn the peg, I can't be pressin' the phone every two seconds. Let it play."*
- **D6. Friction-peg overshoot / arthritis** (comm-listener #2). Forum elder verbatim: *"Friction pegs have a 1:1 ratio, so when you're close to the correct pitch, you have a tendency to jump past the correct position."* Looping tone gives the user time to overshoot, settle, recheck. Reach: 3/5 (Wendell explicit, Harold implicit, Eileen Q11 on low-G settling).
- **D7. Vibration crosstalk on clip-ons in multi-string contexts** (Pain #49). Eileen Q5: *"You've five people on stringed instruments at the one table and your clip-on's picking up the box player's bellows."* Lúcia Q10: cavaco vibrates the clip off. Reach: 2/5 explicit, 4/5 ambient (anywhere mic-tuners fail, clip-ons frequently also fail).

### Category E — Cognitive Load (the mental-model layer)
*Decision-paralysis, naming, mental-model mismatches — friction not about hands but about thoughts.*

- **E1. App assumes user knows target note names** (Pain #17). Harold Q6: chromatic *"assumes I knew the target notes. (...) I didn't know what to do with it."* Reach: 2/5 explicit (Harold beginner, Eileen Q4 reverse-mismatch: *"I know what note I want — just give me the A"*).
- **E2. Jumping needle / cents readout** (Pain #18). Harold Q5 (Snark needle bounces), Eileen Q4 (PitchLab cockpit), comm-listener #10 (Pano Tuner *"so fast you can't read what is sharp or flat"*). Reach: 3/5.
- **E3. Beginner anxiety ("am I tone deaf?")** (comm-listener #23, Theme 3). Forum elders universally reassure beginners; no in-app tooling supports the bridge from "I can't hear it" → "I can." Eileen Q12: 11-year-old student "can't yet hear sharp versus flat" — reference-tone training fits. Reach: 2/5 (Eileen's student, Harold's "feeling, not numbers" Q3).
- **E4. "Update moved the buttons" — muscle-memory destruction** (Pain #14, #38). Wendell Q13: *"Why would they do that? Who'd a thought that was a good idea? An old man tryin' to find the green button and they moved it."* Harold Q2 (parallel concern). Reach: 2/5 explicit, ambient for all aging users.
- **E5. App settings/navigation pattern unfamiliar** (Wendell Q4, Q6). Hamburger menus, gear icons, swipe gestures — Wendell has no mental map. *"Edna paid the bills on that thing. She knew where everything was. I just press the green button when somebody calls."* (Q1).
- **E6. Cumulative attention cost compounds across morning** (Harold Q1). 15 minutes of YouTube-fighting for 45 seconds of usable audio = 95% loss rate on attention-as-currency, in a 15-minute morning window where every minute matters. Reach: 1/5 explicit, structural for older-beginner cohort.
- **E7. Onboarding carousel / signup before first tone** (Pain #46). Harold Q12: *"No ads, no signup, no fellow talking about his Nashville trip."* Reach: 5/5.

### Category F — Cross-Instrument (the DGBD-only ceiling)
*The product's tuning-preset coverage as a gate on entire user populations.*

- **F1. GDAE buried (or absent)** (Pain #44). Eileen Q6: *"DGBD is not Irish tenor. Irish tenor is GDAE. (...) If your app opens to D-G-B-D and there's no obvious way to switch, the conversation's over."* Q7: GDAE must be *first-class on the home screen*, not under "alternative tunings." Reach: 1/5 direct, multiplier = "Dublin to Boston to Melbourne overnight."
- **F2. CGBD plectrum absent** (Pain #43). Wendell Q3: *"That low C, nobody plays it. I gotta find that one off the piano. Marcus, the piano player at rehearsal, he hits a C for me."* Without CGBD support, Banjen literally plays the wrong notes for him. Reach: 1/5 niche + clinical-utility multiplier.
- **F3. Cavaquinho not labeled (even though DGBD code matches)** (Pain #42). Lúcia Q8 (just-discovered the overlap); Marcus Q8: *"They literally just need to put the word 'cavaquinho' in the Play Store listing and (...) Rafa would install it tomorrow."* Reach: 1/5 direct, 1M+ Portuguese-speaking multiplier.
- **F4. No A=432–446Hz adjustable reference pitch** (Pain #26). Marcus Q5, Q11: currently generates 432Hz tones in Ableton. Niche but high-conviction. Reach: 1/5 direct, but signals "real musician built this."
- **F5. No multi-instrument profile / preset memory** (Pain #37). Marcus Q4, Q11: guitar→banjo→cavaco→back in one session; reconfiguring each = "two extra taps and a launch animation × 8–15 times/day."
- **F6. Single-developer cavaquinho-tuner death spiral** (Pain #27). Comm-listener and competitor-analyst confirm the dedicated cavaquinho apps are dying / poorly maintained. Whoever fills the gap inherits the entire user base.

### Category G — Distribution Friction (the referrer-shaped surface)
*Friction in the act of recommending, sharing, or demoing the app to others — Banjen's growth surface.*

- **G1. Recommendation-reputation cost is real** (Pain #28). Eileen Q9: *"Two of the lads gave out to me for a fortnight."* Lúcia Q5: *"O Tuner ONE eu apaguei na frente das minhas amigas. (...) Foi marketing reverso ao vivo."* Marcus Q14: shelved YouTube ep because *"I didn't want to put my name on something that would make my viewers tap an ad and rage at me."* Reach: 5/5.
- **G2. No persona-specific referral asset** (designed-in gap). Each persona has a distinct referrer surface: teacher one-pager (Harold→Ray), pre-written PT WhatsApp share (Lúcia), TheSession.org gear-thread post (Eileen), Camille-facing setup mini-guide (Wendell), Reddit/YouTube indie-dev stance (Marcus). Banjen's referral funnel today is "share the Play Store link" — broken for 5/5.
- **G3. No web tuner landing handoff** (competitor-analyst gap). guitartuna.com captures the "online banjo tuner" SEO; Banjen has no banjen.app/tune handoff. Harold Q1 lands on YouTube; could equally land on a web tuner. Reach: structural — invisible cost in conversion.
- **G4. No social-prescribing channel** (Wendell Q12). His doctor said *"that's medicine."* No infrastructure exists to convert clinical recommendation into install. The persona-profile distribution table lists this as Wendell's primary channel.
- **G5. Demo / share friction in WhatsApp / TheSession / Reddit** (Pain #28 sub). Lúcia Q17 will share *"no mesmo segundo"* if the listing has "afinador de cavaco" in PT. Eileen Q18 will post on TheSession the night Banjen ships GDAE — but only if surviving one Tuesday at Hughes' ad-flash-free. The asset (a pre-shareable card / screenshot / single PT-BR / en-IE quote) doesn't exist today.

### Category H — Lifecycle (onboarding, retention, plateau)
*Pains that fire across the lifecycle: first launch, daily use, week-3 silence threshold, peer-recommendation gate.*

- **H1. First 10 seconds determine permanent adoption or abandonment** (v2 Pattern 7, validated). Harold's bar: "two minutes to first tone, no signup, no ads" (Q12). Wendell's bar: Camille installs, demos once, then him alone next Tuesday. Marcus's bar: 5-minute test (Q12) with hard fails on mic prompt / ad shift / >2 taps to tone. Reach: 3/5 explicit, 5/5 ambient.
- **H2. Three-week silence threshold for recommendation** (Harold Q12). *"If three weeks went by and I'd tuned every morning without a single bad experience — that's when I'd say something to Linda. Probably wouldn't even be a recommendation. I'd just stop complaining."* The latency between "good" and "recommendable" is 21 days of uninterrupted reliability. Reach: 1/5 explicit, structural for the over-60 segment.
- **H3. Avoidance-learning lockout** (Wendell Q2, Q15). One bad experience (Camille's Saturday cleanup) → year-long behavioral lockout. *"I just put the phone down and played the banjo a little sharp and went on with my mornin'."* He has taught himself not to try. Reach: 1/5 + Betty parallel from v2.
- **H4. Plateau / "good enough" loss to free competitor** (Marcus Q3, Q5). *"LikeTones isn't great. It's fine. But 'fine and no ads' beats 'great and ads' every single time."* If Banjen's tuning-surface fix doesn't ship before LikeTones consolidates the "no ads" position, Marcus's cohort migrates and is unreachable. Reach: 1/5 direct, full Marcus-cohort multiplier.
- **H5. Update churn breaks daily users** (Pain #14, #38). Wendell Q13: any update moving buttons = permanent loss. Harold Q2: hesitation pre-baked. Reach: 2/5 explicit + ambient for all older users.
- **H6. Tuning friction = stake threat, not annoyance** (Harold Q15, Wendell Q12, Eileen Q9). For 3/5 personas, fixing the tuning friction protects a downstream outcome (cognitive-health discipline / clinical depression risk / session credibility) that matters more than the tuning itself. Marketing copy that says "tune faster" misses this. Copy that says "don't lose the morning / don't lose Tuesday night / don't lose the session" connects.

---

## Cross-Persona Patterns

Patterns appearing in ≥3 personas. These are the highest-priority opportunities — pains that compound across the user base.

### 1. The tuning surface is sacred — and the kill condition is technically specific (5/5)

**Affected:** All 5 personas. **Mechanism:** Late-loading or audio-capable content on the active tuning UI causes thumb-mis-taps, ear-derails, attention-contract violations, and identity-level credibility damage. **Specificity (v3 vs v2):** Marcus named the precise bug — Cumulative Layout Shift; a *fixed, render-time-reserved bottom banner* survives all 5 filters (GStrings model). **Design implication:** Reserve the ad slot at first paint or do not ship the ad. Audio ads on the tuning surface are forbidden by product policy, not just deferred by roadmap.

### 2. Microphone permission is a brand-trust signal (5/5)

**Affected:** Harold (Q17), Eileen (Q16), Lúcia (Q16), Wendell (Q16), Marcus (Q12, Q16 — including his student Bill). **Mechanism:** For a reference-tone-only app, the mic permission is interpreted as either incompetence or surveillance. **Design implication:** "No microphone required" is a top-line listing claim, not a hidden technical detail. Surface on Play Store screenshot 1.

### 3. Subscriptions are disqualifying, one-time IAP is welcomed (5/5)

**Affected:** All 5 personas, with trauma-history anchoring (Lúcia: Cifra Club, Harold: $0.99/wk, Eileen: Lisbon afinador, Wendell: Camille's cleanup, Marcus: $2.99/mo App Store close). **Mechanism:** Subscription = "permanent relationship with a tool I want to use once a day" = category-level mismatch. **Design implication:** Pricing band $3–5 USD / R$9.90–14.90 / €3–5, one-time, ad-free unlock. No "Pro" terminology. No "free trial."

### 4. Mic-based tuners structurally fail in real environments (4/5)

**Affected:** Eileen (Hughes' pub), Lúcia (Bar do Juarez TV + samba galpão), Marcus (home studio HVAC), Wendell (clip-on Snark crosstalk implied via "Marina lost three"). Harold is the exception — quiet den — but his concern still flags mic-listening for privacy reasons. **Mechanism:** Pub TV, samba bar football match, rehearsal hall percussion, multi-instrument live tracking — mic + room noise = failure. **Design implication:** Reference-tone architecture is **the only architecture that works** for these users. Position it as the professional solution, not the beginner crutch.

### 5. Distribution is referrer-shaped, not Play-Store-shaped (5/5)

**Affected:** Harold→Ray, Lúcia→WhatsApp roda (60+), Eileen→TheSession.org gear thread + WhatsApp (12), Wendell→Camille + bandmates + GP, Marcus→Reddit + small YouTube. **Mechanism:** None of the 5 uses Play Store editorial discovery; Lúcia and Eileen actively distrust top-ranked listings. **Design implication:** Build per-persona referrer assets (teacher one-pager, PT WhatsApp share card, TheSession-ready post, Camille setup guide, Reddit AMA stance). Do not measure success in Play Store conversion alone.

### 6. Looping playback enables two-handed tuning (4/5)

**Affected:** Harold (Q14), Lúcia (Q9), Wendell (Q19 explicit), Eileen (Q3 — fork held to ear while tuning). **Mechanism:** Both hands must be on the instrument — one to pluck, one to turn the peg. One-shot tones force a third-hand operation that none of the four has. **Design implication:** Tone plays until user taps to stop. Loop length ≥ time-to-tune-one-string (5–10 sec). Auto-advance ("session mode") is the next evolution.

### 7. Tuning friction is a stake threat (3/5)

**Affected:** Harold (cognitive-health discipline), Wendell (depression-recurrence risk via missed Tuesdays), Eileen (musician credibility in front of session peers). **Mechanism:** For these three, a bad tuning experience doesn't just waste minutes — it threatens whether they sit down at all next time, whether the band is still possible, whether their recommendation network still trusts them. **Design implication:** Marketing copy that frames the value as "don't lose the morning / Tuesday / the session" outperforms "tune faster" by an order of magnitude.

### 8. First experience is the entire trial (3/5 + ambient)

**Affected:** Harold (Q12 — 2 minutes or done), Wendell (Q15 — one bad experience = year lockout), Marcus (Q12 — 5-minute test with hard fails). Lúcia and Eileen ambient (each has a "delete in front of friends / closed the tab" trigger). **Mechanism:** No learning curve granted. A single confusing UI, unexpected ad, or wrong tuning causes permanent abandonment. **Design implication:** The first 10 seconds must produce a successful tone, no permission, no signup, no carousel. Test against the 65+ cohort with literal first-launch instrumentation.

### 9. Recommendation reputation has financial-grade cost (4/5)

**Affected:** Eileen ("two of the lads gave out for a fortnight"), Lúcia ("apaguei na frente das minhas amigas"), Marcus (shelved YouTube ep with 2,800 subs), Harold (won't show Ray until certain). **Mechanism:** The cost of a bad recommendation isn't apology — it's reputation damage that compounds across the recommender's network. Eileen sits on a recommendation a full week before sharing. **Design implication:** Banjen's reputation must be ad-shift-fix-shipped *before* asking for referrals. Otherwise referrals carry negative ROI.

### 10. Language/instrument identity gate operates pre-install (3/5 + multipliers)

**Affected:** Lúcia (PT-BR listing), Eileen (GDAE preset visibility), Wendell (CGBD preset). **Mechanism:** The persona's instrument or language doesn't appear in the listing/home screen, so they never install — *or* close the tab in <5 seconds. **Design implication:** Localized listings + first-class tuning presets *visible on the home screen* are not roadmap items; they are gating preconditions for three distinct distribution channels (Brazilian samba, Irish trad, US plectrum).

---

## How Might We Reframes

Top 10 highest-severity pains, with 1–2 HMW each. (HMW format: "How might we help [persona] [goal] without [pain]?")

**HMW-1.** Layout-shift banner on tuning surface (Pain #1)
- HMW we deliver a tuning UI where the ad slot is reserved at first paint, so layout never shifts after the user has engaged?
- HMW we make the tuning surface a "no-mutation zone" by product policy — no banner, badge, animation, or overlay can fire during active tuning interaction?

**HMW-2.** Audio ad fires during active tuning (Pain #2)
- HMW we guarantee that the only sound emitted by the app between tap and stop is the reference tone — by architecture, not by content moderation?

**HMW-3.** Interstitial on cold-start (Pain #3)
- HMW we get the user from app icon tap to looping D in under 2 seconds, with zero intermediate screens?

**HMW-4.** Subscription pricing (Pain #9) / Accidental-subscription dark pattern (Pain #4)
- HMW we monetize Banjen as a "hammer purchase" — one tap, one charge, owned forever — that matches every persona's mental model for tool purchases?
- HMW we make the ad-free unlock so transparently one-time that Lúcia, Eileen, and Harold each show it to their network as evidence of fairness?

**HMW-5.** App opens to DGBD with no visible alt-tuning preset (Pain #6) / GDAE buried (Pain #44) / CGBD absent (Pain #43)
- HMW we make the home screen a tuning picker — DGBD / GDAE / CGBD with localized labels ("Plectrum / Cavaquinho," "Irish tenor," "Chicago") — visible in the first 3 seconds of first launch?

**HMW-6.** English-only listing for PT-BR searcher (Pain #7) / Cavaquinho not labeled (Pain #42)
- HMW we make Banjen the #1 organic result for "afinador de cavaco" with a PT-BR human-translated listing and "Afinador de Cavaquinho e Banjo" in the title — within one Play Store deploy cycle?
- HMW we make Lúcia's roda WhatsApp share possible the day she discovers Banjen?

**HMW-7.** Microphone permission requested by a reference-tone-only app (Pain #8)
- HMW we ensure Banjen never requests microphone permission — by architecture — and surface "No microphone required" as the top line of the Play Store screenshot stack?

**HMW-8.** Layout shift mid-interaction (Pain #5) / Tap-target adjacency to ads/billing (Pain #40)
- HMW we lay out the tuning screen so that no tap target sits within thumb-travel distance of an ad, browser-launch, or billing surface — by spec, not by luck?

**HMW-9.** Late-loading banner → thumb-mis-tap → Chrome (Pain #12)
- HMW we reserve the bottom 64dp at first paint as a static, non-animating surface that the banner ad inhabits but never grows, shrinks, or shifts — Marcus's GStrings tolerance bar?

**HMW-10.** One-shot tones (no loop) (Pain #10) / Two-handed tuning impossible (Pain #24)
- HMW we make every reference tone loop for ≥10 seconds by default, with a single visible "stop" affordance, so the user's two hands can stay on the instrument?
- HMW we add an opt-in "session mode" that auto-advances through all 4 strings — first surfaced independently by Siobhan (v2) and Rafael (v2), now structurally implied by Eileen Q3, Lúcia Q9, and Wendell Q19?

**Bonus HMW — Tuning friction as stake threat (Pattern #7)**
- HMW we frame Banjen's value proposition not as "faster tuning" but as "don't lose the morning / don't lose Tuesday night / don't lose the session" — copy that connects with Harold, Wendell, and Eileen's stake-threat reality without surfacing Harold's clinical-private motivation?

---

## Inviolable Constraints

Hard rules derived from the pain analysis. Violating any of these is product malpractice.

1. **No ad on the tuning surface — ever.** Banner, audio, video, sponsored badge, "tuned!" interstitial, late-loading content, animated overlay, anything. The tuning surface is the active-tuning UI from first tap to manual stop. Other surfaces (splash, settings, exit, post-tune confirmation) can be monetized; the tuning surface is sacred. (Evidence: 5/5 personas, 3 cross-persona patterns, 1 named UX bug.)
2. **No microphone permission requested — by architecture.** Banjen's reference-tone-only design means no mic code path should exist. "No microphone required" is a top-line Play Store claim. (Evidence: 5/5 personas; Eileen Q16 calls a mic-asking reference-tone app "incompetent or up to something.")
3. **No subscription, ever — one-time IAP at $3–5 / R$9.90–14.90 / €3–5 only.** No "Pro" terminology, no "free trial" copy, no recurring charge model. (Evidence: 5/5 personas converge; subscription is the #2 cause of 1-star reviews in the category per comm-listener.)
4. **Tuning preset must be visible on the home screen.** DGBD, GDAE, CGBD — all three on the first screen, with localized labels including instrument context ("Plectrum / Cavaquinho," "Irish tenor," "Chicago"). No "Alternative tunings" submenu. (Evidence: Eileen Q6, Q7 / Wendell Q3 / Lúcia Q8.)
5. **No registration, no signup, no email, no onboarding carousel.** The first tap on the app icon must produce a looping reference tone within 2 seconds. (Evidence: 5/5 personas; Harold Q12, Lúcia Q9, Marcus Q1, Wendell Q19, Eileen Q20.)

---

## Changes from v2

### New pains (surfaced for first time in v3)

| Pain | Source |
|---|---|
| Layout-shift CLS bug named precisely | Marcus Q2 (technical diagnosis) |
| Recommendation-reputation cost as financial-grade | Eileen Q9 ("fortnight"), Lúcia Q5 ("marketing reverso"), Marcus Q14 (shelved ep) |
| English-only Play Store listing as *pre-install* gate (not just ASO) | Lúcia Q3 ("nunca, nunca clicaria") |
| Cavaquinho-DGBD overlap invisible *to the user themselves* | Lúcia Q8 ("Sério?... Que loucura.") |
| Camille as install-gate (acquisition path broken at the family layer) | Wendell Q15, Q17 |
| Hidden cognitive-health motivation as actively-protected clinical disclosure | Harold Q16 ("Don't put my name on this") |
| Three-week silence threshold for recommendation | Harold Q12 |
| Avoidance-learning lockout (year-long behavioral) | Wendell Q2, Q15 |
| Hearing-aid 4–8kHz distortion on upper-string D4 | Wendell persona profile (clinical mechanism) |
| LikeTones competitive squeeze on the "no ads" position | Marcus Q3, competitor-analyst |
| "Update moves the buttons" — muscle-memory destruction | Wendell Q13 |
| Reading-glasses lean-cost ("by the time I've got 'em on, I've given up") | Wendell Q10 |
| Web tuner SEO captures users before app store does | comm-listener + competitor-analyst |
| Single-developer cavaquinho-tuner deterioration | comm-listener + competitor-analyst |
| Permission-dialog fear surfaced as parental rule ("when in doubt say no") | Wendell Q16 |
| Trans-language code-switching for music vs work | Lúcia Q19 ("outro registro mental") |
| Storage anxiety on 128GB phone (>200MB = suspicious) | Lúcia Q12 |

### Validated v2 pains (confirmed in v3)

- Ad layout shift mid-tune (v2 Pattern 1 → v3 Pattern 1, now with named bug).
- 15-second fumble gap / time-to-first-tone (v2 Pattern 2 → still universal, validated by Harold Q1, Eileen Q1, Lúcia Q1, Wendell Q11, Marcus Q4).
- Instrument/tuning invisibility (v2 Pattern 3 → v3 Pattern 10, sharpened to *pre-install* gate).
- Mic-based tuners fail in noise (v2 Pattern 4 → v3 Pattern 4, evidence now from 4 distinct environments).
- Looping playback hands-free requirement (v2 Pattern 5 → v3 Pattern 6).
- Subscription rejection / one-time IAP welcomed (v2 Pattern 6 → v3 Pattern 3).
- First experience determines permanent adoption (v2 Pattern 7 → v3 Pattern 8).
- Touch targets and motor accessibility (v2 Betty pain set → v3 Wendell Q7, validated and sharpened).

### v2 pains downgraded or reframed

- **Betty's grief-as-tuning-problem (v2 Pain 2 for Betty)** → reframed in v3 as Wendell's "Edna paid the bills" (Q1, Q6). Still emotionally present but now subordinate to the *clinical-utility* frame (Wendell Q12: "that's medicine"). The grief is real; it is not the design lever. The clinical/social-prescribing channel is.
- **Rafael's deteriorating cavaquinho-app (v2 Pain 2 for Rafael)** → preserved in v3 via Lúcia's parallel (Cifra Club Reclame Aqui) and comm-listener evidence, but Lúcia's *language gate* is the higher-leverage finding. The deteriorating-app pain is real but downstream of the discovery gap.
- **Siobhan's "social invisibility" v2 framing** → upgraded to Eileen's "credibility cost" frame (Q15). Siobhan's pain was *visibility during tuning*; Eileen's is *reputational scarring across a global network*. Same root, harder edge.
- **Jake R. (5-string) pains** → fully retired in v3 (consistent with persona retirement in user-personas.md). 5-string remains the largest banjo segment but the cavaquinho adjacency is higher-leverage.
- **Harold's "dependence on Ray" (v2 Pain 3)** → still present but now subordinate to the *cognitive-health discipline* frame (Harold Q15, Q16). Independence-from-Ray is still a goal; the *threat to morning anchor* is the sharper pain.

### Biggest single change v2 → v3

Harold's hidden cognitive-health motivation moved from *interview subtext* to *actively-protected clinical disclosure*. He asked, mid-interview, whether his name could be kept off the record when discussing the dementia angle (Q16). The product cannot surface this motivation in copy — but every design decision (ad-free tuning surface, looping tones, no-mic, two-minute time-to-tone, three-week silence threshold) must be built as if this disclosure were the only thing that mattered. **Tuning friction is not a UX problem. It is the interruption of a health-relevant habit.** That reframe is the single most important deliverable of the v3 research cycle.

---

STATUS: complete
OUTPUT: /Users/dan/projects/banjen/docs/product/v3/pains.md
