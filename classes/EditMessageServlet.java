import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import io.lettuce.core.api.sync.RedisCommands;
import com.google.gson.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class EditMessageServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        BufferedReader reader = request.getReader();
        MessageEditData editData = gson.fromJson(reader, MessageEditData.class);

        String sender = (String) request.getSession().getAttribute("email");
        String recipient = editData.recipient;
        String messageId = editData.messageId;
        String newText = editData.newText;
		String groupId = editData.groupId;
		System.out.println("Group ka id" + groupId);
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		
		if(groupId != null && !groupId.isEmpty() && !groupId.equals("null")) {
			List<String> groupMembers = redisCommands.lrange(groupId, 0, -1);
			groupMembers.removeIf(String::isEmpty);
			
			boolean updated = false;
			for(String member : groupMembers) {
				String groupMessageKey = "group:" + groupId + ":" + member;
				List<String> groupMessages = redisCommands.lrange(groupMessageKey, 0, -1);
				
				for(int i=0;i<groupMessages.size();i++) {
					String msg = groupMessages.get(i);
					String[] parts = msg.split(": ", 4);
					
					if (parts.length == 4 && parts[0].equals(messageId)) {
						groupMessages.set(i, messageId + ": " + sender + ": " + newText + ": " + parts[3]);
						updated = true;
						break;
					}
				}
				if (updated) {
					redisCommands.del(groupMessageKey);
					redisCommands.rpush(groupMessageKey, groupMessages.toArray(new String[0]));
				}
			}
			System.out.println("updated" + updated);
			
			if (updated) {
				System.out.println("updatedsgewf" + updated);
				DFSUtil.editGroupMessageInFile(groupId.split(":")[1], messageId, newText);
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write("Group message updated successfully.");
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Message not found.");
			}
		} else {
			String senderKey = "chat:" + sender + ":" + recipient;
			String recipientKey = "chat:" + recipient + ":" + sender;

			List<String> senderMessages = redisCommands.lrange(senderKey, 0, -1);
			List<String> recipientMessages = redisCommands.lrange(recipientKey, 0, -1);

			boolean senderUpdated = false;
			boolean recipientUpdated = false;

			if (!senderMessages.isEmpty()) {
				for (int i = 0; i < senderMessages.size(); i++) {
					String msg = senderMessages.get(i);
					String[] parts = msg.split(": ", 5);

					if (parts.length == 5 && parts[0].equals(messageId)) {
						if (parts[1].equals(sender)) {
							senderMessages.set(i, messageId + ": " + sender + ": " + newText + ": " + parts[3] + ": " + timestamp);
							senderUpdated = true;
							break;
						}
					}
				}
			}

			if (!recipientMessages.isEmpty()) {
				for (int i = 0; i < recipientMessages.size(); i++) {
					String msg = recipientMessages.get(i);
					String[] parts = msg.split(": ", 5);

					if (parts.length == 5 && parts[0].equals(messageId)) {
						if (parts[1].equals(sender)) {
							recipientMessages.set(i, messageId + ": " + sender + ": " + newText + ": " + parts[3] + ": "+ timestamp);
							recipientUpdated = true;
							break;
						}
					}
				}
			}

			if (senderUpdated) {
				redisCommands.del(senderKey);
				redisCommands.rpush(senderKey, senderMessages.toArray(new String[0]));
				//DFSUtil.editMessageInFile(sender, recipient, messageId, newText);
			}
			if (recipientUpdated) {
				redisCommands.del(recipientKey);
				redisCommands.rpush(recipientKey, recipientMessages.toArray(new String[0]));
				//DFSUtil.editMessageInFile(sender, recipient, messageId, newText);
			}

			if (senderUpdated && recipientUpdated) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write("Message updated successfully.");
			} else if (!senderUpdated) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.getWriter().write("You do not have permission to edit this message.");
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Message not found.");
			}
		}
}

    private static class MessageEditData {
        String recipient;
        String messageId;
        String newText;
		String groupId;
    }
}
