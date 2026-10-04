package step.learning.java231web.services.storage;

import com.google.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.apache.commons.fileupload2.core.FileItem;

/**
 *
 * @author Lector
 */
@Singleton
public class DiskStorageService implements IStorageService {
    private final String storagePath = "C:/storage/Java231/";

    public DiskStorageService() {
        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public String save(FileItem item) throws IOException {
        String name = item.getName();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex < 0) {
            throw new IOException("File not allowed without extension");
        }
        String ext = name.substring(dotIndex);
        String savedName;
        File file;
        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        do {
            savedName = UUID.randomUUID() + ext;
            file = new File(storagePath + savedName);
        } while(file.exists());
        item.write(file.toPath());
        return savedName;
    }

    @Override
    public InputStream getStream(String filename) throws IOException {
        File file = new File(storagePath + filename);
        if(file.canRead()) {
            return new FileInputStream(file);
        }
        else throw new IOException("File not found: " + filename);
    }
}
