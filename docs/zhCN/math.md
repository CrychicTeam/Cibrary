# Math 教程

Pibrary 的 Math API 不替换原版类型。位置继续用 `Vec3`，盒子继续用 `AABB`，方向继续用 `Direction`。Pibrary 只补那些在玩法、HUD、投掷物、targeting、配置预览和 debug 工具里会反复手写的小段数学。

这套 API 分两层：

- `org.pickaid.pibrary.api.math` 是常用入口，适合直接写在普通玩法代码里。
- `org.pickaid.pibrary.api.math.core`、`geometry`、`curve`、`weight`、`view` 是更细的包，适合需要清楚表达职责的代码。

不要在同一个文件里同时 import 顶层 `PiAabbs` 和 `geometry.PiAabbs`。顶层 `PiAabbs` 有更多生产用 shape helper；`geometry.PiAabbs` 更小，主要服务 projection 和简单 bounds。

## 选哪个类

| 任务 | 类 |
| --- | --- |
| clamp、lerp、remap、wrap、epsilon 判断 | `PiScalars` 或顶层 `PiMath` |
| 一段稳定数值范围，例如 `0..100` | `PiRange` |
| 生成均匀采样点 | `PiSamples` |
| 安全归一化、投影、旋转、角度 | 顶层 `PiVectors` |
| 方块中心、短距离 ray 终点 | `geometry.PiVectors` |
| block face、edge、触碰 block、圆柱/球体粗略包围盒 | 顶层 `PiAabbs` |
| 线段 broad-phase bounds | `geometry.PiRays` |
| 水平/垂直方向、排除某个轴 | `geometry.PiDirections` |
| forward/up/right 坐标系 | `PiOrientation` |
| 动画、缩放、falloff 曲线 | `curve.PiCurves` 或顶层 `PiCurves` |
| loot、行为、variant 的权重选择 | `PiWeightedPool` / `PiWeightedEntry` 或顶层 `PiWeights` |
| 世界坐标投到屏幕 | `view.camera` + `PiProjector` |
| 判断可见、离屏、背后 | `PiVisibility` |
| 离屏箭头、边缘 marker | `PiEdgeIndicators` |

## 数值：从原始值变成可用值

数值 API 解决的是“这个 double 现在能不能安全用”。常见流程是先把原始值归一化，再 clamp，再映射到 UI 或玩法区间。

```java
PiRange energy = new PiRange(0.0D, maxEnergy);

double energy01 = energy.normalizeClamped(currentEnergy);
double barWidth = PiScalars.lerp(0.0D, 182.0D, energy01);
double alpha = PiScalars.clamp01(PiScalars.remap(currentEnergy, 0.0D, maxEnergy, 0.25D, 1.0D));
```

`normalize(...)` 不会 clamp。你想知道是否越界时用它。UI 条、alpha、进度条通常用 `normalizeClamped(...)`。

角度和循环计时用 wrap：

```java
double wrappedYaw = PiScalars.wrap(rawYaw, -180.0D, 180.0D);
double phase = PiScalars.positiveModulo(gameTime + partialTick, 20.0D) / 20.0D;
```

浮点数比较不要直接用 `==`：

```java
if (PiScalars.epsilonEquals(progress, 1.0D, 1.0E-4D)) {
    finish();
}
```

## Vec3：不要造自己的向量类型

Pibrary 继续使用原版 `Vec3`。安全归一化适合 targeting、projectile 和 AI 方向。fallback 应该选一个对当前逻辑有意义的默认方向。

```java
Vec3 origin = player.getEyePosition();
Vec3 rawDirection = target.subtract(origin);
Vec3 direction = PiVectors.safeNormalize(rawDirection, player.getLookAngle());

Vec3 end = origin.add(direction.scale(24.0D));
```

如果要做“向前、向右、向上”的局部坐标，先做 `PiOrientation`：

