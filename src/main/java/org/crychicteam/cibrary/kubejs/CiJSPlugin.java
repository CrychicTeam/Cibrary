package org.crychicteam.cibrary.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

public class CiJSPlugin extends KubeJSPlugin {

    @Override
    public void registerEvents() {
        CiEventS.GROUP.register();
    }

    @Override
    public void init() {

    }
}
