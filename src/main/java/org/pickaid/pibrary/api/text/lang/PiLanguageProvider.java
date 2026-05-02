package org.pickaid.pibrary.api.text.lang;

import java.util.Map;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Forge language provider backed by {@link PiLanguageBundle}.
 */
public abstract class PiLanguageProvider extends LanguageProvider {
    private final String modid;
    private final PiLocale locale;

    protected PiLanguageProvider(PackOutput output, String modid, PiLocale locale) {
        super(output, modid, locale.code());
        this.modid = PiLanguageEntry.requireKey(modid);
        this.locale = locale;
    }

    protected PiLanguageProvider(PackOutput output, String modid, String locale) {
        this(output, modid, PiLocale.of(locale));
    }

    protected abstract void add(PiLanguageBundle.Builder<?> translations);

    @Override
    protected final void addTranslations() {
        PiLanguageBundle.RootBuilder builder = PiLanguages.bundle(modid);
        add(builder);
        for (Map.Entry<String, String> entry : builder.build().entries(locale).entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }
}
