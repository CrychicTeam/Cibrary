package org.pickaid.pibrary.dev.example;

import net.minecraft.world.entity.player.Player;
import org.pickaid.pibrary.api.service.PiLivingService;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServices;
import org.pickaid.pibrary.api.service.PiStatePlayerService;

@PiLivingService(namespace = "pibrary", path = "counter_player", state = CounterState.class)
public final class CounterPlayerService extends PiStatePlayerService<CounterState> {
    public CounterPlayerService(PiLivingServiceContext context) {
        super(context, CounterState.class);
    }

    public static CounterPlayerService get(Player player) {
        return PiLivingServices.require(player, CounterPlayerService.class);
    }

    public void increment() {
        updateState(state -> state.count++);
    }

    public void incrementEnergy() {
        gainEnergy(1);
    }

    public void gainEnergy(int value) {
        updateState(state -> state.energy += value);
    }

    public int energy() {
        return viewState().energy;
    }

    public int getCount() {
        return viewState().count;
    }

    public int dirtyCount() {
        return dirtySet().keys().size();
    }
}
