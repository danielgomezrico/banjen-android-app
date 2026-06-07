# Banjen — Final User Personas v3
*May 20, 2026 | Persona Refiner — UX Design Thinking Investigation*

*Sources: 5 simulated semi-structured interviews (Harold, Lúcia, Eileen, Wendell, Marcus), Banjen v3 research synthesis (market-analyst, community-listener, competitor-analyst, social-analyst, academic-researcher), and v2 continuity baseline.*

---

## Executive Summary

- **The dealbreaker is now empirically universal, and it is more specific than v2 said.** All five interviewees independently surfaced the same failure mode: an ad — banner, interstitial, or audio — appearing on the tuning surface *after* the user is already engaged. Marcus diagnosed it as Cumulative Layout Shift ("any web dev with a year of experience knows you reserve the ad slot at render time"); Eileen called it sacred; Harold framed it as a violation of the attention contract; Lúcia treated it as cause for live, in-public uninstallation; Wendell responds by *turning off his phone entirely*. The v2 rule "no ads during tuning" was correct but soft. The v3 rule is **no layout shift, no audio interruption, no late-loading content anywhere on the tuning surface, ever.**
- **Harold's hidden motivation is more clinical than v2 implied.** v2 framed his dementia concern as emotional subtext. The interview reveals he asked, mid-conversation, whether the recording could be turned off when discussing it — and explicitly asked his name not be used. He treats this as health information, not hobby talk. Tuning friction is not annoyance; it threatens "the work." Product copy and onboarding must respect this without exposing it.
- **Lúcia did not know cavaquinho and banjo share DGBD.** This is the single most important growth finding in v3. The market is already built; only the listing is missing. Her exact response when told: *"Sério? Banjo de quatro cordas é Ré-Sol-Si-Ré também? Que loucura."* The blocking growth investment is one Play Store metadata change, not engineering.
- **Eileen's gating condition has hardened.** v2 had Siobhan as a softer "would prefer GDAE." Eileen's stance is binary: *"If your app opens to D-G-B-D and there's no obvious way to switch, the conversation's over before it started."* A GDAE preset is no longer a roadmap nice-to-have; it is a precondition for the entire Irish trad acquisition channel and for her recommendation on TheSession.org — a channel that "reaches Dublin to Boston to Melbourne overnight."
- **Wendell's risk is downstream, not upstream.** v2 framed Betty as ad-fearful. Wendell shows that the *acquisition path itself* is broken: he will never install Banjen by himself. Camille (his daughter) is the install gate. His doctor said "music is medicine." The acquisition channel is therefore family + clinical (social prescribing), not Play Store. He represents a clinical-utility persona the v2 framing under-captured.

---

## Primary Persona Declaration

**Harold M., 67.** The gap between what Harold needs and what Banjen ships today is the smallest gap on the persona list — four big buttons, looping tones, no ads during play. His hidden motivation (cognitive-health discipline) raises the stake of every friction from "annoyance" to "interruption of a health-relevant habit," giving Banjen a defensible value proposition no competitor articulates. He is also the most representative of the Banjo Hangout / older-beginner cohort that defines the English-language acquisition channel, and his teacher Ray is the canonical referral source the entire over-60 segment converts through.

---

## Persona 1: Harold M., 67

*"The Retired Beginner" — retired accountant, Knoxville TN, Deering Goodtime DGBD banjo. Tech 2/5. Pension + Social Security ($55k/yr). Full decision authority; compares every app to a set of strings.*

### Goals

- **Primary:** Play well enough to join the Thursday bluegrass jam circle at the community center; specifically, hold his part on "Wagon Wheel" without being a drag on Ray.
- **Secondary:** Maintain a 7:00 AM daily practice anchor that gives the rest of his retired week a shape.
- **Hidden (do not surface in copy):** Cognitive-decline insurance. After watching Linda's mother decline from dementia and reading the AARP / USF coverage of instrument learning, he treats the morning banjo as preventive medicine. He has not told Linda. *"I'm not just tuning a banjo. I'm — I don't want to be dramatic about it — I'm doing the work."* He asked, mid-interview, whether his name could be kept off this. Treat as health-private.

### Sharpest pain (interview-surfaced)

Not the YouTube ad-and-monologue gauntlet (already known in v2), but the **post-friction loss of the 7:00 AM anchor itself.** A bad tuning experience doesn't just waste fifteen minutes — it threatens whether he sits down at all the next day. *"I'm not afraid of sounding bad. I'm afraid of skipping a day. Then two days. Then... well. You see where that ends."* Banjen is not competing with YouTube. It is competing with the gravitational pull of *not bothering.*

