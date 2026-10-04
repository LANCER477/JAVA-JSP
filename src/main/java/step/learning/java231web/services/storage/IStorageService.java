package step.learning.java231web.services.storage;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.fileupload2.core.FileItem;

public interface IStorageService {
    String save(FileItem item) throws IOException;
    InputStream getStream(String filename) throws IOException;
}
