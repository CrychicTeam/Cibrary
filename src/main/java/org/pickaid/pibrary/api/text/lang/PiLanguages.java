package org.pickaid.pibrary.api.text.lang;

/**
 * Entry points for language data generation helpers.
 */
public final class PiLanguages {
    private PiLanguages() {
    }

    public static PiLanguageBundle.RootBuilder bundle(String modid) {
        return new PiLanguageBundle.RootBuilder(modid);
    }

    public static PiLanguageEntry.Builder entry(String key) {
        return PiLanguageEntry.key(key);
    }
}