### Behaviors

- 7:00 AM sharp, every morning, wooden chair in the den, music stand, side table for phone + coffee. Dishwasher through the wall to the right. Beagle (Roscoe) barks at the mail truck at 7:15.
- Workaround: YouTube "D G B D banjo reference tone" — typically 15 minutes of phone-fighting for 45 seconds of usable audio.
- Backup workaround: arrives at $40 Ray lessons and has Ray tune it. $6–7 of the lesson goes to tuning = $25–30/month tuning tax.
- Already burned by GuitarTuna (banjo behind Pro paywall), Snark clip-on ("needle jumps around"), unspecified chromatic ("assumes I know the target notes"), accidental 99¢/week subscription he didn't notice for four months.
- Will not show an app to Ray until he is *certain* it works — credibility cost of recommending a bad tool to his teacher is too high.
- Refuses microphone permission by default. Read about phone surveillance "in the paper, the paper, not online."
- Has never recommended any tuner app to anyone in his circle. The bar for recommendation: three weeks without a single bad experience.

### Verbatim quotes (Harold interview, 2026-05-20)

> "I'm not afraid of sounding bad. I'm afraid of skipping a day. Then two days. Then... well. You see where that ends." *(Q15)*

> "I don't need the app to teach me. I need it to play me the note and then get out of my way. That's it. That's the whole thing." *(Pull quote)*

> "Three dollars to make the tuner stop fighting me — that's a steal. Now — if you said three dollars a *month*, I'd say no… You don't subscribe to a hammer." *(Q13)*

### Conditional triggers

