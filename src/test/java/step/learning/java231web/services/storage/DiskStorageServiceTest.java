package step.learning.java231web.services.storage;

import org.apache.commons.fileupload2.core.FileItem;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DiskStorageServiceTest {

    @Test
    public void testSaveAndGetStream() throws Exception {
        DiskStorageService storageService = new DiskStorageService();

        String testContent = "Тестовий вміст файлу";
        byte[] contentBytes = testContent.getBytes();

        FileItem mockItem = (FileItem) Proxy.newProxyInstance(
                FileItem.class.getClassLoader(),
                new Class<?>[]{FileItem.class},
                (proxy, method, args) -> {
                    if ("getName".equals(method.getName())) {
                        return "тестовий_документ_їжак.txt";
                    }
                    if ("write".equals(method.getName())) {
                        Path dest = (Path) args[0];
                        Files.write(dest, contentBytes);
                        return null;
                    }
                    if ("getInputStream".equals(method.getName())) {
                        return new ByteArrayInputStream(contentBytes);
                    }
                    return null;
                }
        );

        String savedName = storageService.save(mockItem);
        assertNotNull(savedName);
        assertTrue(savedName.endsWith(".txt"));

        try (InputStream in = storageService.getStream(savedName)) {
            assertNotNull(in);
            byte[] readBytes = in.readAllBytes();
            assertEquals(testContent, new String(readBytes));
        }

        // Clean up
        File savedFile = new File("C:/storage/Java231/" + savedName);
        if (savedFile.exists()) {
            savedFile.delete();
        }
    }

    @Test
    public void testSaveWithoutExtensionThrowsException() {
        DiskStorageService storageService = new DiskStorageService();

        FileItem mockItem = (FileItem) Proxy.newProxyInstance(
                FileItem.class.getClassLoader(),
                new Class<?>[]{FileItem.class},
                (proxy, method, args) -> {
                    if ("getName".equals(method.getName())) {
                        return "fileWithoutExtension";
                    }
                    return null;
                }
        );

        assertThrows(Exception.class, () -> storageService.save(mockItem));
    }
}
