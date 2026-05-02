# 富文本与可视 Component

`PiTexts.markup(...)` 用在“仍然需要是 Minecraft `Component`，但直接写
`append(...)` 链太啰嗦”的地方：spell tooltip、手册页、界面说明、JEI 描述和调试面板。

Pibrary 使用 `org.commonmark:commonmark` 解析文本。CommonMark 负责标准 Markdown
语法；Pibrary 负责把解析树转成 Minecraft component，并用 `PiTextMarkupScope` 处理游戏内的特殊行内节点。

## 为什么用 CommonMark

这个选择偏保守，但适合 Forge runtime。

- `commonmark-java` 是 Java 原生库，体量小，能稳定 jarJar。
- `flexmark-java` 功能更多，但更重，会把 text API 推向完整文档处理工具。
- `markdown-it` 和 `remark/unified` 的插件生态很强，但它们更适合 JS 构建工具，不适合直接放进 Forge 运行时。

因此现在的边界是：CommonMark 处理真正的 Markdown 解析，Pibrary 保持一个小而明确的游戏扩展层。

## 基础写法

```java
MutableComponent line = PiTexts.markup(
        "**Fireball** ![image](example:textures/gui/spell/fireball.png#size=18x18)\\n"
                + "Cost: [mana](example:fireball)");
```

默认支持这些内容：

- `**text**` 变成粗体。
- `*text*` 变成斜体。
- `` `text` `` 变成灰色行内代码。
- `\\n`、真实换行和 Markdown hard break 都会变成真实换行。
- `[label](https://example.invalid)` 变成带下划线的可点击链接。
- `[label](target)` 可以通过 `PiTextMarkupScope` 变成项目自己的内容。
- `![image](target)` 可以通过默认图片规则变成可视 component。

## 翻译 Key 和默认文本

游戏里实际使用时，建议同时提供翻译 key 和默认文本。key 进入语言文件；默认文本让缺语言文件、测试环境、开发预览也能直接显示。

```java
MutableComponent line = PiTexts.markupTranslatable(
        "tooltip.example.spell.fireball",
        "**{name}** ![image]({icon}#size=18x18)\\n"
                + "Cost: [mana]({spell})\\n"
                + "Cooldown: {cooldown}s",
        scope,
        PiTextArgs.of()
                .component("name", spell.displayName())
                .resource("icon", spell.icon())
                .resource("spell", spell.id())
                .number("cooldown", rules.cooldownSeconds()));
```

处理顺序是固定的：

1. 先用 key 从当前语言取原文。
2. 如果没有这条翻译，用传入的默认文本。
3. 替换 `{name}`、`{icon}`、`{spell}` 这类命名参数。
4. 再交给 CommonMark 和 `PiTextMarkupScope` 解析。

`PiTextArgs.text(...)` 和 `number(...)` 会在 markup 里按普通文字处理，避免玩家名字或数据里的 `*`、`[` 被误识别成 Markdown。`resource(...)` 适合放进链接 target，例如 `![image]({icon})`。`component(...)` 会把已有 Component 插回结果树，颜色、hover 和 click 事件不会先被压成纯字符串。

语言 datagen 可以用同一份 key/default。不同语言里的 `{...}` 参数必须一致，写漏时会在构建 bundle 时直接报错：

```java
PiTextKey fireballTooltip = PiTexts.textKey(
        "tooltip.example.spell.fireball",
        "**{name}** costs {mana} mana");

PiLanguageBundle bundle = PiLanguages.bundle("example")
        .entry(fireballTooltip, entry -> entry
                .locale("en_us", "**{name}** costs {mana} mana")
                .locale("zh_cn", "**{name}** 消耗 {mana} 魔力"))
        .build();
```

## Spell Tooltip 示例

当文本依赖游戏数据时，用局部 scope。markup 不应该写 `mana:8` 这种假值；它应该描述当前要显示什么，然后由 scope 读取真实数据。

```java
public static MutableComponent spellTooltip(ResourceLocation spellId) {
    SpellRules rules = ExampleDataConfigs.SPELL_VALUES.requireEntry(spellId);

    PiTextMarkupScope scope = PiTextMarkupScope.builder(spellId.getNamespace())
            .inline("mana", (inline, localScope) ->
                    Component.literal(rules.manaCost() + " mana").withStyle(ChatFormatting.AQUA))
            .inline("cooldown", (inline, localScope) ->
                    Component.literal(rules.cooldownTicks() + " ticks").withStyle(ChatFormatting.GRAY))
            .inline("range", (inline, localScope) ->
                    Component.literal(rules.range() + " blocks").withStyle(ChatFormatting.GREEN))
            .build();

    return PiTexts.markupTranslatable(
            "tooltip.example.spell.fireball",
            "**{name}** ![image]({icon}#size=18x18)\\n"
                    + "Cost: [mana]({spell})\\n"
                    + "Cooldown: [cooldown]({spell})\\n"
                    + "Range: [range]({spell})",
            scope,
            PiTextArgs.of()
                    .component("name", rules.displayName())
                    .resource("icon", rules.icon())
                    .resource("spell", spellId));
}
```

手册页和 JEI 描述也走同样思路：先拿到数据，再让局部 inline handler 把数据渲染成文本、图标或其他可视节点。

## 图片和动图

默认 scope 注册了 `image`、`img`、`gif`、`anim`、`animation`。

```java
MutableComponent icon = PiTexts.markup(
        "Icon: ![image](example:textures/gui/spell/fireball.png#size=18x18)");

MutableComponent loop = PiTexts.markup(
        "[gif](example:textures/gui/spell/fireball_loop.png#size=20x10;frames=8;ticks=2;loop=false)");
```

图片和动图节点是自定义 `ComponentContents`。它们仍然在同一个 component 树里，但原版 serializer 不认识这些内容。发送到原版 JSON、网络、日志或其他 vanilla-only 路径前，先转 fallback：

```java
MutableComponent safe = PiTextComponents.vanillaFallback(icon);
```

GUI 中可以直接绘制可视节点：

```java
PiVisualTextRenderers.drawLine(graphics, font, icon, x, y, 0xFFFFFF, false, gameTime);
```

自定义图标、徽章、进度块时，继承 `PiVisualTextContents` 并注册 renderer：

```java
PiVisualTextRenderers.global().register(
        MY_BADGE_TYPE,
        MyBadgeContents.class,
        MyBadgeRenderer::draw);
```

## 自定义行内规则

行内规则属于一个 scope。把它放在使用它的 tooltip、screen 或 guide page 附近。

```java
PiTextMarkupScope scope = PiTextMarkupScope.builder("example")
        .inline("school", (inline, localScope) -> {
            ResourceLocation schoolId = localScope.resolveResource(inline.target());
            SpellSchool school = SpellSchools.REGISTRY.getValue(schoolId);
            if (school == null) {
                return Component.literal("unknown school").withStyle(ChatFormatting.RED);
            }
            return school.displayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE);
        })
        .build();

MutableComponent line = PiTexts.markup("School: [school](arcane)", scope);
```

规则可以返回普通文本、翻译文本、hover/click 文本、图片、动图，或者任何自定义 `PiVisualTextContents`。

## 替换 Parser

默认 parser 覆盖普通运行时用法。测试工具、书本编辑器或项目自己的预览界面需要特殊语法时，可以只对一次调用传入别的后端：

```java
MutableComponent preview = PiTexts.markup(source, scope, new MyStrictPreviewParser());
```

普通 tooltip 优先使用局部 `PiTextMarkupScope` 扩展规则。只有语法本身必须改变时，才替换 parser。
