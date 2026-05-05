# Projectile Trace

Projectile trace 用来把“从 A 到 B 的飞行检测”集中成一个请求。业务代码给出起点、终点、碰撞盒、目标过滤和命中数量；底层 tracer 负责 block/entity 检测和 graze 结果。

```java
PiProjectileTraceRequest request = new PiProjectileTraceRequest(
        level,
        caster,
        eyePosition,
        eyePosition.add(look.scale(24.0D)),
        new AABB(-0.15D, -0.15D, -0.15D, 0.15D, 0.15D, 0.15D),
        PiTargetQuery.look(24.0D, 0.5D).livingTargetsOnly(),
        PiProjectileCollisionMode.STOP_ON_BLOCK,
        4,
        0.25D
);

PiProjectileTraceResult result = PiProjectileTraces.trace(request);
```

常见用法：

- 真正命中的实体走 damage、knockback、点燃等逻辑。
- graze 命中用于擦边特效、声音、粒子，不当作真正命中。
- `maxEntityHits` 限制一次 projectile 最多处理多少目标，避免大范围穿透弹无限扩散。
- `PiTargetQuery` 负责描述查找范围、是否只要 living entity、是否需要视线等基础规则。更具体的友军/敌人判断放在命中后的业务逻辑或项目自己的 resolver 里。

Forge 1.20.1 的实际 tracer 在 runtime 层安装。上层代码只依赖 `PiProjectileTraceRequest` 和 `PiProjectileTraces`，后续版本迁移时优先换 runtime 实现。
