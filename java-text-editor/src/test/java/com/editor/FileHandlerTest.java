package com.editor;

import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class FileHandlerTest {
    private final FileHandler handler = new FileHandler();
    private File tempFile;

    @BeforeEach
    void setup() throws IOException {
        tempFile = File.createTempFile("test", ".txt");
    }

    @AfterEach
    void cleanup() {
        if (tempFile.exists()) tempFile.delete();
    }

    @Test
    void testWriteAndReadFile() {
        String testContent = "Hello, world!\nSecond line.";
        handler.writeFile(tempFile, testContent);
        String result = handler.readFile(tempFile);
        assertEquals(testContent + "\n", result);
    }
}
