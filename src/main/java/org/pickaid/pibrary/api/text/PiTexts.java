package org.pickaid.pibrary.api.text;

import java.util.Iterator;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Small helpers around vanilla {@link Component}.
 *
 * <p>This class deliberately returns Minecraft components instead of wrapping
 * them. Callers can pass the result directly to item tooltips, screens, chat,
 * JEI adapters, and vanilla rendering code.</p>
 */
public final class PiTexts {
    private PiTexts() {
    }

    public static String tooltipKey(String modid, String path) {
        return key("tooltip", modid, path);
    }

    public static String titleKey(String modid, String path) {
        return key("title", modid, path);
    }

    public static String messageKey(String modid, String path) {
        return key("message", modid, path);
    }

    public static MutableComponent tooltip(String modid, String path, Object... args) {
        return translatable(tooltipKey(modid, path), args);
    }

    public static MutableComponent title(String modid, String path, Object... args) {
        return translatable(titleKey(modid, path), args);
    }

    public static MutableComponent message(String modid, String path, Object... args) {
        return translatable(messageKey(modid, path), args);
    }

    public static MutableComponent literal(String text) {
        return Component.literal(Objects.requireNonNull(text, "text"));
    }

    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(Objects.requireNonNull(key, "key"), args);
    }

    public static MutableComponent join(Component delimiter, Iterable<? extends Component> parts) {
        Objects.requireNonNull(delimiter, "delimiter");
        Objects.requireNonNull(parts, "parts");
        Iterator<? extends Component> iterator = parts.iterator();
        MutableComponent joined = Component.empty();
        if (!iterator.hasNext()) {
            return joined;
        }
        joined.append(iterator.next().copy());
        while (iterator.hasNext()) {
            joined.append(delimiter.copy());
            joined.append(iterator.next().copy());
        }
        return joined;
    }

    public static MutableComponent copyable(String label, String value) {
        Objects.requireNonNull(value, "value");
        return literal(label).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal(value))));
    }

    private static String key(String prefix, String modid, String path) {
        String cleanedModid = Objects.requireNonNull(modid, "modid").trim();
        String cleanedPath = Objects.requireNonNull(path, "path").trim();
        if (cleanedModid.isEmpty()) {
            throw new IllegalArgumentException("modid must not be blank");
        }
        if (cleanedPath.isEmpty()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return prefix + "." + cleanedModid + "." + cleanedPath;
    }
}
