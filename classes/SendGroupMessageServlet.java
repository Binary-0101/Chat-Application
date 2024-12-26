import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.*;
import com.google.gson.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.nio.file.*;
import java.util.*;
import java.util.UUID; 

@MultipartConfig
public class SendGroupMessageServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
        System.out.println("Came to SendGroupMessageServlet");

        String sender = (String) request.getSession().getAttribute("email");
        String groupId = request.getParameter("groupId");
        String text = request.getParameter("text");
        Part attachment = request.getPart("attachment");
		
		System.out.println("printed " + groupId+" "+text);

        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Group ID must not be null or empty");
        }

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String messageId = UUID.randomUUID().toString();

        List<String> groupMembers = redisCommands.lrange(groupId, 0, -1);
        groupMembers.removeIf(String::isEmpty);

        if (attachment != null && attachment.getSize() > 0) {
            String fileName = Paths.get(attachment.getSubmittedFileName()).getFileName().toString();
            String uploadPath = System.getProperty("user.dir") + "/local/dfs/group/attachments/";
            File dir = new File(uploadPath);

            if (!dir.exists()) {
                System.out.println("Creating directory...");
                dir.mkdirs();
            }

            File file = new File(uploadPath, fileName);
            System.out.println("Full file path: " + file.getAbsolutePath());

            try (InputStream input = attachment.getInputStream()) {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            String mimeType = Files.probeContentType(file.toPath());
            System.out.println("MimeType: " + mimeType);

            String attachmentId = UUID.randomUUID().toString();
			redisCommands.hset("attachments", attachmentId, file.getAbsolutePath() + "|" + fileName + "|" + mimeType);

            String attachmentMessage = "[attachment]" + attachmentId + "|" + sender + "|" + fileName + "|" + file.getAbsolutePath() + "|unread|" + mimeType;

            for (String member : groupMembers) {
                String groupMessageKey = "group:" + groupId + ":" + member;
                redisCommands.rpush(groupMessageKey, attachmentMessage);
            }


			redisCommands.hset("attachments", attachmentId, file.getAbsolutePath() + "|" + fileName + "|" + mimeType);
			
			DFSUtil.storeGroupMessage(sender, groupId.split(":")[1] , attachmentMessage);
			NotificationServlet.addGroupNotification(groupId, sender, "[Attachment: " + fileName + "]", messageId, groupMembers);
        } else {
            String groupMessage = messageId + ": " + sender + ": " + text + ": " + "unread";

            for (String member : groupMembers) {
                String groupMessageKey = "group:" + groupId + ":" + member;
                redisCommands.rpush(groupMessageKey, groupMessage);
            }
			NotificationServlet.addGroupNotification(groupId, sender, text, messageId, groupMembers);
            DFSUtil.storeGroupMessage(groupId.split(":")[1], messageId, sender, text, "unread");
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
