# Text Markup

`PiTexts.markup(...)` is for text that still needs to be a Minecraft
`Component`, but is too awkward to write as a long chain of `append(...)` calls:
spell tooltips, guide pages, screen labels, JEI descriptions, and debug panels.

Pibrary uses `org.commonmark:commonmark` as the parser. CommonMark owns the
normal Markdown grammar; Pibrary only translates the parsed tree into Minecraft
components and handles game-specific inline nodes through `PiTextMarkupScope`.

## Why CommonMark

The parser choice is deliberately conservative.

- `commonmark-java` is Java-native, small, and stable enough to embed with jarJar.
- `flexmark-java` is more feature-rich, but it is heavier and pulls the text API toward a document-processing toolkit.
- `markdown-it` and `remark/unified` have excellent plugin ecosystems, but they are JavaScript-first and fit build tooling better than Forge runtime code.

The result is a practical split: CommonMark handles real Markdown parsing, while
Pibrary keeps the game-facing extension layer small and typed.

## Basic Markup

```java
MutableComponent line = PiTexts.markup(
        "**Fireball** ![image](example:textures/gui/spell/fireball.png#size=18x18)\\n"
                + "Cost: [mana](example:fireball)");
```

Supported by the default renderer:

- `**text**` becomes bold text.
- `*text*` becomes italic text.
- `` `text` `` becomes gray inline code text.
- `\\n`, a real line break, or a Markdown hard break becomes a real newline.
- `[label](https://example.invalid)` becomes underlined clickable text.
- `[label](target)` can become project-specific content through `PiTextMarkupScope`.
- `![image](target)` can become a visual component through the default image handlers.

## Translation Key And Default Text

In normal gameplay code, provide both a translation key and a default string.
The key goes into language files; the default keeps tests, previews, and missing
translations readable.

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

The order is fixed:

1. Resolve the current language text by key.
2. Use the default string if the key is missing.
3. Replace named arguments such as `{name}`, `{icon}`, and `{spell}`.
4. Parse the result with CommonMark and `PiTextMarkupScope`.

`PiTextArgs.text(...)` and `number(...)` are treated as plain text inside markup,
so player names or data containing `*` and `[` do not accidentally become
Markdown. `resource(...)` is for link targets such as `![image]({icon})`.
`component(...)` inserts an existing Component back into the parsed tree without
flattening its color, hover event, or click event into a plain string.

Language datagen can use the same key/default pair. Placeholder sets must match
between locale values, so a missing `{mana}` fails when the bundle is built:

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

## Spell Tooltip Example

Use a local scope when the text depends on gameplay data. The markup should not
contain a fake value such as `mana:8`; it should name the thing being described,
then let the scope read the current data.

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

The same idea works for guide pages and JEI descriptions: load the data first,
then let local inline handlers render the values.

## Images And Animated Images

The default scope registers `image`, `img`, `gif`, `anim`, and `animation`.

```java
MutableComponent icon = PiTexts.markup(
        "Icon: ![image](example:textures/gui/spell/fireball.png#size=18x18)");

MutableComponent loop = PiTexts.markup(
        "[gif](example:textures/gui/spell/fireball_loop.png#size=20x10;frames=8;ticks=2;loop=false)");
```

Image and animated-image nodes are custom `ComponentContents`. They stay inside
the same component tree, but vanilla serializers do not know how to write them.
Convert to fallback text before sending through a vanilla-only path:

```java
MutableComponent safe = PiTextComponents.vanillaFallback(icon);
```

GUI code can draw visual nodes directly:

```java
PiVisualTextRenderers.drawLine(graphics, font, icon, x, y, 0xFFFFFF, false, gameTime);
```

Custom visuals should extend `PiVisualTextContents`, then register a renderer:

```java
PiVisualTextRenderers.global().register(
        MY_BADGE_TYPE,
        MyBadgeContents.class,
        MyBadgeRenderer::draw);
```

## Custom Inline Rules

Inline rules belong to a scope. Keep them close to the screen, tooltip, or guide
page using them.

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

Rules can return normal text, translatable text, hover/click components, images,
animated images, or any custom `PiVisualTextContents` implementation.

## Parser Replacement

The default parser should cover normal runtime use. A test tool, book editor, or
project-specific preview screen can pass a different backend for one call:

```java
MutableComponent preview = PiTexts.markup(source, scope, new MyStrictPreviewParser());
```

Prefer local `PiTextMarkupScope` rules for ordinary tooltips. Replace the parser
only when the grammar itself must change.
