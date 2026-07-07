import SwiftUI
import GoogleMobileAds

/// Real AdMob anchored banner (320×50) on a dark slot. Lives in a fixed-height slot
/// in `EarView` (below the tuning surface) so it can never cause layout shift on the tuner.
///
/// The banner unit id and the app id both come from `ios/Config/Ads.xcconfig`
/// (Google TEST ids by default; CI overrides them with the real ids via a
/// gitignored `Ads.local.xcconfig`). The unit id is surfaced through the
/// `GADBannerUnitIdentifier` Info.plist key and read here at runtime; the app id
/// flows into `GADApplicationIdentifier`. See `Ads.local.xcconfig.example`.
struct AdBannerView: View {
    /// Google sample TEST banner unit — used when the Info.plist value is missing
    /// or left unsubstituted (e.g. running without the config wired up).
    private static let testBannerUnitID = "ca-app-pub-3940256099942544/2435281174"

    /// Banner unit id resolved from `GADBannerUnitIdentifier`, falling back to the
    /// test unit when absent/empty or when the `$(VAR)` token wasn't substituted.
    private static let bannerUnitID: String = {
        let id = Bundle.main.object(forInfoDictionaryKey: "GADBannerUnitIdentifier") as? String
        guard let id, !id.isEmpty, !id.hasPrefix("$(") else { return testBannerUnitID }
        return id
    }()

    var body: some View {
        // Floats over the (cream) tuning surface like Android's AdView — transparent
        // until a banner actually loads, so no dark box shows over the canvas.
        BannerContainer(adUnitID: Self.bannerUnitID)
            .frame(width: 320, height: 50)
    }
}

private struct BannerContainer: UIViewRepresentable {
    let adUnitID: String

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> BannerView {
        let banner = BannerView(adSize: AdSizeBanner) // 320×50 standard banner
        banner.adUnitID = adUnitID
        banner.delegate = context.coordinator
        context.coordinator.banner = banner
        if let rootVC = Self.rootViewController() {
            banner.rootViewController = rootVC
            banner.load(Request())
            context.coordinator.didLoad = true
        }
        return banner
    }

    func updateUIView(_ uiView: BannerView, context: Context) {
        // Root view controller can be nil during the very first render; load once it exists.
        if !context.coordinator.didLoad, let rootVC = Self.rootViewController() {
            uiView.rootViewController = rootVC
            uiView.load(Request())
            context.coordinator.didLoad = true
        }
    }

    private static func rootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?.rootViewController
    }

    final class Coordinator: NSObject, BannerViewDelegate {
        var didLoad = false
        weak var banner: BannerView?
        private var retries = 0
        private let maxRetries = 5

        func bannerViewDidReceiveAd(_ bannerView: BannerView) {
            retries = 0
        }

        func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
            // Recover from transient failures (e.g. network timeouts) with backoff.
            guard retries < maxRetries else { return }
            retries += 1
            let delay = Double(retries) * 4.0
            DispatchQueue.main.asyncAfter(deadline: .now() + delay) { [weak self] in
                guard let banner = self?.banner else { return }
                if banner.rootViewController == nil {
                    banner.rootViewController = BannerContainer.rootViewController()
                }
                banner.load(Request())
            }
        }
    }
}
