import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import io.lettuce.core.api.sync.RedisCommands;
import com.google.gson.*;
import java.util.*;


public class DeleteMessageServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
        BufferedReader reader = request.getReader();
        MessageDeleteData deleteData = gson.fromJson(reader, MessageDeleteData.class);

        String sender = (String) request.getSession().getAttribute("email");
        String recipient = deleteData.recipient;
		String groupId = deleteData.groupId;
        String messageId = deleteData.messageId; 
		String attachmentId = deleteData.attachmentId;
		
		System.out.println("delete server "+ attachmentId);

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		String messageKey = "chat:" + sender + ":" + recipient;
        String reverseKey = "chat:" + recipient + ":" + sender;
		
		if (attachmentId != null && !attachmentId.isEmpty()) {
			String attachmentInfo = redisCommands.hget("attachments", attachmentId);
            if (attachmentInfo != null) {
                String[] attachmentDetails = attachmentInfo.split("\\|");
                String filePath = attachmentDetails[0]; 
                File attachmentFile = new File(filePath);
                if (attachmentFile.exists()) {
                    boolean deleted = attachmentFile.delete();
                    if (deleted) {
                        System.out.println("Attachment file deleted: " + filePath);
                    } else {
                        System.out.println("Failed to delete attachment file: " + filePath);
                    }
                }
            }
			List<String> messagesSenderToRecipient = redisCommands.lrange(messageKey, 0, -1);
            messagesSenderToRecipient.removeIf(msg -> msg.startsWith("[attachment]" + attachmentId));
			
			redisCommands.del(messageKey);
            if (!messagesSenderToRecipient.isEmpty()) {
                redisCommands.rpush(messageKey, messagesSenderToRecipient.toArray(new String[0]));
            }
			
			List<String> messagesRecipientToSender = redisCommands.lrange(reverseKey, 0, -1);
            messagesRecipientToSender.removeIf(msg -> msg.startsWith("[attachment]" + attachmentId));

            redisCommands.del(reverseKey);
            if (!messagesRecipientToSender.isEmpty()) {
                redisCommands.rpush(reverseKey, messagesRecipientToSender.toArray(new String[0]));
            }
			
			DFSUtil.deleteMessageFromFile(sender, recipient, attachmentId);
		}
		
		if(!groupId.equals("null") && groupId != null && !groupId.isEmpty()) {
			System.out.println("abcddefibg" + recipient + " " + groupId);
			List<String> groupMembers = redisCommands.lrange(groupId, 0, -1);
            groupMembers.removeIf(String::isEmpty);
			
			for (String member : groupMembers) {
                String groupMessageKey = "group:" + groupId + ":" + member;
                List<String> messages = redisCommands.lrange(groupMessageKey, 0, -1);

                if (!messages.isEmpty()) {
                    messages.removeIf(msg -> msg.startsWith(messageId + ":"));

                    redisCommands.del(groupMessageKey);
					
					if(!messages.isEmpty())
                    redisCommands.rpush(groupMessageKey, messages.toArray(new String[0]));
                }
            }
			DFSUtil.deleteGroupMessageFromFile(groupId.split(":")[1], messageId);
		}
		else {
			System.out.println("bcddefibg" + recipient + " " + groupId);

			List<String> messagesSenderToRecipient = redisCommands.lrange(messageKey, 0, -1);
            messagesSenderToRecipient.removeIf(msg -> msg.startsWith(messageId + ":"));

            redisCommands.del(messageKey);
            if (!messagesSenderToRecipient.isEmpty()) {
                redisCommands.rpush(messageKey, messagesSenderToRecipient.toArray(new String[0]));
            }
			
			List<String> messagesRecipientToSender = redisCommands.lrange(reverseKey, 0, -1);
            messagesRecipientToSender.removeIf(msg -> msg.startsWith(messageId + ":"));

            redisCommands.del(reverseKey);
            if (!messagesRecipientToSender.isEmpty()) {
                redisCommands.rpush(reverseKey, messagesRecipientToSender.toArray(new String[0]));
            }
			
			DFSUtil.deleteMessageFromFile(sender, recipient, messageId);
		}

		response.setStatus(HttpServletResponse.SC_OK);
}
    private static class MessageDeleteData {
        String recipient;
        String messageId;  
		String groupId;
		String attachmentId;
    }
}
