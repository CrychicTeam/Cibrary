package org.pickaid.pibrary.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class ComboTriggeredEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation comboId;
    
    public ComboTriggeredEvent(ServerPlayer player, ResourceLocation comboId) {
        this.player = player;
        this.comboId = comboId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getComboId() {
        return comboId;
    }
} 