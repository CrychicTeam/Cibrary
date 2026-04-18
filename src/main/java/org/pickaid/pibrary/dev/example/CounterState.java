package org.pickaid.pibrary.dev.example;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.piserializekit.api.schema.PiField;
import org.pickaid.piserializekit.api.schema.PiSyncModel;
import org.pickaid.piserializekit.api.schema.PiSyncScope;

/**
 * Sample generated state model shared by the block entity and living-service examples.
 */
@PiSyncModel(id = "pibrary:counter_state", version = 1)
public final class CounterState {
    @PiField(id = "count", sync = PiSyncScope.CHUNK, persist = true)
    public int count;

    @PiField(id = "energy", sync = PiSyncScope.OWNER, persist = true)
    public int energy;

    @PiField(id = "active", sync = PiSyncScope.CHUNK, persist = true)
    public boolean active = true;

    @PiField(id = "owner_name", sync = PiSyncScope.OWNER, persist = true)
    public String ownerName = "fallback";

    @PiField(id = "run_id", sync = PiSyncScope.TRACKING, persist = true)
    public UUID runId = new UUID(0L, 1L);

    @PiField(id = "trial", sync = PiSyncScope.TRACKING, persist = true)
    public ResourceLocation trial = ResourceLocation.parse("pibrary:counter");

    @PiField(id = "session_glow", sync = PiSyncScope.CHUNK, persist = false)
    public int sessionGlow;
}