```java
PiOrientation aim = PiOrientation.fromForward(player.getLookAngle());

Vec3 center = player.position()
        .add(aim.offset(0.0D, 1.2D, 5.0D));
Vec3 leftWing = center.add(aim.offset(-2.0D, 0.0D, 0.0D));
Vec3 rightWing = center.add(aim.offset(2.0D, 0.0D, 0.0D));
```

这比在每个地方手写 cross product 更安全。`PiOrientation` 会保留稳定的 `forward/up/right`。

## AABB：搜索、预览和粗略碰撞

普通盒子用顶层 `PiAabbs`。它能做中心盒、体积、union、scale、face、edge、触碰 block 和圆形区域的粗略 bounds。

```java
AABB search = PiAabbs.centered(targetCenter, 6.0D, 3.0D, 6.0D);
List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, search);
```

做方块局部 shape 或 preview 时，用 `face(...)` 和 `unitEdges(...)`：

```java
AABB northPlate = PiAabbs.face(Direction.NORTH, 0.125D);
List<AABB> outline = PiAabbs.unitEdges(0.0625D);
```

遍历一个盒子碰到的 block 时，注意 `forEachTouchedBlock(...)` 会复用同一个 mutable pos。要保存结果时用 `immutable()`。

```java
PiAabbs.forEachTouchedBlock(search, pos -> {
    BlockPos stored = pos.immutable();
    inspectBlock(stored);
});
```

圆柱和球体 helper 是 broad-phase，不是精确圆形碰撞。先用这些 box 找候选，再做距离或角度的 narrow-phase 检查。

```java
for (AABB box : PiAabbs.approximateVerticalCylinder(player.position(), 5.0D, 2.0D)) {
    for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box)) {
        if (candidate.distanceTo(player) <= 5.0F) {
            applyAura(candidate);
        }
    }
}
```

线段查询先用 bounds 包住线段：

```java
Vec3 from = player.getEyePosition();
Vec3 to = from.add(player.getLookAngle().scale(32.0D));
AABB broadPhase = PiRays.segmentBounds(from, to, 0.5D);
```

## Curve：让数值变化有形状

曲线是 `double -> double`。它适合 charge、cooldown、falloff、HUD 动画、alpha、scale 和 debug preview。下面的例子使用的是子包 `org.pickaid.pibrary.api.math.curve.PiCurves`。

```java
PiCurve chargeCurve = PiCurves.easeInOutCubic();

double rawCharge01 = chargeRange.normalizeClamped(chargeTicks);
double shapedCharge = chargeCurve.sample(rawCharge01);
double damage = PiScalars.lerp(2.0D, 18.0D, shapedCharge);
```

多段曲线用于“前半段线性，后半段变慢”这类需求：

```java
PiPiecewiseCurve curve = PiPiecewiseCurve.of(List.of(
        new PiPiecewiseCurve.Segment(new PiRange(0.0D, 0.5D), PiCurves.linear()),
        new PiPiecewiseCurve.Segment(new PiRange(0.5D, 1.0D), PiCurves.easeOutQuad())
));
```

要把曲线变成预览表：

```java
List<Double> preview = PiCurveSamples.sample(PiCurves.smoothstep(), 9);
```

## Weight：可测试的权重选择

`PiWeightedPool` 适合需要确定行为的权重选择。随机玩法代码传 `RandomSource`，测试代码传固定的 `0..1` 数值。

```java
PiWeightedPool<ResourceLocation> pool = PiWeightedPool.of(
        PiWeightedEntry.of(id("common_spell"), 8.0D),
        PiWeightedEntry.of(id("rare_spell"), 2.0D),
        PiWeightedEntry.of(id("legendary_spell"), 0.5D)
);

Optional<ResourceLocation> picked = pool.select(level.random);
```

测试时不用猜 RNG：

```java
assertEquals(id("rare_spell"), pool.select(0.82D).orElseThrow());
```

如果你已经有普通列表和权重函数，顶层 `PiWeights.choose(...)` 更短：

