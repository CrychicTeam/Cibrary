package org.pickaid.pibrary.content.key.combo;

import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.List;
import java.util.function.Consumer;

/**
 * Defines a combo pattern and its associated action.
 */
public interface ComboDefinition {
    /**
     * Gets the unique identifier for this combo.
     */
    ResourceLocation getId();

    /**
     * Checks if the given input sequence matches this combo.
     */
    boolean matches(List<ComboSystem.ComboInput> inputSequence);

    /**
     * Executes the action associated with this combo.
     */
    void execute();

    /**
     * Basic implementation for sequence-based combos.
     */
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

            // Check last N inputs match our sequence
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

    /**
     * Combo that requires specific key states (e.g., charging + press).
     */
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

            // Build map of latest state for each key
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

    /**
     * Defines a requirement for a key in a combo.
     */
    class ComboRequirement {
        public final ResourceLocation keyId;
        public final KeyData.KeyState requiredState;

        public ComboRequirement(ResourceLocation keyId, KeyData.KeyState requiredState) {
            this.keyId = keyId;
            this.requiredState = requiredState;
        }
    }
}