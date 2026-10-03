# 极光前线 · Aurora Frontier

A clearly branded, unofficial **full Mindustry v160.5 fork**, built from source. Original Mindustry by Anuken and contributors is retained, including campaign, factory logistics, sandbox, map editor and local/custom-server multiplayer. This is not a WebView game or an APK repack.

## 新内容 / New content

- **极光挑战 / Aurora Challenge**: launch directly from the main menu; a deterministic snow valley defense scenario with 30 waves, a working starter economy and sample Aurora defenses.
- **棱镜 / Prism**: powered piercing beam turret.
- **星环 / Halo**: homing anti-air burst turret.
- **霜棘 / Rime**: slowing/freezing artillery with differentiated ammunition.
- **萤火 / Glimmer**: healing combat/support drone, built in the **极光无人机工厂 / Aurora Foundry**.
- Chinese and English content descriptions; all original content is preserved.

Aurora custom units/buildings are available in the build menus without research prerequisites. Start with the dedicated challenge to see them in action. Factory units still require resources and power. You can continue building the normal mining, refining and transport economy during the challenge.

## Android installation

The release APK uses a separate package, `io.github.ashuraohma.aurorafrontier`, so it can coexist with official Mindustry. Android 5.0 (API 21) or newer; OpenGL ES 2.0 required. Native code is packaged for ARM64, ARMv7, x86 and x86_64. Device performance still depends on the hardware and map size.

Use the separately supplied APK (Android packages are not published to GitHub), allow installation from the app used to open it, then launch **极光前线 / Aurora Frontier**. Keep the APK signing identity for updates; a differently signed build cannot replace this installation without uninstalling it. Back up saves before uninstalling.

Custom content changes network compatibility. Automatic official community-server discovery is disabled in this fork. Direct/LAN multiplayer is intended for players using the same Aurora build. Do not use this build to join ordinary official servers. Original mod browsing remains optional; third-party mods are not guaranteed compatible.

## Build from corresponding source

Pinned upstream: `Anuken/Mindustry` tag `v160.5`, commit `067c720a8817c1c9fb586c03898a7d948caaed56`.

Requirements:

1. JDK 17 (Temurin works).
2. Android command-line SDK, platform `android-36`, build-tools `36.0.0`; review and accept Google's SDK terms yourself.
3. A checkout of the pinned Arc source **next to this directory**:

```sh
git clone https://github.com/Anuken/Arc ../Arc
git -C ../Arc checkout 8eb00ffff0126d0576c67df46f99b8f6bccd96fe
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew tests:test android:assembleDebug -Pbuildversion=160.5
```

The wrapper pins Gradle 9.3.1; the Android plugin is upgraded to 8.13.2 for the SDK 36 build. Dependencies remain pinned in the build files. Arc includes the native binaries and corresponding native sources/build definitions. No NDK rebuild is needed for the supplied native artifacts.

On memory-constrained machines, use `scripts/build-aurora.sh` for a staged release build: it releases native image-packing memory before R8 runs.

Output: `android/build/outputs/apk/debug/android-debug.apk`. To produce an unsigned release: `./gradlew android:assembleRelease -Pbuildversion=160.5`, then align/sign it with your own private Android signing key. Never commit a keystore or passwords. Release keys are intentionally outside this repository. CI builds a temporary debug APK only to validate compilation and packaging; it is never uploaded. Its debug signature differs from the separately delivered release.

`tests:test` includes local upstream tests and focused Aurora content/challenge tests. The upstream Allure remote-mod integration test is opt-in (`aurora.externalModTests=true` in the test JVM) because it downloads and loads a third-party mod; it is excluded from the default offline-safe run. The GitHub workflow installs a checksum-pinned official Android command-line SDK, packs full-quality sprites, runs tests and validates a temporary debug APK in separate processes. Only test reports are published; APK artifact uploads, releases and Gradle cache uploads are disabled. Passing headless tests and APK validation do not replace a device installation test.

## License and attribution

The original [GPL-3.0 license](LICENSE) is preserved and applies to this fork's code. See [README-UPSTREAM.md](README-UPSTREAM.md), the in-game credits and [THIRD_PARTY.md](THIRD_PARTY.md) for attribution and dependency notices. New Aurora code/icon are also supplied under GPL-3.0. Mindustry's original authors do not endorse this fork.

The repository contains corresponding source and build instructions for the distributed APK, with pinned external source dependencies. Original asset attributions and credits are retained. Please report fork-specific problems in this repository rather than to upstream Mindustry.
