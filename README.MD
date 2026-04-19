# Pibrary

[English](README_EN.md)

`Pibrary` 是 PickAID 系列的根基础模组，不再承担“把所有引擎都塞进一个仓库”的职责。

当前 Start stage 的目标很明确：
1. 保留 `pibrary` 作为真正存在的公共核心模组；
2. 把跨仓库都要依赖的基础边界沉淀成稳定 API；
3. 为后续独立仓库提供统一的核心契约，而不是继续扩张旧单体实现；
4. 让老代码可回溯、可迁移，但不再让 README 继续把它包装成最终架构。

## 当前定位

`Pibrary` 现在负责以下长期核心能力：
1. 核心服务定位与基础 service registry；
2. 配置、诊断、状态、目标选择、注册等通用契约；
3. 实体生命周期、实体选择与高性能 projectile trace 的公共边界；
4. 面向可选 `PiJEICompat` 的 JEI 中立模块 / bootstrap / spec 契约；
5. 作为 Pi 系列公共依赖的最小稳定根；
6. 为独立基础库和 engine 模组提供共同语言。

以下内容不再定义为 `Pibrary` 自身最终边界：
1. 网络传输实现；
2. 序列化实现；
3. KubeJS 插件加速层；
4. 对象计数 / 反应图 / 数据图逻辑本体；
5. 相机、叙事、UI、实体特效、数据图等独立引擎。

## PiRegistrate

现在推荐把 `PiRegistrate` 当成主注册入口。常见内容、registry、host service、config、creative tab 都从一个根出去，初始化代码会明显更集中。

```java
public static final PiRegistrate REG = PiRegistrate.create(MODID);

public static final PiRegistryHandle<SpellType> SPELL_TYPES =
        REG.customRegistry("spell_type", SpellType.class)
                .codec(SpellType.CODEC)
                .defaultKey("basic")
                .register();

public static final PiLevelServiceType<WeatherService> WEATHER =
        REG.levelService("weather", WeatherState.class, WeatherService::new)
                .persisted()
                .noSyncByDefault()
                .register(WeatherService.class);

public static final PiConfigEntry<Integer> COMBAT =
        REG.config("combat", Codec.INT, 5)
                .scope(PiConfigScope.SERVER_DATA_PACK)
                .register();

public static final PiCreativeTabRegistration MAIN_TAB =
        REG.creativeTab("main", "PickAID")
                .register();
```

如果你已经在用 `PiLivingServices.host(...)`、`PiChunkServices.host(...)`、`PiLevelServices.host(...)` 这条直接路径，也不用改旧代码。那条路径仍然成立；只是新代码更建议从 `PiRegistrate` 收束。

## 现在怎么用

如果你要做一个持久化的 living service，做法和现在的 `CounterPlayerService` 一样：定义 service 类，注册 typed handle，然后一直通过这个 handle 获取实例。

```java
@PiLivingService(namespace = "example", path = "counter_player")
public final class CounterPlayerService extends PiStatePlayerService<CounterState> {
    public CounterPlayerService(PiLivingServiceContext context) {
        super(context);
    }
}

public final class ExampleLivingServices {
    public static final PiLivingServiceType<CounterPlayerService> COUNTER_PLAYER =
            PiLivingServices.host(CounterPlayerService.class).register();

    private ExampleLivingServices() {
    }

    public static void register() {
    }
}

CounterPlayerService service = ExampleLivingServices.COUNTER_PLAYER.get(player);
```

如果你要做一个持久化的 level service，也用同样的方式：定义 service 类，注册一个 typed handle，然后通过这个 handle 获取实例。

```java
@PiLevelService(namespace = "example", path = "counter_level")
public final class CounterLevelService extends PiStateLevelService<CounterState> {
    public CounterLevelService(PiLevelServiceContext context) {
        super(context);
    }
}

public final class ExampleLevelServices {
    public static final PiLevelServiceType<CounterLevelService> COUNTER_LEVEL =
            PiLevelServices.host(CounterLevelService.class).register();

    private ExampleLevelServices() {
    }

    public static void register() {
    }
}

CounterLevelService service = ExampleLevelServices.COUNTER_LEVEL.get(level);
```

`Pibrary` 只负责 bootstrap 已生成的 descriptor。真正要让这个 service 进入运行时，仍然应该由你的模组在自己的初始化阶段调用 `ExampleLevelServices.register()`。

