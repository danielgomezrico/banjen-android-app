// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "BanjenCore",
    platforms: [
        .iOS(.v17),
        .macOS(.v13),
    ],
    products: [
        .library(name: "BanjenCore", targets: ["BanjenCore"]),
    ],
    targets: [
        .target(name: "BanjenCore"),
        .testTarget(name: "BanjenCoreTests", dependencies: ["BanjenCore"]),
    ]
)
