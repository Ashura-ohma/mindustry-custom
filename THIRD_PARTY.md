# Third-party notices: Aurora Frontier / 极光前线

Aurora Frontier is an unofficial modified build of **Mindustry v160.5**, created by **Anuken and contributors**. It is not an official Mindustry release and does not imply endorsement by the original authors.

## Mindustry

- Source: https://github.com/Anuken/Mindustry
- Base tag: `v160.5`
- Base commit: `067c720a8817c1c9fb586c03898a7d948caaed56`
- License: GNU General Public License, version 3; see the original [LICENSE](LICENSE) and the packaged copy at [core/assets/licenses/GPL-3.0.txt](core/assets/licenses/GPL-3.0.txt)
- Original contributor list: [core/assets/contributors](core/assets/contributors)

The Aurora changes include native turrets, a support drone and factory, the Frozen Pass challenge, related translations, and fork-specific Android branding. Existing upstream copyright notices, credits, translations, and license terms are retained. The new equipment reuses existing game sprite regions with additional drawing and effect code.

The corresponding modified source and build instructions should accompany distribution of this fork. This notice does not replace the GPL or the separate licenses of the components below.

## Runtime libraries

### Arc

- Author/project: Anuken and Arc contributors
- Source: https://github.com/Anuken/Arc
- Pinned commit: `8eb00ffff0126d0576c67df46f99b8f6bccd96fe`
- License: Apache License 2.0, copied without modification from the pinned checkout's `LICENSE` to [core/assets/licenses/Apache-2.0.txt](core/assets/licenses/Apache-2.0.txt)
- Used modules include the core framework, Android backend, networking, font rendering, text effects, graphics, and native support libraries

Individual incorporated components can carry additional notices. For example, `extensions/arcnet/src/arc/net/dns/NameserverProvider.java` and the related provider classes identify their dnsjava-derived code as BSD-3-Clause. Arc's pinned native build scripts reference SoLoud, stb_image, and FreeType; this document does not relicense those components as Apache-2.0. Preserve their component notices with their native distributions.

### LZ4 Java

- Dependency: `at.yawk.lz4:lz4-java:1.10.2`
- Source: https://github.com/yawkat/lz4-java
- License identified by the resolved dependency POM: Apache License 2.0
- Project developers credited by that POM: Adrien Grand, Rei Odaira, and Jonas Konrad
- License copy: [Apache-2.0.txt](core/assets/licenses/Apache-2.0.txt)

This identifies the Java artifact's declared license. Native compression/hash components retain their own applicable notices.

### Rhino

- Dependency: `com.github.Anuken:rhino:32395f942976a6d1d21ec9877664554cd5762306`
- Mindustry fork: https://github.com/Anuken/rhino
- Original project: https://github.com/mozilla/rhino
- Rhino's published licensing information: https://rhino.github.io/license.html

The original Rhino project identifies MPL 2.0 as its principal license and calls out additional notices for incorporated code. The pinned stripped-down fork does not include a top-level license file in the checked repository listing, and its resolved JAR does not contain a LICENSE/NOTICE entry. Consult and retain the applicable source-file notices rather than assuming the game's GPL replaces them.

### Android dx

- Dependency: `com.jakewharton.android.repackaged:dalvik-dx:9.0.0_r3`
- Packaging project: https://github.com/JakeWharton/dalvik-dx
- Original library: Android Open Source Project's Dalvik dx

The packaging project's README distinguishes its Apache-2.0 packaging code from the code deployed in the JAR, whose notices are in `platform_dalvik/NOTICE`. Preserve the NOTICE belonging to the deployed version. The build's declaration of this dependency is not a claim that every file has the same license.

## Fonts and icons inherited from Mindustry

The following identifications come from the supplied font files' embedded name/license tables and the upstream Fontello configuration. The font files themselves have not been changed for Aurora Frontier.

- `monospace.woff`: **Fira Code Medium 6.002**, copyright 2014–2021 The Fira Code Project Authors. SIL Open Font License 1.1. Project: https://github.com/tonsky/FiraCode
- `font_jp.woff`: **Noto Sans JP Medium 2.004**, copyright 2014–2021 Adobe, with reserved font name “Source”. SIL Open Font License 1.1. The embedded designer credits include Ryoko Nishizuka, Paul D. Hunt, and Sandoll Communications. License: https://openfontlicense.org/open-font-license-official-text/
- `tech.ttf`: **Darktech LDR**, copyright 2012 Michał “Neoqueto” Nowak. Its embedded license is Creative Commons Attribution-ShareAlike 3.0. Original work: https://fontstruct.com/fontstructions/show/715809 ; license: https://creativecommons.org/licenses/by-sa/3.0/
- `logic.ttf`: **ProggyCleanTT**, by Tristan Grimmer. The official Proggy Fonts project provides the MIT license and credits copyright 2004, 2005 Tristan Grimmer: https://github.com/bluescan/proggyfonts/blob/master/LICENSE
- `icon.ttf` and the icon portion of `font.woff`: merged with **Fontello**. Fontello's tool license does not replace the licenses of the fonts it packages: https://github.com/fontello/fontello#license

The supplied [Fontello configuration](core/assets-raw/fontgen/config.json) identifies these source collections:

| Collection | Credited author | Source-font license information |
| --- | --- | --- |
| Font Awesome | Dave Gandy | SIL OFL; https://github.com/fontello/awesome-uni.font/blob/master/config.yml |
| Typicons | Stephen Hutchings | SIL OFL; https://github.com/fontello/typicons.font/blob/master/config.yml |
| Zocial | Sam Collins | MIT; https://github.com/fontello/zocial.font/blob/master/config.yml |
| Iconic | P.J. Onori | SIL OFL; https://github.com/fontello/iconic-uni.font/blob/master/config.yml |
| Entypo | Daniel Bruce | SIL OFL for the Fontello source font; https://github.com/fontello/entypo/blob/master/config.yml |
| MFG Labs | MFG Labs | SIL OFL; https://github.com/fontello/mfglabs.font/blob/master/config.yml |
| Elusive | Aristeides Stathopoulos | SIL OFL; https://github.com/fontello/elusive.font/blob/master/config.yml |
| Brandico | Fontello project contributors | SIL OFL for the font; its separate icon artwork is CC BY; https://github.com/fontello/brandico.font#license |
| Modern Pictograms | John Caserta | SIL OFL; https://github.com/fontello/modernpics.font#license |

The configuration also contains custom icons inherited from Mindustry. The main `font.woff` has been merged upstream; its Fontello name table alone does not identify every underlying text-glyph source. This inventory therefore does not claim to reconstruct all historical font/asset provenance or replace the inherited upstream notices.

## Scope

This document records source-backed credits and license references for the modified Android build. It is not an exhaustive legal audit of all transitive dependencies or game assets. The source checkout's original notices and each component's license remain authoritative. Full GPL-3.0 and Apache-2.0 texts are packaged in `core/assets/licenses/`; other licenses linked here remain independently applicable.
