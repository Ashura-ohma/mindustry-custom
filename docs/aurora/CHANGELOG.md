# Aurora Frontier 1.0.0（基于 Mindustry v160.5）

## 代码导航

- `core/src/mindustry/content/AuroraContent.java`：炮塔、无人机、工厂、弹种和渲染叠加
- `core/src/mindustry/game/AuroraChallenge.java`：固定种子地形、初始基地、供电供弹、波次与开局流程
- `core/src/mindustry/core/ContentLoader.java`：在原版内容之后注册新内容，避免打乱原版 ID
- `core/src/mindustry/ui/fragments/MenuFragment.java`：桌面/手机、横屏/竖屏挑战入口
- `core/src/mindustry/ui/dialogs/GameOverDialog.java`：仅针对挑战标记显示胜利/失败和 30 波上限
- `tests/src/test/java/AuroraContentTests.java`、`AuroraChallengeTests.java`：新增回归测试
- `android/build.gradle`：独立应用 ID、版本、Android SDK 36 和构建兼容修复

## 开局怎么玩

1. 在主菜单选择「极光挑战」，阅读任务说明后部署
2. 初始基地已经有采铜、采铅、供电和两条核心卸载供弹线；不要拆断主供弹线
3. 棱镜负责地面穿透，星环负责防空，霜棘负责范围减速；注意其近距离盲区
4. 核心里的石墨/硅是有限的初始资源，需要逐渐建立自己的煤、沙、硅、石墨生产链
5. 无人机工厂需要供电和硅 55、钛 35、铅 25，每 40 秒生产一架萤火；可用原版指挥系统控制
6. 地图有铜、铅、煤、钛、钍和沙/水，支持继续发展原版工厂
7. 击退第 30 波后获胜。想体验自由建造、战役或地图编辑器，仍可使用原版入口

## 二次开发建议

- 调整平衡先修改 `AuroraContent` 中建造费用、耗电、射程、装填时间和弹种
- 调整关卡难度改 `AuroraChallenge.rules()` 的波组、间隔和初始物资
- 调整地图只改确定性生成方法并重新跑路径、物流与存档回归测试
- 新增内容在原版列表后追加；已发布内容不要随便重排或重命名，否则会影响旧存档
- 当前复用原版精灵并叠加独立颜色/晶体轮廓，因此包内不需要外部 Mod 或运行时资源下载
- 自绘新精灵应放入 `core/assets-raw/sprites/`，执行 `./gradlew tools:pack`，再重新打包

## 构建修复

- Android Gradle Plugin 从上游 8.2.2 升到 8.13.2，匹配当前 Gradle/JDK/SDK 36 工作流
- 明确 Android 合并 assets 与桌面 dist 必须依赖图集生成，避免干净构建先打包后生成的竞态
- 图集生成器尊重单位 `generateIcons=false`，允许内置新单位安全复用现有图集区域
- 默认测试不下载外部 Allure Mod；该上游网络集成测试保留为显式启用项

## 发布边界

- 这是非官方 GPL 分支；原作者署名和第三方许可保留
- 独立包名、图标和数据目录，官方 Mindustry 不会被覆盖
- 自定义内容改变联机兼容性，匹配本分支版本的玩家使用直接连接/局域网联机
- 不包含广告、支付、统计 SDK、账户注册或自动更新服务