- **Would convert if:** App opens directly to four big DGBD buttons, looping tones, no signup, no mic prompt, no full-screen onboarding. He can tap a string, hear it, tune in two minutes, and exit. He repeats this experience for three weeks without a single ad-interruption — at which point he silently stops complaining (his actual measure of approval) and eventually shows it to Ray.
- **Would lose if:** Any sound that is not the reference tone fires during tuning (an audio ad, a notification, a chime); the screen layout shifts while he is reaching for a button; the app asks for a microphone permission without explaining why; he sees the word "subscription" or "free trial" anywhere; a banjo-specific feature is gated behind a paywall (GuitarTuna's mistake — he will not be fooled twice).

---

## Persona 2: Lúcia G., 31

*"The Cavaquinho Diaspora Player" — software-support analyst, Pinheiros, São Paulo. Saturday samba roda at Bar do Juarez, Vila Madalena. Samsung Galaxy A54, R$6,500/mo. Tech 3/5. Decision authority: full + the strongest distribution multiplier in the persona set (60-member WhatsApp roda group, each with downstream groups).*

### Goals

- **Primary:** Keep her cavaquinho in DGBD before the Saturday roda starts at 19h — and tune her cousin Diego's too, because he always arrives late.
- **Secondary:** Find *one* tuner she trusts enough to share to the WhatsApp roda group without a warning caveat ("usa esse mas cuidado").
- **Hidden:** Cultural validation. Cavaquinho is "indispensable" to samba/choro/pagode but invisible in English-language music tech. The dignity of an app that simply names her instrument in the listing — *"Afinador de Cavaquinho e Banjo"* — is itself a value proposition. She mentioned the language gate is a "sinal de respeito" (a sign of respect).

### Sharpest pain (interview-surfaced)

Not the ads themselves (she expected those) but the **language gate before she ever installs.** She searches in Portuguese — "afinador de cavaquinho" or "afinador de cavaco" — and *will not click an English-language listing in the results.* *"Eu nunca, nunca buscaria 'banjo tuner.'"* The v2 frame ("ASO matters") understated this. The frame is not "translate the listing." The frame is **"if you're in English, you don't exist to her."** She doesn't reject Banjen; she never sees it.

### Behaviors

- Searches in Portuguese for everything music-related. English fine for work (software documentation). Music is "outro registro mental" — a different mental register.
- Tunes mostly in noisy environments (bar TV, samba practice galpão with percussionists) where mic tuners are useless. Has used relative tuning off Marquinhos' 7-string violão for years.
- Burned by Cifra Club Afinador — accidental 7-day-trial → R$12.90/month → two-month cancellation fight → Reclame Aqui complaint. She has told the story to roda friends; brand is dead to ~30 cavaquinhistas through her alone.
- Deletes ad-bombing apps *in front of friends*. *"O Tuner ONE eu apaguei na frente das minhas amigas… Foi marketing reverso ao vivo."*
- Distribution: WhatsApp roda group (60+) → each member's downstream group. Estimated reach of one share: ~1,000 cavaquinhistas in São Paulo within one week.
- Trusts Brazilian YouTubers (Professor Damiro, Cavaquinho na Veia) and Instagram Reels (Professora Cris Delanno) far more than Play Store stars. Does not use TikTok for music.
- Storage-anxious: 128GB Galaxy phone, WhatsApp eating most of it. Apps over 200MB look suspicious. Must work offline.

### Verbatim quotes (Lúcia interview, 2026-05-20)

> *"Eu busco 'afinador de cavaquinho' — sempre em português. Se o app está só em inglês, eu nem clico no resultado."* *(Pull quote)*

> *"Sério? Banjo de quatro cordas é Ré-Sol-Si-Ré também? Que loucura."* *(Q8, on discovering the banjo/cavaquinho overlap mid-interview.)*

> *"Se vocês colocarem 'afinador de cavaco' no título da loja, eu mando no grupo da roda no mesmo segundo. Sessenta pessoas, e cada uma tem o seu grupo."* *(Pull quote / Q17)*

### Conditional triggers

- **Would convert if:** The Play Store listing surfaces for "afinador de cavaquinho" / "afinador de cavaco" search, has a description written in actual (not machine-translated) Portuguese, contains the word "cavaquinho" in the title; the app works without microphone permission; one-time IAP at R$9.90–R$14.90 unlocks ad-free.
- **Would lose if:** English-only listing (lost at search results page, pre-install); any subscription model (Cifra Club trauma transfer makes this disqualifying, not just preferable); interstitial or audio ad on cold-start; a microphone permission request she can't justify; she shares it, friends in the WhatsApp group hit ads, she takes a reputational hit and warns the group off it.

---

## Persona 3: Eileen B., 58

*"The Irish Tenor Session Player" — Galway primary teacher; Tuesday session at Hughes' pub; tenor banjo + octave mandolin, both GDAE. iPhone. Tech 4/5 — fluent with Tunefox, Pro Metronome, OAIM, TheSession.org. EUR 47k/yr. Decision authority: full, amplified — one post on TheSession reaches "Dublin to Boston to Melbourne overnight."*

### Goals

- **Primary:** Stay in tune through fast-paced sessions where the gap between tune sets is ~25 seconds and stopping the set to re-tune is socially unacceptable.
- **Secondary:** Find one tool that handles tenor banjo AND octave mandolin (both GDAE) and replaces the tuning forks she keeps physically losing through her case lining.
- **Hidden:** Identity as the trusted gear-recommender in her session network. Influence is her social currency, and she protects it ruthlessly — *"my name's on the post forever."*

### Sharpest pain (interview-surfaced)

Not the noisy-pub problem (already known) but the **"15-second fumble gap"** during which she is *visibly* losing competence in front of peers. *"If you're fumbling with an app while everyone is starting the next set, you don't play that set. And if that happens twice, people notice."* The pain is not about tuning accuracy — it is about **being seen struggling with tech in front of trad peers.** This is identity-level, not task-level.

### Behaviors

- A tuning fork is *acoustically perfect* but losable. She is on her 4th A fork this year (case lining slip, school hoover, etc.). Banjen's competitive moat against the fork is **losability**, not sound quality.
- Tunes A from fork → derives D-G-E by fifths, double-stop checks. The whole sequence is ~40 seconds in a quiet room, up to 3 minutes in Hughes' with the football on.
- Cleartune at home (fine); useless in Hughes' the moment it fills up. Tried PitchLab ("cockpit of a 747"), Snark ("looked daft, vibration crosstalk"), generic chromatics ("treating me like a learner — I know what note I want").
- Once shared an untested afinador app from a Lisbon trip → turned out to be a subscription racket → "two of the lads gave out to me for a fortnight." Now sits on a recommendation for a full week before sharing.
- Once witnessed an app play a full-screen video ad with sound on at the start of a session → publicly deleted it on the spot, "made a production of deleting it," tells the story for six months. Apps die by anecdote in her network.
- Teaches three private tenor students; one (11yo) cannot yet hear sharp vs. flat. A reference-tone trainer is the right pedagogy for her, and Eileen would install it on the mother's phone tomorrow — *if* it has GDAE.

### Verbatim quotes (Eileen interview, 2026-05-20)

> "GDAE or it's not a tenor banjo. If your app opens to D-G-B-D the conversation's already over — I've closed the tab." *(Pull quote)*

> "The tuning fork is acoustically perfect. The problem is it's the size of a teaspoon and the inside of my case is the Bermuda Triangle. Beat the fork on losability, not on sound, and you've something." *(Pull quote)*

> "I'm not posting on TheSession about an app that ad-flashed me in front of the Whelan's lads. My name's on that post forever. Forever, like." *(Pull quote)*

### Conditional triggers

- **Would convert if:** GDAE appears as a first-class preset on the home screen (not buried under "alternative tunings"); the home view is a tuning picker + four big buttons that play actual notes; no mic permission requested; the app survives one Tuesday at Hughes' without an ad-flash. Then: TheSession gear-thread post → session WhatsApp share → 200+ Irish tenor players in a month.
- **Would lose if:** Opens to DGBD with GDAE buried in settings — closed, never reopened. Mic permission asked of a reference-tone app ("incompetent or up to something"). Any ad-flash during a session set — publicly deleted, story told for six months across at least three sessions.

---

## Persona 4: Wendell P., 71

*"The CGBD Plectrum Veteran" — retired Dixieland four-string player, New Orleans. Plectrum CGBD tuning. Old Samsung Galaxy. Tech 1/5. Wife Edna passed eighteen months ago; daughter Camille is his de facto phone gatekeeper. Pension $42k/yr. Decision authority: full on small purchases, BUT will not transact alone.*

### Goals

- **Primary:** Keep his plectrum banjo in CGBD for Tuesday rehearsals with the Dixieland sextet over by Treme — his primary social tie and, per his doctor, "medicine."
- **Secondary:** Stop arriving 20 minutes early to "early-bird tuning club" where Marcus the pianist plays his C-G-B-D for him. Reclaim independence as a musician.
- **Hidden:** The Tuesday rehearsal is his clinical outcome. *"My doctor said 'that's medicine.' Those were her words. So when I can't tune the banjo, it's not just I can't tune the banjo. It's the medicine's not workin'."* Tuning friction is not a UX problem — it is a depression-recurrence risk.

### Sharpest pain (interview-surfaced)

Not "ads scare him" (v2 frame) but **avoidance learning.** One bad experience (an inadvertently downloaded app, Camille's Saturday "clean it up" visit) has produced a year-long behavioral lockout. *"I just put the phone down and played the banjo a little sharp and went on with my mornin'."* He has *taught himself not to try.* The product problem is no longer "make the app good" — it is "give Camille something simple enough she will install and demo it for him."

### Behaviors

- 10:00 AM practice window — when arthritis meds are working. 15–20 minutes before hands say no. Back-bedroom chair, side table for phone and coffee.
- Strums across, hears sourness in the 2nd string, picks up the phone, sits with it in his lap "I don't know how long," puts it back down without tapping. Plays a little out of tune.
- Hand tremor + fat-pad finger contact — taps two buttons at once or the one next to the target. Needs ≥thumb-sized buttons with explicit spacing.
- Cannot read small print without reading glasses on the side table. Won't lean for them — by the time he has them on, he has given up on the phone.
- Defaults to "no" on every permission dialog ("Camille said when in doubt say no"). A reference-tone app that *never asks* for the microphone is a literal precondition.
- Has never paid for an app. Won't transact alone. Three dollars is fine ("less than a beer") — the *transaction* is the blocker, not the price.
- Distribution channel for him is Camille + bandmates + doctor. Not online. Not Play Store. Specifically: the emerging social-prescribing channel (GPs / occupational therapists recommending music apps to seniors) is the right hand-off.
- Hears the banjo *better with his hearing aid removed* — aid algorithms struggle with 4–8kHz frequencies present in the upper D4 string. Optimize speaker clarity at 12 inches, not room-fill volume.

### Verbatim quotes (Wendell interview, 2026-05-20)

> "Edna paid the bills on that thing. She knew where everything was. I just press the green button when somebody calls." *(Pull quote)*

> "Don't make me read. That's all I'm askin'. Don't make me read little words on a little screen with my hands tremblin'. Make a sound. I'll find it." *(Pull quote)*

> "Make it so an old man with shakin' hands and bad eyes and a dead wife can press one big button in his back bedroom on a Tuesday mornin' and hear his banjo's note come out clear, and don't put nothin' on that screen that's gonna scare him off." *(Q20)*

### Conditional triggers

- **Would convert if:** Camille installs it for him on a Sunday, sets it up, and shows him one time. Four big thumb-sized buttons, ≥16dp spacing, warm banjo timbre (not a beep), looping tones (he cannot hold phone + turn peg simultaneously), CGBD preset, zero permissions, zero tappable surfaces that lead to billing screens. Marcus the pianist no longer required.
- **Would lose if:** DGBD only, no CGBD — literally the wrong notes for him. Any ad anywhere near a tappable control = accidental tap → reinforced avoidance → permanent shutdown. Any permission dialog. Any "free trial" copy. Any modal that doesn't have an obvious close. Update that moves the buttons ("they updated it, and one day the buttons are in one place and the next week they moved. Why would they do that?").

---

## Persona 5: Marcus T., 28 (Skeptic / Acquired-and-Lost)

*"The Acquired-and-Lost" — Austin TX multi-instrumentalist, part-time barista + gig musician + ~2,800-subscriber YouTube channel. Pixel 8, Ableton Live, Boss TU-3, Focusrite. Tech 5/5. $38k/yr. Plays guitar/banjo/cavaquinho. Installed Banjen, hit an ad layout shift mid-tune, rage-quit, now on GStrings + LikeTones. Embodies every download that doesn't convert.*

### Goals

- **Primary:** Tune any instrument in under 30 seconds, with zero menu dives, because tuning is overhead that kills creative momentum.
- **Secondary:** Switch instruments mid-session (guitar / banjo / cavaquinho / mandolin) without reconfiguring his tuner each time.
- **Hidden:** Leverage his guitar ear across instruments rather than learning instrument-specific tuning skills. Plays cavaquinho with an Austin Brazilian music group (~20 people, ~18 of whom would install if Banjen surfaced cavaquinho support). His personal interest in the DGBD/cavaquinho overlap *independently* validates Lúcia's market thesis.

### Sharpest pain (interview-surfaced)

Not "ads bad" (he'd tolerate a properly-placed static banner like GStrings has) but a **specific, named technical failure: Cumulative Layout Shift on the tuning screen.** *"A web dev with a year of experience knows you reserve the ad slot at render time. Whoever built Banjen's ad integration skipped that lesson."* He has named the bug, knows the fix, and has already designed the correct monetization model. He is not anti-monetization. He is anti-interruption.

### Behaviors

- Tunes 8–15 times a day across morning practice, three to five Zoom student lessons, evening sessions/recording.
- Daily stack: GStrings (mediocre UX, won't sell to him, has earned tolerance), LikeTones (mediocre but *promise-keeping* — "No Ads, No Purchases, No Registration"), Boss TU-3 pedal for electric.
- Five-minute install test: (1) mic permission before any tap? → uninstall. (2) ads on tuning screen with layout shift? → uninstall. (3) more than 2 taps to play a D? → fail. (4) settings screen full of upsell prompts? → uninstall.
- Distribution authority on r/banjo and r/WeAreTheMusicMakers; small but trust-rich YouTube channel. Almost recorded a "best tuner apps" video featuring Banjen — shelved it after the ad-shift incident because he wouldn't risk audience trust on a tool that would make viewers rage-tap an ad.
- Already DM'd app developers and converted one (a metronome dev who replied in 4 hours). His paid loyalty bar is "indie dev responds within a day." His tweet to GuitarTuna's automated support address — never opened the app again.
- Records with A=432Hz artists; currently generates reference tones in Ableton because no tuner ships adjustable reference pitch.
- Cavaquinho overlap is genuinely useful to him personally — second persona to validate DGBD-cavaquinho as a real, not theoretical, market.

### Verbatim quotes (Marcus interview, 2026-05-20)

> "I tapped the D string, the tone started playing, I'm holding my banjo up against my chest… and then a banner ad loaded in at the bottom of the screen — and the whole layout shifted up to make room for it… My thumb was already in motion to tap the D again to re-hear it, and instead my thumb landed on the ad. Chrome opens. My banjo string is still ringing behind the Chrome window. I switched back, uninstalled, went to GStrings, finished the session." *(Q2)*

> "LikeTones isn't great. It's fine. But 'fine and no ads' beats 'great and ads' every single time. That's not a preference, that's math." *(Pull quote)*

> "You built the correct app and then monetized the one square inch you weren't allowed to touch — fix that and you have a real product; leave it alone and LikeTones takes your market while you're A/B testing banner placements." *(Q20)*

### Conditional triggers

- **Would convert (re-install) if:** Three non-negotiable conditions all true: (1) zero ads of any kind on the tuning screen — no banner, no sponsored anything, no late-loading content. (2) Homescreen widget — tap, hear D, no app launch. (3) Adjustable reference pitch A=432–446Hz. Bonus: GDAE/CGBD presets (move opinion of Banjen as a product, even if he personally doesn't use them); cavaquinho-labeled tuning preset (he'd shoot a "best tuner apps" YouTube episode featuring it).
- **Would lose (stays gone) if:** Layout shift on the tuning screen recurs once after reinstall — "it's gone forever." LikeTones continues to credibly hold the "No Ads, No Purchases, No Registration" position and Banjen doesn't ship the tuning-surface fix in the next quarter — Marcus's cohort migrates and is unreachable by anything except a category-defining feature (widget + adjustable pitch + multi-instrument profiles).

---

## Cross-Persona Patterns

**1. Ad on the tuning surface is the universal kill condition (5/5 personas).** Confirmed across all five interviews. Strongest specificity from Marcus (Cumulative Layout Shift, named bug), broadest emotional weight from Eileen ("sacred"), highest behavioral cost from Wendell (turns off phone entirely, year-long avoidance). Subtype distinction: a **fixed, render-time-reserved bottom banner** is tolerable to Marcus/Lúcia (GStrings model); a **late-loading, layout-shifting** banner is fatal to all five. Audio ads during tuning are fatal to all five with no subtype exception.

**2. Microphone permission is a brand-trust signal (5/5).** Harold refuses by default ("phones listening"). Wendell will press "no" on any unfamiliar dialog. Lúcia treats it as suspicious if the app could work without it. Eileen calls it "incompetent or up to something" for a reference-tone app. Marcus's older student Bill won't install mic-permission apps. **No mic required** should be a top-line listing claim, not a hidden technical detail.

**3. Subscriptions are disqualifying, one-time IAP is welcomed (5/5).** Harold: "You don't subscribe to a hammer." Lúcia: Cifra Club trauma transfers to *any* subscription model. Eileen: "I will not pay €1.99 a month for the rest of my natural life to tune a banjo." Wendell: incomprehensible. Marcus: closes App Store page on $2.99/mo, would happily pay $5 one-time. Pricing band: **$3–5 / R$9.90–14.90 / €3–5 one-time, ad-free unlock.**

**4. Mic-based tuners structurally fail in real environments (4/5: Eileen, Lúcia, Wendell, Marcus).** Pub TV, samba bar football match, rehearsal hall percussion, multi-instrument live tracking. The reference-tone architecture is not a "different way to tune" — it is the **only architecture that works where these users actually play.** This is Banjen's structural moat against clip-ons, chromatic apps, and the entire mic-based category.

**5. Distribution is word-of-mouth, not Play Store (5/5, channel varies).** Harold → Ray (teacher) → jam circle. Lúcia → WhatsApp roda group (60+) → downstream groups. Eileen → TheSession.org gear thread + session WhatsApp (12). Wendell → Camille (daughter) + bandmates + GP. Marcus → Reddit + small YouTube channel. **None of the five uses Play Store editorial discovery.** Lúcia and Eileen actively distrust top-ranked listings (sponsored, fake reviews). Banjen's growth model must serve five distinct *referrer-shaped* surfaces, not one Play Store SEO surface.

**6. Looping playback enables two-handed tuning (4/5: Harold, Lúcia, Wendell, Eileen).** All four cannot hold the phone and turn the peg simultaneously. The tone must keep playing until they tap to stop. This is a quiet structural requirement competitors regularly violate (one-shot-tap audio).

**7. Tuning friction is a stake threat, not an annoyance (3/5: Harold, Wendell, Eileen).** Harold's discipline / cognitive-health regimen; Wendell's clinical depression risk via missed Tuesdays; Eileen's musician credibility in front of peers. For these three, "fix the tuning friction" is not a UX improvement — it protects a downstream outcome that matters more than the tuning itself. Marketing copy that says "tune faster" misses this. Copy that says "don't lose the morning / don't lose the session / don't lose Tuesday night" connects.

---

## Design Principles (derived)

**1. The tuning surface is sacred — reserve the ad slot at render time or not at all.**
*Why:* All five personas have the same kill condition. Marcus has named the precise technical mechanism (Cumulative Layout Shift). Wendell turns off his phone for a year. This is non-negotiable.
*Application:* Any banner ad must be (a) bottom-fixed, (b) reserved at first paint so no layout shift can occur, (c) never animated, (d) never audio-capable, (e) absent entirely during the active tuning interaction. Audio ads and interstitials on the tuning screen are forbidden by product policy, not just deferred by roadmap.

**2. Looping reference tones are the architecture, not a feature.**
*Why:* Two-handed tuning is the actual physical act. Mic tuners fail in 4/5 of the personas' real environments. Reference-tone-first is the structural moat.
*Application:* Tone plays until user taps to stop. Default loop length ≥ time-to-tune-one-string (5–10 sec). No mic permission requested, ever, for the tuning flow. "No microphone required" surfaced as a top-line value claim on the Play Store listing.

**3. Tuning presets are first-class — DGBD, GDAE, CGBD all on the home screen.**
*Why:* Eileen will not install if she can't see GDAE in five seconds. Wendell cannot tune to DGBD; he needs CGBD. Lúcia happens to land on DGBD coincidentally but the *cavaquinho label* matters more than the tuning.
*Application:* Home screen is (a) tuning picker (DGBD / GDAE / CGBD, with labels including "Plectrum/Cavaquinho," "Irish tenor," "Chicago"), (b) four big buttons that play the notes. No "alternative tunings" submenu. No settings dive. The picker IS the home screen.

**4. Touch targets and motor accessibility are non-decorative.**
*Why:* Wendell's tremor + arthritis + reading glasses + tappable-button anxiety. Older-adult research (Jin et al. 2007) shows 60dp targets with 16dp spacing reduce error rates ~70%.
*Application:* String buttons ≥60dp (≈thumb pad size), ≥16dp spacing, high-contrast labels readable without glasses, warm banjo timbre (Wendell: "a banjo. Not a beep."), no surfaces near string buttons that can be accidentally tapped into a billing or browser flow.

**5. Pricing is a one-time hammer purchase, never a relationship.**
*Why:* All five personas. Harold's "hammer" framing, Lúcia's Cifra Club trauma, Eileen's "rest of my natural life," Wendell's transaction fear, Marcus's "category thing."
*Application:* Single one-time IAP, $3–5 / €3–5 / R$9.90–14.90 to remove ads forever. No "free trial." No subscription tier. No "Pro" terminology. Purchase flow simple enough that Camille can complete it for Wendell on a Sunday visit.

**6. Localization is identity, not translation.**
*Why:* Lúcia will not click an English listing. The pt-BR title with "cavaquinho" / "cavaco" is the single highest-leverage zero-code growth move available. Brazilian users explicitly read translation quality as a respect signal.
*Application:* Ship pt-BR Play Store listing ("Afinador de Cavaquinho e Banjo") with human-translated description, "afinador," "cavaco," "samba," "choro" in keyword set. Treat Irish trad in en-IE with "GDAE," "tenor banjo," "Irish trad" surfaced similarly. Localize the cultural framing, not just the strings.

**7. Distribution is referrer-shaped, not Play-Store-shaped.**
*Why:* No persona discovers via Play Store editorial. Each persona has a single dominant referrer surface: Ray/teacher, WhatsApp roda, TheSession.org, Camille/family, Reddit/YouTube.
*Application:* Build per-persona referrer assets: a one-pager teachers can hand to students; a pre-written PT WhatsApp share message with a cavaco-friendly screenshot; a TheSession.org gear-thread-ready feature summary highlighting GDAE + no-mic + €3 one-time; a Camille-facing "set up Banjen for your parent" mini-guide; an indie-dev Reddit AMA stance for the Marcus cohort. Do not measure success in Play Store conversion alone.

---

## Changes from v2

| Status | Personas | Note |
|---|---|---|
| **Retained, deepened** | Harold M. (primary), Marcus T. (skeptic) | Harold's hidden cognitive-health motivation re-validated as clinical-grade, not folkloric. Marcus's churn pattern named precisely as Cumulative Layout Shift; three reinstall conditions confirmed and prioritized. |
| **Evolved (same archetype, refreshed)** | Siobhan K. → Eileen B. (58, Galway primary teacher); Betty L. → Wendell P. (71, New Orleans plectrum); Rafael S. → Lúcia G. (31, São Paulo cavaquinho) | Eileen ages-up to match social-analyst.md trad-influence concentration in older session veterans. Wendell shifts the v2 ad-fear frame to *avoidance learning* + clinical risk + Camille-as-install-gate. Lúcia moves the v2 NYC diaspora frame to the in-country BR domestic market (5–10× larger) and broadens gender frame; carries v2 WhatsApp distribution insight intact. |
| **Retired** | Jake R. (5-string clawhammer) | Retired in v2; v3 research confirms 5-string is the largest *banjo* segment but the cavaquinho adjacency is a higher-leverage non-banjo play, validating continued deprioritization. |
| **New (no v2 antecedent)** | None — all v3 personas trace to v2 archetypes via the synthesis-personas step; the *evidence base* (5 interviews + 5 research files + academic literature) is new, but the persona set is a refinement, not an expansion. |

**Biggest single change v2 → v3:** Harold's dementia-prevention motivation moved from "interview subtext" to "actively-protected clinical disclosure." He asked, mid-interview, whether his name could be kept off this. The product cannot surface this in copy — but it must be designed for.

---

## Distribution Implications

| Persona | Primary channel | Multiplier mechanic | Estimated reach per successful referral |
|---|---|---|---|
| **Harold M.** | Ray (banjo teacher) → Thursday jam circle → broader Banjo Hangout demographic | Teacher recommendation is the #1 acquisition driver for the over-60 English-language banjo cohort. One Ray-class teacher influences 8–15 students across a multi-year tenure. | ~10–20 per converted teacher; high trust, slow ramp, lifetime retention |
| **Lúcia G.** | WhatsApp roda group (60+) → each member's downstream WhatsApp groups → Brazilian cavaquinho YouTubers (Professor Damiro, Cavaquinho na Veia) | WhatsApp is the dominant Brazilian app-virality channel. One share in the right group propagates in hours, not days. Reputation cost of recommending a bad app is real — she vets before sharing. | ~500–1,000 cavaquinhistas in São Paulo within one week per share; CAC effectively zero |
| **Eileen B.** | TheSession.org gear-thread post + session WhatsApp (12) + Enda Scahill / Lisa Canny mention | Trad community is small but globally connected ("Dublin to Boston to Melbourne overnight"). A single trusted post reaches thousands of lurkers. Eileen sits on recommendations a full week before sharing. | ~200+ Irish tenor players globally per month after a sustained good experience; effectively saturates the GDAE segment within a quarter if shipped well |
| **Wendell P.** | Camille (daughter) → bandmates (Robert, Marcus the pianist) → potentially GP / social-prescribing referral channel | Not online. Acquisition happens only through family install + bandmate demo + emerging clinical "music is medicine" channel. Slow, but produces extremely high-LTV / extremely high-trust installs. | ~3–6 per Camille-installed instance via bandmates + family + clinical referral; saturates older-plectrum niche |
| **Marcus T.** | Reddit (r/banjo, r/WeAreTheMusicMakers) + ~2,800-sub YouTube channel + gig-musician friends | High-frequency, high-credibility, high-skepticism cohort. One "best tuner apps for banjo" YouTube episode (which he already drafted and shelved) reaches several thousand engaged viewers. He will not recommend until the ad-shift bug is fixed. | ~2,000–5,000 working-musician views per recovered recommendation; cohort migrates as a bloc to whichever tool ships the widget + no-shift fix first |

**Combined implication:** Banjen's growth is not Play-Store-shaped. It is **referrer-shaped across five separate trust networks**, each gated by a different precondition: Harold's network gates on Harold's three-week silence, Lúcia's gates on a pt-BR listing, Eileen's gates on a GDAE preset, Wendell's gates on Camille's install ritual, Marcus's gates on the layout-shift fix + widget. Ship all five preconditions and Banjen unlocks five distribution multipliers in parallel. Ship none and Banjen ships into Play Store search results — the one surface none of these users use.

---

STATUS: complete
OUTPUT: /Users/dan/projects/banjen/docs/product/v3/user-personas.md
PRIMARY_PERSONA: Harold M., 67
TOP_3_PAINS: (1) Any ad / audio / layout-shift on the tuning surface — universal kill across all 5 personas; (2) Tuning friction as a stake threat (Harold's cognitive-health discipline / Wendell's clinical depression risk via missed Tuesdays / Eileen's session credibility); (3) Language and tuning-preset gates that block discovery before install — Lúcia (pt-BR listing absent) and Eileen (GDAE buried in settings)
PERSONAS_KEPT: 5 (Harold retained-deepened, Eileen evolved from Siobhan, Wendell evolved from Betty, Lúcia evolved from Rafael, Marcus retained-reframed)
BIGGEST_CHANGE: Harold's hidden cognitive-health motivation upgraded from emotional subtext to actively-protected clinical disclosure; tuning friction reframed from "annoyance" to "interruption of a health-relevant habit" with implications for copy, monetization, and distribution policy
