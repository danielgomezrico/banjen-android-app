# Market Analyst Report — Banjen

**Date:** May 2026
**Analyst:** market-analyst (Banjen UX team, Phase 1 Empathize)
**Scope:** Sizing, competitive landscape (high-level), trends, adjacencies, TAM for a 4-string DGBD reference-tone tuner.

---

## Executive Summary

- **Global banjo player base is small but durable** — best estimate ~300k–500k active players worldwide, with ~10k pros and ~200k banjos sold/year. The 5-string bluegrass segment dominates (~70%); 4-string tenor/plectrum is a long tail concentrated in Ireland, jazz revival, and folk niches. [Banjo Hangout][Music Trades]
- **Cavaquinho is the single biggest hidden DGBD pool Banjen can serve** — Brazilian cavaquinho is "nearly always tuned DGBD" and is core to samba/choro/pagode (genres with tens of millions of listeners and active practitioners across Brazil + Portuguese-speaking diaspora). The dedicated "Cavaquinho Chord Bible: DGBD Standard Tuning" book confirms DGBD is the de-facto Brazilian standard. [Wikipedia: Cavaquinho][Amazon: Chord Bible]
- **Tuner-app market is bifurcated**: one mass-market hybrid leader (GuitarTuna, 100M+ installs, freemium subscription, banjo gated behind paywall) and a long tail of niche reference-tone or chromatic micro-apps with 10k–500k installs. Banjen sits in the niche tier and currently has no major direct ear-training competitor. [Google Play][Yousician]
- **6-month trend signal is mildly positive for acoustic/fretted**: fretted instruments grew 38% in the US over the past decade, acoustic captured ~54% of 2025 instrument market share, banjo resonator segment forecast to grow modestly through 2033. Bluegrass/Americana continues steady cultural drip; no breakout banjo moment in last 6 months. [NAMM 2025][Mordor]
- **Best minimal-code adjacency = cavaquinho first, then mandolin/octave mandolin (GDAE) and baritone uke (DGBE)** — all are 4-course/4-string fretted instruments that match Banjen's mp3-loop architecture. Cavaquinho is zero-code (same DGBD pitches, just listing/keyword changes). Mandolin GDAE requires only 4 new mp3 assets.

---

## Segment Sizing

| Segment | Standard tuning | Est. active players (global) | DGBD fit? | Confidence |
|---|---|---|---|---|
| 5-string bluegrass banjo | gDGBD (re-entrant) | ~200k–350k | Partial — 4 of 5 strings match | High |
| 4-string tenor banjo (Irish) | GDAE | ~30k–60k | No | Medium |
| 4-string tenor banjo (jazz/trad) | CGDA | ~5k–15k | No | Low-Med |
| 4-string plectrum banjo | CGBD | ~3k–8k | Close (3 of 4) | Low |
| 4-string banjo (DGBD "Chicago") | DGBD | ~5k–15k | **Exact** | Low |
| Brazilian cavaquinho | **DGBD** | **~500k–2M** | **Exact** | Medium |
| Portuguese cavaquinho | DGBD or DGBE | ~50k–150k | Exact/close | Low-Med |
| Mandolin | GDAE | ~1M–2M | No (adjacent) | Medium |
| Baritone ukulele | DGBE | ~200k–500k | Close (3 of 4) | Low-Med |
| Octave mandolin / tenor guitar | GDAE | ~50k–100k | No (adjacent) | Low |

**Key insight:** Banjen's literal addressable pool (DGBD-tuned instruments) is dominated by cavaquinho players, not banjo players, by roughly 5–10x. The Brazilian samba/choro/pagode ecosystem alone is materially larger than the entire global 4-string banjo population. The v2 UX finding (Rafael S. persona, cavaquinho hidden fit) is confirmed and likely understated.

Sources: [Banjo Hangout forum estimates], [Wikipedia: Cavaquinho], [Mandolin Cafe], [Ukulele Underground].

---

## Competitor Landscape (high-level)

