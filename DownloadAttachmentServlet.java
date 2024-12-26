import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/downloadAttachment")
public class DownloadAttachmentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String attachmentId = request.getParameter("attachmentId");
        
        if (attachmentId == null || attachmentId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Attachment ID is missing");
            return;
        }
        
        String filePath = "local/dfs/chat/attachments/" + attachmentId; 
        
        File file = new File(filePath);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }
        
        response.setContentType(getServletContext().getMimeType(filePath));
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
