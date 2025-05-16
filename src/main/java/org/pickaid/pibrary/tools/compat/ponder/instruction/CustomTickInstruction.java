package org.pickaid.pibrary.tools.compat.ponder.instruction;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.TickingInstruction;

public class CustomTickInstruction extends TickingInstruction {
    TickInstructionBuilder builder;

    public CustomTickInstruction(boolean blocking, int ticks) {
        super(blocking, ticks);
        this.builder = new TickInstructionBuilder();
    }

    public TickInstructionBuilder createBuilder() {
        return this.builder;
    }

    @Override
    protected void firstTick(PonderScene scene) {
        if (this.builder.firstTickFunction != null) {
            this.builder.firstTickFunction.apply(scene);
        }
    }

    @Override
    public void tick(PonderScene scene) {
        if (remainingTicks == totalTicks)
            firstTick(scene);
        if (this.builder.tickFunction!= null) {
            this.builder.tickFunction.apply(scene);
        }
        if (remainingTicks > 0)
            remainingTicks--;
    }

    @FunctionalInterface
    public interface TickFunction {
        void apply(PonderScene scene);
    }

    @FunctionalInterface
    public interface FirstTickFunction {
        void apply(PonderScene scene);
    }

    public static class TickInstructionBuilder {
        TickFunction tickFunction;
        FirstTickFunction firstTickFunction;

        public TickInstructionBuilder withTickFunction(TickFunction tickFunction) {
            this.tickFunction = tickFunction;
            return this;
        }

        public TickInstructionBuilder withFirstTickFunction(FirstTickFunction firstTickFunction) {
            this.firstTickFunction = firstTickFunction;
            return this;
        }
    }
}
