package org.pickaid.pibrary.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import org.pickaid.pibrary.api.registry.ArmorSetRegistry;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.kubejs.contents.armorset.CustomArmorSet;

public class PiJSPlugin extends KubeJSPlugin {
    public static RegistryInfo<ArmorSet> ARMOR_SET = RegistryInfo.of(ArmorSetRegistry.ARMOR_SET_REGISTRY_KEY, ArmorSet.class);

    @Override
    public void registerEvents() {
        PiEventS.GROUP.register();
    }

    @Override
    public void init() {
        ARMOR_SET.addType("basic", CustomArmorSet.Builder.class, CustomArmorSet.Builder::new);
    }
}