| App | Approx. installs | Model | Approach | Notes |
|---|---|---|---|---|
| GuitarTuna (Yousician) | 100M+ | Freemium / Pro sub | Chromatic visual + hybrid | Banjo tuning behind paywall — opportunity for free alternative |
| Banjo Tuner – LikeTones | ~500k–1M | Free, ad-supported | Chromatic + reference tones | Probably the closest mass-market competitor |
| Ultimate Banjo Tuner (t4a) | ~100k–500k | Free w/ ads | Chromatic, multi-tuning | Targets 5-string with 6 tunings |
| Master Banjo Tuner (Netigen) | ~50k–200k, top-500 in 30d, ~4.8k DLs/mo | Free w/ ads | Chromatic | Active competitor |
| Banjo Tuner: Simple & Accurate | ~50k–100k | Free w/ ads | Chromatic mic-based | |
| **Banjen** | (own data) | Free w/ AdMob banner | **Pure reference-tone loop** | Differentiated: ear-training, DGBD-only, simplicity |

**Pricing patterns:** dominant model is free + interstitial/banner ads; subscription only credible at GuitarTuna scale. Sub-$5 one-time IAP "remove ads" is the realistic monetization ceiling for niche tuners.

**Strategic read:** Banjen's ear-training simplicity is a real moat against chromatic competitors for Harold M.-type personas. Deep teardown is competitor-analyst's job; this report flags that no incumbent currently markets to cavaquinho players in Portuguese.

---

## Trends (last 3–6 months)

1. **Fretted-instrument tailwind continues.** NAMM 2025 Global Report: fretted +38% over decade; acoustic instruments held 54.55% of 2025 instrument market share. No deceleration signal in late-2025/early-2026 data. [NAMM]
2. **Banjo specifically: steady, not breakout.** Banjo resonator segment forecast for "steady growth 2025–2033" driven by bluegrass/Americana/folk. No viral moment (no equivalent to the 2019 "Old Town Road" bump). [Data Insights Market]
3. **Beginner-friendly instruments outperform.** "Approachable, affordable, quick learning curves" — acoustic guitars and ukuleles dominate beginner search interest into early 2026. Implies beginner-positioned tuners benefit from broader top-of-funnel. [Accio]
4. **Search interest seasonality:** acoustic guitar searches peaked Nov 2025 (holiday gifting) — banjo likely follows the same Q4 curve. Banjen install spikes should be expected Nov–Jan.
5. **Subscription fatigue at the long tail.** GuitarTuna Pro paywalls niche tunings (banjo); this is the structural opening for ad-supported single-purpose tuners like Banjen.
6. **No detectable shift in Irish tenor / GDAE interest** — stable, traditional, anchored by session culture. Not a growth bet but not declining. [The Irish Place]

---

## Adjacent Market Opportunities

Ranked by code effort vs. addressable upside.

### Tier 1 — Zero code change
- **Brazilian cavaquinho (DGBD)** — Add "cavaquinho", "afinador de cavaquinho", "samba", "choro", "pagode" keywords to Play Store listing in pt-BR. Add pt-BR localization (already partially in place — pt locale exists per CLAUDE.md). Potentially the highest-leverage move in the entire roadmap. v2 UX research already flagged this; market sizing confirms it.
- **Portuguese cavaquinho** — same listing tweak, pt-PT variant; smaller pool but identical tech.

### Tier 2 — 4 new mp3 assets, minor UI
- **Mandolin (GDAE)** — ~1–2M global players, all standard-tuned to GDAE. Massive TAM relative to banjo. Requires G3/D4/A4/E5 reference tones and a tuning selector. Same architecture, same Compose pattern.
- **Baritone ukulele (DGBE)** — ~200k–500k players, DGBE standard. 3 of 4 strings overlap DGBD. Simple add.
- **Tenor banjo Irish (GDAE)** — shares tones with mandolin; once GDAE is in, this is free.
- **Octave mandolin / tenor guitar (GDAE)** — same GDAE assets at lower octave; cheap.

### Tier 3 — Selector + per-tuning UI work
- **5-string banjo (gDGBD)** — requires high-G (G4) drone tone + 5th button. Modest work. Addresses the v2 UX deprioritization caveat (Jake R. was removed but 5-string is still the biggest banjo segment).
- **Plectrum banjo (CGBD)** and **tenor CGDA** — niche but cheap once selector exists.

**Recommended product shape:** keep Banjen "the simplest DGBD tuner" as the home experience (Harold M. sacred), but add a hidden/secondary tuning picker that unlocks cavaquinho-marketed, mandolin, baritone uke modes — captured by separate Play Store listing experiments or in-app banner.