如果你要做一个持久化的 chunk service，也延续同样的 handle-first 用法。

```java
@PiChunkService(namespace = "example", path = "counter_chunk")
public final class CounterChunkService extends PiStateChunkService<CounterState> {
    public CounterChunkService(PiChunkServiceContext context) {
        super(context);
    }
}

public final class ExampleChunkServices {
    public static final PiChunkServiceType<CounterChunkService> COUNTER_CHUNK =
            PiChunkServices.host(CounterChunkService.class).register();

    private ExampleChunkServices() {
    }

    public static void register() {
    }
}

CounterChunkService service = ExampleChunkServices.COUNTER_CHUNK.get(chunk);
Optional<CounterChunkService> existing = PiChunkServices.find(level, chunkPos, CounterChunkService.class);
CounterChunkService resolved = PiChunkServices.resolve(level, chunkPos, CounterChunkService.class);
```

`find(level, chunkPos, ...)` 不会加载区块。`resolve(level, chunkPos, ...)` 才是允许显式解析区块的路径。

## 仓库关系

当前推荐关系如下：
1. `Pibrary`：根核心模组与公共基础 API。
2. `PiNet`：独立网络基础库。
3. `PiSerializeKit`：独立序列化基础库。
4. `PiKubeJSCompat`：独立 KubeJS / RecipeJS / builder compat 库。
5. `PiJEICompat`：独立 JEI compat 库，把 `api/jei` 中立契约转成真实的 recipe viewer 插件。
6. `PiDataGraph`：承载对象计数、反应链、图驱动逻辑，并使用 `PiSerializeKit` 做强类型序列化。
7. 未来的 `PiEngine` 家族：承载相机、UI、动画、故事、渲染桥等上层引擎。

## 现阶段代码状态

这个仓库目前分成两层：
1. `src/main/java/org/pickaid/pibrary/api/**`
   这里开始建立新的核心 API 面。
2. `src/main/java/org/pickaid/pibrary/content/**` 与其他旧包
   这里仍保留旧单体时代代码，后续会逐步拆分迁移。

历史单体快照保留在：
`restore/legacy-monolith-20260331`

它的作用是：
1. 保留旧实现以便迁移和回看；
2. 避免在 start stage 里把旧逻辑重新塞回新架构说明里；
3. 让新 API 可以逐步接管旧内容，而不是一次性硬删。

## Start Stage 已建立的核心 API 方向

当前已经开始明确这些基础面：
1. `api/core`
   服务 key 与 service registry。
2. `api/config`
   配置条目与配置作用域边界。
3. `api/diagnostics`
   调试/诊断输出统一入口。
4. `api/registry`
   面向未来 registry kit 的注册请求与阶段定义。
5. `api/state`
   通用状态 key 与同步策略。
6. `api/targeting`
   目标选择查询与 resolver 契约。
7. `api/entity`
   实体生命周期与受击处理边界。
8. `api/projectile`
   面向性能优化的 projectile trace / impact 契约。
9. `api/jei`
   JEI 中立的 recipe-viewer 契约、模块收集面与 GUI / alias / transfer 规格。

这些类目前刻意保持轻量：
1. 先把边界做对；
2. 先能被多个 repo 共同引用；
3. 等实际 engine/compat 模组接入后，再决定哪些部分需要沉淀成实现。

## 当前不在本阶段直接做满的内容

以下内容本阶段只保留边界，不急着做重量级实现：
1. 自动配置装配器；
2. 通用网络同步框架；
3. 完整 registry helper runtime；
4. 复杂 targeting 几何实现；
5. 旧单体功能的一次性迁移。
6. 对象计数逻辑本体。

## Maven

当前仓库仍使用已有发布仓路径：

```gradle
repositories {
    maven { url = 'https://maven.mihono.cn/repository/pickaid1201/' }
}
```

## 下一步重点

1. 让 `Pibrary` 的核心 API 被 `PiNet`、`PiSerializeKit`、`PiKubeJSCompat` 和后续 engine repos 实际消费；
2. 让 `PiDataGraph` 承担对象计数与图驱动逻辑，`PiSerializeKit` 负责强类型序列化边界；
3. 逐步把旧单体中的通用部分抽离成稳定基础契约；
4. 按 repo 规划继续拆分引擎实现，而不是让 `pibrary` 再变回总包仓。
