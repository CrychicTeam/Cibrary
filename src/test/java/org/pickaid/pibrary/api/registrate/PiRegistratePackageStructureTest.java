package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PiRegistratePackageStructureTest {
    @Test
    void sharedTransformClassesStaySmallEnoughToRemainBoringHelpers() throws IOException {
        assertLineCountAtMost("PiEntryTransforms.java", 120);
        assertLineCountAtMost("PiBlockTransforms.java", 240);
        assertLineCountAtMost("PiItemTransforms.java", 180);
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