```java
Optional<SpellType> spell = PiWeights.choose(spells, SpellType::weight, random);
```

## Camera：参数从哪里来

普通客户端渲染里，camera 值来自 Minecraft 自己的 camera、FOV 和窗口尺寸。Pibrary 不拥有 camera；它只把这一帧 camera 信息变成可复用的数学对象。

```java
Minecraft minecraft = Minecraft.getInstance();
Camera camera = minecraft.gameRenderer.getMainCamera();

PiCameraFrame frame = PiCameraFrame.gameplayView(
        camera.getPosition(),
        camera.getLookVector(),
        camera.getUpVector(),
        minecraft.options.fov().get(),
        minecraft.gameRenderer.getDepthFar(),
        minecraft.getWindow().getGuiScaledWidth(),
        minecraft.getWindow().getGuiScaledHeight()
);
```

如果你在 render event 里已经拿到了真实矩阵，用 `PiCameraFrame.fromMatrices(...)` 保留那套矩阵。这样以后迁移版本时，通常只需要改“如何取 camera/matrix”，后面的 projection、visibility、edge indicator 不用跟着改。

## 世界坐标投到屏幕

投影流程是：构造 `PiCameraFrame`，把世界点交给 `PiProjector`，再根据可见性决定画在原位置、屏幕边缘，还是不画。

```java
Vec3 targetPoint = entity.position().add(0.0D, entity.getBbHeight() + 0.5D, 0.0D);
PiProjectedPoint projected = PiProjector.projectPoint(frame, targetPoint);

switch (PiVisibility.classify(projected)) {
    case VISIBLE -> drawMarker(projected.screenX(), projected.screenY());
    case OFFSCREEN, BEHIND_CAMERA -> PiEdgeIndicators
            .fromWorldPoint(frame, targetPoint, 12.0D)
            .ifPresent(edge -> drawArrow(edge.screenX(), edge.screenY(), edge.angleRadians()));
    case NOT_PROJECTABLE -> {
    }
}
```

`OFFSCREEN` 表示目标在 camera 前方但超出窗口。`BEHIND_CAMERA` 表示目标在玩家背后。直接在背后的点没有左右信息时，`PiEdgeIndicators` 会把 marker 放到顶部边缘。

世界盒子也能投影：

```java
PiProjectedBounds bounds = PiProjector.projectBounds(frame, entity.getBoundingBox());

if (PiVisibility.classify(bounds) == PiVisibilityResult.VISIBLE) {
    drawRect(bounds.minX(), bounds.minY(), bounds.maxX(), bounds.maxY());
}
```

## Local Space：先变换，再投影

如果点不是世界坐标，而是相对某个实体、方块或系统原点的局部坐标，用 `PiSpaceTransform`：

```java
PiSpaceTransform transform = PiSpaceTransforms
        .offset(Vec3.atCenterOf(blockEntity.getBlockPos()));

PiProjectedPoint point = PiProjector.projectPoint(
        frame,
        transform,
        new Vec3(0.5D, 1.0D, 0.5D)
);
```

多个 transform 可以链起来：

```java
PiSpaceTransform localToWorld = localToEntity.then(entityToWorld);
```

## 迁移时保留什么

这套 Math API 的迁移策略是把易变的东西关在边缘：

- 原版 `Vec3`、`AABB`、`Direction` 继续作为数据类型，不引入自定义向量。
- camera 采集集中在 `PiCameraFrame.gameplayView(...)` 或你自己的 `PiCameraSource`。
- render 代码只消费 `PiProjectedPoint`、`PiProjectedBounds`、`PiEdgeIndicator` 这些结果。
- 玩法代码只消费数值、方向和 bounds helper，不碰矩阵细节。

升级 Minecraft 版本时，如果 camera getter、FOV getter 或矩阵来源变化，优先改 capture 代码。不要把版本差异散到每个 HUD marker、targeting 或 projectile 类里。
