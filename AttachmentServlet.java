import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import io.lettuce.core.api.sync.RedisCommands;

@WebServlet("/AttachmentServlet")
public class AttachmentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("came to attachment");
        String attachmentId = request.getParameter("attachmentId");
        if (attachmentId == null || attachmentId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Attachment ID is required.");
            return;
        }

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String attachmentData = redisCommands.hget("attachments", attachmentId);
        
        if (attachmentData == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            return;
        }

        String[] data = attachmentData.split("\\|");
        if (data.length < 3) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid file metadata.");
            return;
        }

        String filePath = data[0];
        String fileName = data[1];
        String mimeType = data[2];

        Path file = Paths.get(filePath);
        if (Files.exists(file)) {
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

            try (InputStream in = Files.newInputStream(file); OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }
}
