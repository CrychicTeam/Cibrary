package org.pickaid.pibrary.api.diagnostics;

/**
 * Consumer of diagnostic entries.
 */
public interface PiDiagnosticSink {
    /**
     * Publishes one diagnostic entry.
     *
     * @param entry diagnostic entry
     */
    void publish(PiDiagnosticEntry entry);
}
