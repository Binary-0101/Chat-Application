import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import io.lettuce.core.api.sync.RedisCommands;
import com.google.gson.*;
import java.util.*;

public class MarkAsReadServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		System.out.println("Came to markasreadservlet");
		
        BufferedReader reader = request.getReader();
        MessageReadData readData = gson.fromJson(reader, MessageReadData.class);

        String sender = (String) request.getSession().getAttribute("email");
        String recipient = readData.recipient;
        String messageId = readData.messageId;
		String attachmentId = readData.attachmentId;
		
		System.out.println("markasreadservlet: "+ recipient + ": " + messageId);
		System.out.println("markasreadservlet: "+ recipient + ": " + attachmentId);
		
        if ((messageId == null && attachmentId == null) || 
			(messageId != null && attachmentId != null)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Invalid data received");
			return;
		}

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String senderKey = "chat:" + sender + ":" + recipient; 
        String recipientKey = "chat:" + recipient + ":" + sender; 
		
		boolean senderUpdated = false, recipientUpdated = false;

        try {
			if(messageId != null) {
				List<String> senderMessages = redisCommands.lrange(senderKey, 0, -1);
				for (int i = 0; i < senderMessages.size(); i++) {
					String message = senderMessages.get(i);
					if (message.contains(messageId) && message.contains(": unread")) {
						String updatedMessage = message.replace(": unread", ": read");
						redisCommands.lset(senderKey, i, updatedMessage);
						senderUpdated = true;
						break;
					}
				}
				
				if(senderUpdated) {
					DFSUtil.updateMessageStatusInFile(sender, recipient, messageId, attachmentId, "read");
				}

				List<String> recipientMessages = redisCommands.lrange(recipientKey, 0, -1);
				for (int i = 0; i < recipientMessages.size(); i++) {
					String message = recipientMessages.get(i);
					if (message.contains(messageId) && message.contains(": unread")) {
						String updatedMessage = message.replace(": unread", ": read");
						redisCommands.lset(recipientKey, i, updatedMessage);
						recipientUpdated = true;
						break;
					}
				}
				
				if(recipientUpdated) {
					DFSUtil.updateMessageStatusInFile(sender, recipient, messageId, attachmentId, "read");
				}
				
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				List<String> senderMessages = redisCommands.lrange(senderKey, 0, -1);
				for (int i = 0; i < senderMessages.size(); i++) {
					String message = senderMessages.get(i);
					if (message.contains("[attachment]"+attachmentId) && message.contains("|unread|")) {
						String updatedMessage = message.replace("|unread", "|read");
						redisCommands.lset(senderKey, i, updatedMessage);
						senderUpdated = true;
						break;
					}
				}
				if(senderUpdated) {
					DFSUtil.updateMessageStatusInFile(sender, recipient, messageId, attachmentId, "read");
				}

				List<String> recipientMessages = redisCommands.lrange(recipientKey, 0, -1);
				for (int i = 0; i < recipientMessages.size(); i++) {
					String message = recipientMessages.get(i);
					if (message.contains("[attachment]"+attachmentId) && message.contains("|unread|")) {
						String updatedMessage = message.replace("|unread", "|read");
						redisCommands.lset(recipientKey, i, updatedMessage);
						recipientUpdated = true;
						break;
					}
				}
				
				
				if(recipientUpdated) {
					DFSUtil.updateMessageStatusInFile(sender, recipient, messageId, attachmentId, "read");
				}
				
				response.setStatus(HttpServletResponse.SC_OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }

    private static class MessageReadData {
        String recipient;
        String messageId;
		String attachmentId;
    }
}