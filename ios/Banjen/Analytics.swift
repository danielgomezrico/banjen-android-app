import FirebaseAnalytics

/// Lightweight wrapper around Firebase Analytics.
/// Safe to call even if Firebase is not configured (no GoogleService-Info.plist).
/// Event names and parameter keys are kept in sync with the Android implementation.
enum Analytics {
    static func logEvent(_ name: String, parameters: [String: Any]? = nil) {
        FirebaseAnalytics.Analytics.logEvent(name, parameters: parameters)

        #if DEBUG
        print("Analytics: \(name) \(parameters ?? [:])")
        #endif
    }
}
