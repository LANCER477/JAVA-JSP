package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import step.learning.java231web.services.storage.IStorageService;

@Singleton
public class StorageServlet extends HttpServlet {
    private final IStorageService storageService;

    @Inject
    public StorageServlet(IStorageService storageService) {
        this.storageService = storageService;
    }

    public StorageServlet() {
        this(null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

        jakarta.servlet.ServletContext ctx = null;
        try {
            ctx = req.getServletContext();
        } catch (Exception ignored) {
        }

        // 1. Спробуємо завантажити зі storageService
        if (storageService != null) {
            try {
                InputStream in = storageService.getStream(filename);
                if (in != null) {
                    sendImageStream(resp, filename, in, ctx);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Спробуємо завантажити зі стандартної директорії C:/storage/Java231/
        File diskFile = new File("C:/storage/Java231/" + filename);
        if (diskFile.exists() && diskFile.isFile()) {
            try (InputStream in = new FileInputStream(diskFile)) {
                sendImageStream(resp, filename, in, ctx);
                return;
            } catch (Exception ignored) {
            }
        }
        // 3. Спробуємо завантажити з ресурсів веб-додатку (наприклад /img/{filename})
        if (ctx != null) {
            try {
                InputStream resIn = ctx.getResourceAsStream("/img/" + filename);
                if (resIn != null) {
                    sendImageStream(resp, filename, resIn, ctx);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void sendImageStream(HttpServletResponse resp, String filename, InputStream in, jakarta.servlet.ServletContext ctx) throws IOException {
        String mimeType = ctx != null ? ctx.getMimeType(filename) : null;
        if (mimeType == null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".webp")) {
                mimeType = "image/webp";
            } else if (lower.endsWith(".png")) {
                mimeType = "image/png";
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (lower.endsWith(".gif")) {
                mimeType = "image/gif";
            } else {
                mimeType = "application/octet-stream";
            }
        }
        resp.setContentType(mimeType);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        try (in; OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
