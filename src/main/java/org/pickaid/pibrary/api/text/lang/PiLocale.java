package org.pickaid.pibrary.api.text.lang;

import java.util.Locale;
import java.util.Objects;

/**
 * Minecraft language file locale code.
 *
 * <p>This is intentionally not an enum. Mods, resource packs, and future
 * platforms can use locale codes beyond the small set Pibrary happens to know.</p>
 */
public final class PiLocale implements Comparable<PiLocale> {
    public static final PiLocale EN_US = of("en_us");
    public static final PiLocale ZH_CN = of("zh_cn");

    private final String code;

    private PiLocale(String code) {
        this.code = normalize(code);
    }

    public static PiLocale of(String code) {
        return new PiLocale(code);
    }

    public String code() {
        return code;
    }

    static String normalize(String code) {
        String result = Objects.requireNonNull(code, "code").trim().toLowerCase(Locale.ROOT).replace('-', '_');
        if (result.isEmpty()) {
            throw new IllegalArgumentException("locale code must not be blank");
        }
        return result;
    }

    @Override
    public int compareTo(PiLocale other) {
        return code.compareTo(other.code);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof PiLocale locale && code.equals(locale.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
