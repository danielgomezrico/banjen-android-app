//
//  BanjenApp.swift
//  Banjen
//
//  Created by Dan on 28/06/26.
//

import SwiftUI
import GoogleMobileAds
import FirebaseCore

@main
struct BanjenApp: App {
    init() {
        // Firebase Crashlytics: only configure when the project's GoogleService-Info.plist
        // is bundled. Until you add it, the app still runs (Crashlytics simply stays off).
        // Add it: download GoogleService-Info.plist from the Firebase console for the iOS
        // app (bundle id com.banjen.ios.Banjen) and drop it into ios/Banjen/.
        if Bundle.main.url(forResource: "GoogleService-Info", withExtension: "plist") != nil {
            FirebaseApp.configure()
        }

        // Google Mobile Ads — requires GADApplicationIdentifier in Info.plist (set as a
        // build setting; currently Google's TEST app id — replace before shipping).
        MobileAds.shared.start(completionHandler: nil)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
