package org.pickaid.pibrary.api.text.component;

import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.pickaid.pibrary.api.text.PiTexts;

/**
 * CommonMark-backed implementation for {@link PiTextMarkup}.
 *
 * <p>The parser handles normal Markdown structure and delegates project-specific
 * links/images to {@link PiTextMarkupScope}. That keeps the grammar reliable
 * while still letting a spell tooltip, manual page, or screen define local
 * markers such as {@code [cost](example:fireball)}.</p>
 */
public final class PiCommonmarkTextMarkupParser implements PiTextMarkupParser {
    private static final PiCommonmarkTextMarkupParser DEFAULT = new PiCommonmarkTextMarkupParser(Parser.builder().build());

    private final Parser parser;

    public PiCommonmarkTextMarkupParser(Parser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public static PiCommonmarkTextMarkupParser defaultParser() {
        return DEFAULT;
    }

    @Override
    public MutableComponent parse(String markup, PiTextMarkupScope scope) {
        Objects.requireNonNull(scope, "scope");
        Document document = (Document) parser.parse(normalizeEscapedNewlines(Objects.requireNonNull(markup, "markup")));
        MutableComponent root = PiTexts.literal("");
        renderDocument(document, scope, root);
        return root;
    }

    private static void renderDocument(Document document, PiTextMarkupScope scope, MutableComponent out) {
        boolean needsLineBreak = false;
        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (needsLineBreak) {
                out.append("\n");
            }
            renderBlock(node, scope, out);
            needsLineBreak = true;
        }
    }

    private static void renderBlock(Node node, PiTextMarkupScope scope, MutableComponent out) {
        if (node instanceof Paragraph || node instanceof Heading) {
            renderChildren(node, scope, out, Style.EMPTY);
            return;
        }
        if (node instanceof ListItem) {
            out.append("- ");
            renderChildren(node, scope, out, Style.EMPTY);
            return;
        }
        if (node instanceof BulletList bulletList) {
            for (Node child = bulletList.getFirstChild(); child != null; child = child.getNext()) {
                if (child != bulletList.getFirstChild()) {
                    out.append("\n");
                }
                out.append("- ");
                renderChildren(child, scope, out, Style.EMPTY);
            }
            return;
        }
        if (node instanceof OrderedList orderedList) {
            Integer markerStart = orderedList.getMarkerStartNumber();
            int number = markerStart == null ? 1 : markerStart;
            for (Node child = orderedList.getFirstChild(); child != null; child = child.getNext()) {
                if (child != orderedList.getFirstChild()) {
                    out.append("\n");
                }
                out.append(Integer.toString(number++));
                out.append(". ");
                renderChildren(child, scope, out, Style.EMPTY);
            }
            return;
        }
        renderChildren(node, scope, out, Style.EMPTY);
    }

    private static void renderChildren(Node parent, PiTextMarkupScope scope, MutableComponent out, Style style) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            renderInline(child, scope, out, style);
        }
    }

    private static void renderInline(Node node, PiTextMarkupScope scope, MutableComponent out, Style style) {
        if (node instanceof Text text) {
            appendStyled(out, PiTexts.literal(text.getLiteral()), style);
            return;
        }
        if (node instanceof Code code) {
            appendStyled(out, PiTexts.literal(code.getLiteral()), style.withColor(ChatFormatting.GRAY));
            return;
        }
        if (node instanceof SoftLineBreak || node instanceof HardLineBreak) {
            appendStyled(out, PiTexts.literal("\n"), style);
            return;
        }
        if (node instanceof StrongEmphasis) {
            renderChildren(node, scope, out, style.withBold(true));
            return;
        }
        if (node instanceof Emphasis) {
            renderChildren(node, scope, out, style.withItalic(true));
            return;
        }
        if (node instanceof Image image) {
            renderImage(image, scope, out, style);
            return;
        }
        if (node instanceof Link link) {
            renderLink(link, scope, out, style);
            return;
        }
        if (node instanceof HtmlInline htmlInline) {
            appendStyled(out, PiTexts.literal(htmlInline.getLiteral()), style);
            return;
        }
        renderChildren(node, scope, out, style);
    }

    private static void renderLink(Link link, PiTextMarkupScope scope, MutableComponent out, Style style) {
        String label = plainText(link).trim();
        if (label.isEmpty()) {
            label = link.getDestination();
        }
        PiTextInline inline = new PiTextInline(label, link.getDestination());
        MutableComponent custom = scope.createInline(inline).orElse(null);
        if (custom != null) {
            appendStyled(out, custom, style);
            return;
        }
        Style linkStyle = style.withUnderlined(true);
        if (isWebUrl(link.getDestination())) {
            linkStyle = linkStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.getDestination()));
        }
        renderChildren(link, scope, out, linkStyle);
    }

    private static void renderImage(Image image, PiTextMarkupScope scope, MutableComponent out, Style style) {
        String label = plainText(image).trim();
        if (label.isEmpty()) {
            label = "image";
        }
        MutableComponent custom = scope.createInline(new PiTextInline(label, image.getDestination()))
                .or(() -> scope.createInline(new PiTextInline("image", image.getDestination())))
                .orElse(null);
        if (custom != null) {
            appendStyled(out, custom, style);
            return;
        }
        appendStyled(out, PiTexts.literal("[" + label + "]"), style);
    }

    private static void appendStyled(MutableComponent out, MutableComponent component, Style parentStyle) {
        MutableComponent copy = component.copy();
        copy.withStyle(copy.getStyle().applyTo(parentStyle));
        out.append(copy);
    }

    private static String plainText(Node node) {
        StringBuilder result = new StringBuilder();
        collectPlainText(node, result);
        return result.toString();
    }

    private static void collectPlainText(Node node, StringBuilder out) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Text text) {
                out.append(text.getLiteral());
            } else if (child instanceof Code code) {
                out.append(code.getLiteral());
            } else if (child instanceof SoftLineBreak || child instanceof HardLineBreak) {
                out.append('\n');
            } else {
                collectPlainText(child, out);
            }
        }
    }

    private static String normalizeEscapedNewlines(String markup) {
        StringBuilder result = new StringBuilder(markup.length());
        for (int i = 0; i < markup.length(); i++) {
            char current = markup.charAt(i);
            if (current == '\\' && i + 1 < markup.length() && markup.charAt(i + 1) == 'n') {
                result.append('\n');
                i++;
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }

    private static boolean isWebUrl(String target) {
        String lower = target.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://");
    }
}
