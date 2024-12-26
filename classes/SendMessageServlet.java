import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.*;
import com.google.gson.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.nio.file.*;
import java.util.UUID;

@MultipartConfig
public class SendMessageServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
		System.out.println("came to sendmsgservlet");

        String sender = (String) request.getSession().getAttribute("email");
        String recipient = request.getParameter("recipient");
        String text = request.getParameter("text");
        Part attachment = request.getPart("attachment");
				
		String messageId = UUID.randomUUID().toString();
		long timestamp = System.currentTimeMillis(); // in millisec
		System.out.println("came to one-one");

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String messageKey = "chat:" + sender + ":" + recipient;
        String reverseKey = "chat:" + recipient + ":" + sender;
		
		if(attachment != null && attachment.getSize() >  0) {
			String fileName = Paths.get(attachment.getSubmittedFileName()).getFileName().toString();
            String uploadPath = System.getProperty("user.dir") + "/local/dfs/chat/attachments/";
            File dir = new File(uploadPath);
			
			 if (!dir.exists()) {
                System.out.println("Directory does not exist. Creating directory...");
                dir.mkdirs();
            }
			
			File file = new File(uploadPath, fileName);
            System.out.println("Full file path: " + file.getAbsolutePath());

            try (InputStream input = attachment.getInputStream()) {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
			
            String mimeType = Files.probeContentType(file.toPath());
			System.out.println("mimeType" + mimeType);
			
			String attachmentId = UUID.randomUUID().toString();
			
            String attachmentMessage = "[attachment]" + attachmentId + "|"+ sender + "|" + fileName + "|" + file.getAbsolutePath() + "|unread|" + mimeType + "|" + timestamp;

            redisCommands.rpush(messageKey, attachmentMessage);
            redisCommands.rpush(reverseKey, attachmentMessage);
			
           // DFSUtil.storeMessage(sender, recipient, attachmentMessage);
			NotificationServlet.addNotification(sender, recipient, "[Attachment=> " + fileName + "]", attachmentId);
		}
		else {
			String message = messageId + ": " + sender + ": " + text + ": " + "unread" + ": " + timestamp;
			redisCommands.rpush(messageKey, message);
			redisCommands.rpush(reverseKey, message);
			
			//DFSUtil.storeMessage(messageId, sender, recipient, text, "unread");
			NotificationServlet.addNotification(sender, recipient, text, messageId);

			response.setStatus(HttpServletResponse.SC_OK);
		}
    }
}