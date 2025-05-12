package org.pickaid.pibrary.content.key.combo;

import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.network.PibraryNetworkHandler;
import org.pickaid.pibrary.network.key.ComboPacket;

import java.util.*;

public class ComboSystem {
    private static ComboSystem INSTANCE;
    private final Map<ResourceLocation, ComboDefinition> registeredCombos = new HashMap<>();
    private final LinkedList<ComboInput> currentSequence = new LinkedList<>();
    private final long comboTimeWindow;

    private ComboSystem(long comboTimeWindow) {
        this.comboTimeWindow = comboTimeWindow;
    }

    public static ComboSystem getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComboSystem(1000);
        }
        return INSTANCE;
    }

    public void registerCombo(ComboDefinition comboDefinition) {
        registeredCombos.put(comboDefinition.getId(), comboDefinition);
    }

    public void recordKeyInput(ResourceLocation keyId, long timestamp, KeyData.KeyState keyState) {
        cleanExpiredInputs(timestamp);
        currentSequence.add(new ComboInput(keyId, timestamp, keyState));
        checkForCombos();
    }

    private void cleanExpiredInputs(long currentTime) {
        while (!currentSequence.isEmpty() &&
                (currentTime - currentSequence.getFirst().timestamp) > comboTimeWindow) {
            currentSequence.removeFirst();
        }
    }

    private void checkForCombos() {
        for (ComboDefinition combo : registeredCombos.values()) {
            if (combo.matches(currentSequence)) {
                triggerCombo(combo);
                PibraryNetworkHandler.HANDLER.toServer(new ComboPacket(combo.getId()));
                currentSequence.clear();
                return;
            }
        }
    }

    private void triggerCombo(ComboDefinition combo) {
        combo.execute();
    }

    public void resetCombo() {
        currentSequence.clear();
    }

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