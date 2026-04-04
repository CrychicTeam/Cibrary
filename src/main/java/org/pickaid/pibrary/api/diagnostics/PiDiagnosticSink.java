package org.pickaid.pibrary.api.diagnostics;

public interface PiDiagnosticSink {
    void publish(PiDiagnosticEntry entry);
}
