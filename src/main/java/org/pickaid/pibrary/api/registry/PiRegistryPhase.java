package org.pickaid.pibrary.api.registry;

/**
 * Legacy internal request model kept only for bridge migration.
 * New public registry entry points live under {@code api.registrate.registry}.
 *
 * Phases where a registry contribution may be applied.
 */
public enum PiRegistryPhase {
    STATIC_BOOTSTRAP,
    MOD_EVENT_REGISTRATION,
    DATA_GENERATION
}
