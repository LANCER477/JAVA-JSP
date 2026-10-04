package step.learning.java231web.services.form;

import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;

@Singleton
public class MixedFormParseService implements IFormParseService {
    private static final int MEMORY_THRESHOLD = 10 * 1024 * 1024; // 10 MB threshold
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;   // 10 MB max single file size
    private static final long MAX_REQUEST_SIZE = 20 * 1024 * 1024; // 20 MB max total request size

    @Override
    public FormParseResult parse(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Map<String, FileItem> files = new HashMap<>();

        if (JakartaServletDiskFileUpload.isMultipartContent(request)) {
            try {
                DiskFileItemFactory factory = DiskFileItemFactory.builder()
                        .setBufferSize(MEMORY_THRESHOLD)
                        .get();

                JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload(factory);
                upload.setFileSizeMax(MAX_FILE_SIZE);
                upload.setSizeMax(MAX_REQUEST_SIZE);

                List<DiskFileItem> items = upload.parseRequest(request);
                for (FileItem item : items) {
                    if (item.isFormField()) {
                        fields.put(item.getFieldName(), item.getString(StandardCharsets.UTF_8));
                    } else {
                        if (item.getName() != null && !item.getName().trim().isEmpty() && item.getSize() > 0) {
                            files.put(item.getFieldName(), item);
                        }
                    }
                }
            } catch (FileUploadSizeException ex) {
                request.setAttribute("formError", "Файл перевищує дозволений ліміт розміру (" + (MAX_FILE_SIZE / (1024 * 1024)) + " МБ)!");
                throw new RuntimeException("Розмір файлу перевищує дозволений ліміт: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                request.setAttribute("formError", "Помилка обробки форми: " + ex.getMessage());
                throw new RuntimeException("Помилка парсингу multipart-форми: " + ex.getMessage(), ex);
            }
        } else {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                fields.put(entry.getKey(), entry.getValue() != null && entry.getValue().length > 0 ? entry.getValue()[0] : "");
            }
        }

        return new FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return fields;
            }

            @Override
            public Map<String, FileItem> getFiles() {
                return files;
            }
        };
    }
}