---

## TAM Estimates

| Market | Players (global) | Smartphone-equipped, tuner-seeking | Confidence |
|---|---|---|---|
| Banjen today (4-string DGBD banjo + 5-string overlap) | ~250k–400k | ~150k–250k | High |
| + Cavaquinho (BR + PT diaspora) | +600k–2.2M | +400k–1.5M | **Medium** (range wide, but order-of-magnitude defensible) |
| + Mandolin (GDAE, requires Tier-2 work) | +1M–2M | +700k–1.5M | Medium |
| + Baritone uke + Irish tenor + octave mandolin | +300k–700k | +200k–500k | Low-Medium |
| **Total expanded TAM (Tier 1+2)** | **~2.5M–5.3M** | **~1.5M–3.7M** | Medium |

**Confidence calibration:** banjo numbers are forum-derived (no industry census exists); cavaquinho numbers extrapolated from Brazilian samba/pagode participation (Brazil has ~200M people, samba is a national genre); mandolin numbers from Mandolin Cafe/forum lore. All within order of magnitude.

**Bottom line:** Tier-1 listing/keyword work alone plausibly 3–5x's Banjen's addressable market with zero engineering risk. Tier-2 mandolin support is the single highest-ROI engineering investment.

---

## Sources

- [Banjo Hangout — How many banjo players are there?](https://www.banjohangout.org/archive/368290)
- [McNeela Music — 4-string vs 5-string banjo](https://blog.mcneelamusic.com/4-string-banjo-5-string-banjo-differences/)
- [Clareen Banjos — Tenor vs 5-string](https://www.banjo.ie/tenor-banjo-versus-5-string-banjo/)
- [The Irish Place — Irish Banjo](https://www.theirishplace.com/traditional-irish-music/the-irish-banjo/)
- [McNeela — Irish Tenor Banjo Basics](https://blog.mcneelamusic.com/irish-tenor-banjo-buyers-guide/)
- [Wikipedia — Cavaquinho](https://en.wikipedia.org/wiki/Cavaquinho)
- [Amazon — Cavaquinho Chord Bible: DGBD Standard Tuning](https://www.amazon.com/Cavaquinho-Chord-Bible-Standard-Fretted/dp/1906207399)
- [TuCuatro — How to tune a Cavaquinho](https://tucuatro.com/learn/topic/cavaquinho-basic-tuning/)
- [NAMM — 2025 Global Report key takeaways](https://www.namm.org/blog/industry-insights-key-takeaways-2025-global-report)
- [Mordor Intelligence — Musical Instrument Market](https://www.mordorintelligence.com/industry-reports/musical-instrument-market)
- [Data Insights Market — Banjo Resonator Forecast 2025–2033](https://www.datainsightsmarket.com/reports/banjo-resonator-1915406)
- [Accio — 2025 Musical Instrument Trends](https://www.accio.com/business/trending-musical-instruments)
- [GuitarTuna on Google Play](https://play.google.com/store/apps/details?id=com.ovelin.guitartuna)
- [Banjo Tuner — LikeTones (Google Play)](https://play.google.com/store/apps/details?id=com.sonkins.tbanjo)
- [Ultimate Banjo Tuner (Google Play)](https://play.google.com/store/apps/details?id=com.t4a.tuner.banjo)
- [Master Banjo Tuner (AppBrain)](https://www.appbrain.com/app/master-banjo-tuner/pl.netigen.simplebanjotuner)
- [Banjen (Google Play)](https://play.google.com/store/apps/details?id=com.makingiants.android.banjotuner)
- [Mandolin Cafe — Best smartphone tuner](https://www.mandolincafe.net/home/forum/instruments-and-equipment/equipment/101509-best-smartphone-tuner-app-for-mandolin/page2)
- [Guitar Tunio — Mandolin GDAE tuning guide](https://guitartunio.com/mandolin-standard-tuning-proper-technique)
- [Ukulele Underground — Baritone DGBE / GDAE tuning](https://forum.ukuleleunderground.com/threads/best-way-to-string-a-baritone-ukulele-for-gdae-octave-mandolin-tuning.139764/)
- [Live Ukulele — Baritone resources](https://liveukulele.com/baritone/)

STATUS: complete
Output: /Users/dan/projects/banjen/docs/product/v3/research/market-analyst.md
