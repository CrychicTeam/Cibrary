/**
 * Text helpers and rich text content contracts.
 *
 * <p>{@link org.pickaid.pibrary.api.text.PiTexts} stays close to vanilla
 * {@link net.minecraft.network.chat.Component}. Translation helpers can pair a
 * key with default text and named {@link org.pickaid.pibrary.api.text.PiTextArgs}.
 * Rich markup is parsed through a CommonMark backend, then converted into
 * Minecraft components. Rich visual content is represented as custom
 * {@link net.minecraft.network.chat.ComponentContents}, so it stays in the same
 * component tree as normal text and can still provide vanilla fallback text for
 * logs, narration, JSON, and network-safe paths.</p>
 */
package org.pickaid.pibrary.api.text;
