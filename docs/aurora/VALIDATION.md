# Validation for Aurora Frontier 1.0.0

## Automated engine tests

Final local `tests:test`: **345 passed, 0 failed**, plus **1 intentionally skipped** upstream remote-Mod test. New focused coverage is 5 content tests and 6 challenge tests.

- Registration, unlocks and build types for all five additions
- Target filters, ammunition/status effects, drone commands and support ability
- Exactly 2,400 powered simulation ticks to produce a drone, with exact item consumption
- Thirty-wave spawn bounds and victory boundary, connected power and starter ammunition
- Actual mining/conveyor delivery, automatic ammunition refill and ground reachability
- Deterministic map restart and native save/reload preserving scenario/power
- HUD guard: the live wave timer must not be replaced by a never-completing objective

The skipped test downloads the unrelated Allure third-party mod from GitHub. Its source is retained and it can be explicitly enabled in the test JVM. No external mods are bundled in this APK.

## Android package checks

- Built from the full upstream source with JDK 17, Gradle 9.3.1, Android Gradle Plugin 8.13.2 and SDK/build-tools 36
- Separate package `io.github.ashuraohma.aurorafrontier`, release version `1.0.0-aurora-v160.5`, versionCode 1
- Minimum API 21, target API 36, OpenGL ES 2.0
- ARM64, ARMv7, x86 and x86_64 native libraries included
- ARM64 native ELF LOAD segments align to 16 KiB; APK is zip-aligned for 16 KiB native pages
- Signed with a dedicated RSA-4096 key outside the source tree; Android v1, v2 and v3 signature verification passes
- ZIP integrity, native libraries, compiled custom classes, complete main/fallback sprite atlases, fonts, Chinese strings, license files and 114 original built-in map files checked

## Limits

No physical Android device or Android emulator was available in this environment. Package validation and desktop-engine checks are not an Android installation test; device-specific drivers, touch behavior and performance need an actual phone check. The complete 30-wave match has not been played manually to completion, so difficulty tuning remains a gameplay iteration item.

All original game assets and mechanics remain in the distribution. The dedicated challenge and its custom content are embedded; importing a separate Mod is not required. Local/private games should use matching Aurora builds, not stock clients/servers.

## Real-engine graphical smoke

Passed English desktop layout and Simplified Chinese mobile landscape/portrait layout checks using the real game code and assets on a software-rendered OpenGL framebuffer. Covered main menu, brief cancel/reopen/deploy, custom sprite loading, live first-wave defenses, save/reload and victory conditions. The final Aurora wordmark, Chinese menu entry and 1/30 wave timer were visually inspected.

The test machine has no accessible display socket. The smoke harness therefore used a temporary offscreen SDL/GLEW adapter outside the repository; it changed only the test rendering backend, not gameplay classes, assets or the production APK. These images are desktop-engine previews with mobile UI enabled, not screenshots from an Android phone.
