package org.crychicteam.cibrary.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import org.crychicteam.cibrary.api.registry.ArmorSetRegistry;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.kubejs.contents.armorset.CustomArmorSet;

public class CiJSPlugin extends KubeJSPlugin {
    public static RegistryInfo<ArmorSet> ARMOR_SET = RegistryInfo.of(ArmorSetRegistry.ARMOR_SET_REGISTRY_KEY, ArmorSet.class);

    @Override
    public void registerEvents() {
        CiEventS.GROUP.register();
    }

    @Override
    public void init() {
        ARMOR_SET.addType("basic", CustomArmorSet.Builder.class, CustomArmorSet.Builder::new);
    }
}
