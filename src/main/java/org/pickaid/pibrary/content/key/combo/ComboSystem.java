package org.pickaid.pibrary.content.key.combo;

import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.*;

/**
 * ComboSystem manages combinations of key presses to trigger special actions.
 * <p>
 * Features:
 * <p>
 * 1. Sequence Tracking - Records sequence of keys pressed within time window
 * <p>
 * 2. Pattern Recognition - Matches input sequences against registered combo patterns
 * <p>
 * 3. Event Triggering - Fires events when combos are successfully executed
 */
public class ComboSystem {
    // Singleton instance
    private static ComboSystem INSTANCE;

    // Maps combo IDs to their definitions
    private final Map<ResourceLocation, ComboDefinition> registeredCombos = new HashMap<>();

    // Tracks current input sequence
    private final LinkedList<ComboInput> currentSequence = new LinkedList<>();

    // Maximum time between inputs to be considered part of the same combo
    private final long comboTimeWindow;

    private ComboSystem(long comboTimeWindow) {
        this.comboTimeWindow = comboTimeWindow;
    }

    public static ComboSystem getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComboSystem(300);
        }
        return INSTANCE;
    }

    /**
     * Registers a combo definition with the system.
     *
     * @param comboDefinition The combo to register
     */
    public void registerCombo(ComboDefinition comboDefinition) {
        registeredCombos.put(comboDefinition.getId(), comboDefinition);
        Pibrary.LOGGER.info("Registered combo: {}", comboDefinition.getId());
    }

    /**
     * Records a key press for combo detection.
     *
     * @param keyId The key that was pressed
     * @param timestamp The time of the key press
     * @param keyState The state of the key (PRESSED, RELEASED, etc.)
     */
    public void recordKeyInput(ResourceLocation keyId, long timestamp, KeyData.KeyState keyState) {
        cleanExpiredInputs(timestamp);
        currentSequence.add(new ComboInput(keyId, timestamp, keyState));
        checkForCombos();
    }

    /**
     * Removes inputs that are too old to be part of current combo.
     */
    private void cleanExpiredInputs(long currentTime) {
        while (!currentSequence.isEmpty() &&
                (currentTime - currentSequence.getFirst().timestamp) > comboTimeWindow) {
            currentSequence.removeFirst();
        }
    }

    /**
     * Checks if current sequence matches any registered combo.
     */
    private void checkForCombos() {
        for (ComboDefinition combo : registeredCombos.values()) {
            if (combo.matches(currentSequence)) {
                triggerCombo(combo);
                currentSequence.clear();
                return;
            }
        }
    }

    /**
     * Triggers actions associated with a completed combo.
     */
    private void triggerCombo(ComboDefinition combo) {
        combo.execute();
        Pibrary.LOGGER.info("Executed combo: {}", combo.getId());
    }

    /**
     * Clears current combo sequence.
     */
    public void resetCombo() {
        currentSequence.clear();
    }

    /**
     * Get the current input sequence for debugging.
     */
    public List<ComboInput> getCurrentSequence() {
        return Collections.unmodifiableList(currentSequence);
    }

    /**
     * Represents a single input in a combo sequence.
     */
    public static class ComboInput {
        public final ResourceLocation keyId;
        public final long timestamp;
        public final KeyData.KeyState keyState;

        public ComboInput(ResourceLocation keyId, long timestamp, KeyData.KeyState keyState) {
            this.keyId = keyId;
            this.timestamp = timestamp;
            this.keyState = keyState;
        }
    }
}