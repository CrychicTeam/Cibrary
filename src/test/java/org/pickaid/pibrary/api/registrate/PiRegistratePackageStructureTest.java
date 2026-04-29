package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PiRegistratePackageStructureTest {
    @Test
    void registrateBuilderClassesStaySmallEnoughToRemainBoringHelpers() throws IOException {
        assertLineCountAtMost("PiBlockBuilder.java", 340);
        assertLineCountAtMost("PiItemBuilder.java", 300);
        assertLineCountAtMost("PiBaseRegistrate.java", 240);
    }

    private static void assertLineCountAtMost(String fileName, int maxLines) throws IOException {
        Path path = Path.of(
                "src/main/java/org/pickaid/pibrary/api/registrate",
                fileName
        );
        long lines = Files.lines(path).count();
        assertTrue(lines <= maxLines, fileName + " has " + lines + " lines, expected <= " + maxLines);
    }
}
