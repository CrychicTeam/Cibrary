package org.pickaid.pibrary.content.key.combo;

import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.List;
import java.util.function.Consumer;

public interface ComboDefinition {
    ResourceLocation getId();
    boolean matches(List<ComboSystem.ComboInput> inputSequence);
    void execute();

    class SequenceCombo implements ComboDefinition {
        private final ResourceLocation id;
        private final ResourceLocation[] keySequence;
        private final Consumer<ResourceLocation> action;

        public SequenceCombo(ResourceLocation id, ResourceLocation[] keySequence,
                             Consumer<ResourceLocation> action) {
            this.id = id;
            this.keySequence = keySequence;
            this.action = action;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public boolean matches(List<ComboSystem.ComboInput> inputSequence) {
            if (inputSequence.size() < keySequence.length) {
                return false;
            }

            int startIndex = inputSequence.size() - keySequence.length;
            for (int i = 0; i < keySequence.length; i++) {
                ComboSystem.ComboInput input = inputSequence.get(startIndex + i);
                if (!input.keyId.equals(keySequence[i]) ||
                        input.keyState != KeyData.KeyState.PRESSED) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void execute() {
            action.accept(id);
        }
    }

    class StateCombo implements ComboDefinition {
        private final ResourceLocation id;
        private final ComboRequirement[] requirements;
        private final Consumer<ResourceLocation> action;

        public StateCombo(ResourceLocation id, ComboRequirement[] requirements,
                          Consumer<ResourceLocation> action) {
            this.id = id;
            this.requirements = requirements;
            this.action = action;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public boolean matches(List<ComboSystem.ComboInput> inputSequence) {
            if (inputSequence.size() < requirements.length) {
                return false;
            }
            for (int i = 0; i < requirements.length; i++) {
                boolean found = false;
                for (int j = inputSequence.size() - 1; j >= 0; j--) {
                    ComboSystem.ComboInput input = inputSequence.get(j);
                    if (input.keyId.equals(requirements[i].keyId)) {
                        if (input.keyState != requirements[i].requiredState) {
                            return false;
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }

            return true;
        }

        @Override
        public void execute() {
            action.accept(id);
        }
    }

    class ComboRequirement {
        public final ResourceLocation keyId;
        public final KeyData.KeyState requiredState;

        public ComboRequirement(ResourceLocation keyId, KeyData.KeyState requiredState) {
            this.keyId = keyId;
            this.requiredState = requiredState;
        }
    }
}