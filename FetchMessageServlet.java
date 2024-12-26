import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FetchMessageServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String sender = (String) request.getSession().getAttribute("email");
        String recipient = request.getParameter("recipient");
        String groupId = (String) request.getSession().getAttribute("groupId");

        System.out.println("GroupId: " + groupId);

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        List<String> messages = new ArrayList<>();

        if (groupId != null && !groupId.isEmpty()) {
            List<String> groupMembers = redisCommands.lrange(groupId, 0, -1);
            for (String member : groupMembers) {
                String groupMessageKey = "group:" + groupId + ":" + member;
                List<String> memberMessages = redisCommands.lrange(groupMessageKey, 0, -1);
                messages.addAll(memberMessages);
            }
			
			//System.out.println("messaage is group:" + messages);
            if (messages.isEmpty()) {
                messages = DFSUtil.fetchGroupMessages(groupId.split(":")[1]);
							System.out.println("in dfs: "+ messages);
							
				for (String message : messages) {
					for (String member : groupMembers) {
						String groupMessageKey = "group:" + groupId + ":" + member;
						redisCommands.rpush(groupMessageKey, message);
					}
				}
            }

        } else {
            String messageKey = "chat:" + sender + ":" + recipient;
            String reverseKey = "chat:" + recipient + ":" + sender;

            messages = redisCommands.lrange(messageKey, 0, -1);

            if (messages.isEmpty()) {
                messages = redisCommands.lrange(reverseKey, 0, -1);
            }
			
			System.out.println("messages from reddi s "+messages);

            if (messages.isEmpty()) {
                List<String> dfsMessages = DFSUtil.fetchMessages(sender, recipient);
                messages = dfsMessages;
				
				System.out.println("messages from dfs s "+messages);
				
                for (String dfsMessage : dfsMessages) {
                    redisCommands.rpush(messageKey, dfsMessage);
                    redisCommands.rpush(reverseKey, dfsMessage);
                }
            }
        }

        List<Message> messageList = new ArrayList<>();
        Set<String> seenMessageIds = new HashSet<>();

        for (String msg : messages) {
            if (msg.startsWith("[attachment]")) {
                String[] parts = msg.split("\\|", 6);
                if (parts.length == 6) {
                    String attachmentId = parts[0].replace("[attachment]", "").trim();
					String fileName = parts[2];
					String filePath = parts[3];
					String readStatus = parts[4];
					String mimeType = parts[5];
					
                    if (!seenMessageIds.contains(attachmentId)) {
                        seenMessageIds.add(attachmentId);
						
						redisCommands.hset("attachments", attachmentId, filePath + "|" + fileName + "|" + mimeType);
						
                        messageList.add(new Message(
                                parts[1].equals(sender) ? "sent" : "received",
                                parts[1],
                                parts[2],
                                parts[3],
                                parts[4],
                                parts[5],
                                attachmentId
                        ));
						System.out.println("mimeTyoe "+ parts[5]);
                    }
                }
            } else {
				//System.out.println("msg :" + msg);
                String[] parts = msg.split(": ", 4);
                if (parts.length == 4) {
                    String messageId = parts[0];

                    if (!seenMessageIds.contains(messageId)) {
                        seenMessageIds.add(messageId);

                        messageList.add(new Message(
                                parts[1].equals(sender) ? "sent" : "received",
                                parts[1],
                                parts[2],
                                parts[3],
                                messageId
                        ));
                    }
                }
            }
        }
		
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(messageList));
    }

    private static class Message {
        String type, text, readStatus, messageId, sender, fileUrl, fileName, filePath, mimeType, attachmentId;

        Message(String type, String sender, String text, String readStatus, String messageId) {
            this.type = type;
            this.text = text;
            this.readStatus = readStatus;
            this.messageId = messageId;
            this.sender = sender;
        }

        Message(String type, String sender, String fileName, String filePath, String readStatus, String mimeType, String attachmentId) {
            this.type = type;
            this.sender = sender;
            this.fileName = fileName;
            this.filePath = filePath;
            this.readStatus = readStatus;
            this.mimeType = mimeType;
            this.attachmentId = attachmentId;
			
			if (mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("image/")) {
				this.text = "[attachment]" + filePath + "|" + fileName + "|" + mimeType + "|" + attachmentId;
			} else {
				this.text = "[attachment]" + filePath + "|" + fileName + "|" + mimeType + "|" + attachmentId;
			}
        }
    }
}
