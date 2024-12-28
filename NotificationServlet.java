import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.ArrayList;
import java.util.*;
import com.google.gson.*;

public class NotificationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");

        if (email == null) {
            response.sendRedirect("signin.jsp");
            return;
        }

        String action = request.getParameter("action");
        if ("fetchCount".equals(action)) {
            int unreadCount = getUnreadNotificationCount(email);
            response.setContentType("application/json");
            response.getWriter().write("{\"unreadCount\": " + unreadCount + "}");
        } else if ("fetchNotifications".equals(action)) {
            List<String[]> notifications = getNotifications(email);
            Gson gson = new Gson();
            String json = gson.toJson(notifications);
            response.setContentType("application/json");
            response.getWriter().write(json);
        }
    }
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
		
		if (email == null) {
            response.sendRedirect("signin.jsp");
            return;
        }
		
		String messageId = request.getParameter("messageId");
		String groupId = request.getParameter("groupId");
		String recipient = request.getParameter("recipient");
		
		if(groupId != null) {
			removeGroupNotification(groupId, recipient);
		} else if(messageId != null) {
			removeNotification(recipient, messageId);
		} else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
	}

    private List<String[]> getNotifications(String email) {
        List<String[]> notifications = new ArrayList<>();
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + email;

        List<String> storedNotifications = redisCommands.lrange(notificationKey, 0, -1);
		
		if(storedNotifications.isEmpty()) {
			storedNotifications = DFSUtil.fetchNotifications(email);
		}
		
        for (String notificationData : storedNotifications) {
			String[] parts;
			System.out.println(notificationData);
			if(notificationData.startsWith("group")) {
				parts = notificationData.split(":", 8); 
				if (parts[7].trim().equals("unread")) {
					notifications.add(new String[] { parts[1], parts[3], parts[4], parts[5], parts[6] });
				} 
			} else {
				parts = notificationData.split(": ", 5); 
				if (parts.length == 5 && parts[3].equals("unread")) {
					notifications.add(new String[] { parts[0], parts[1], parts[2], parts[4] });
				} 
			}
        }
		System.out.println("fetchNotifications " + notifications);
        return notifications;
    }

    public static void addNotification(String sender, String recipient, String message, String messageId) {
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + recipient;

        String notification = messageId + ": " + sender + ": " + message + ": unread" + ": " + recipient;
        redisCommands.rpush(notificationKey, notification);
		
		DFSUtil.storeNotifications(notification, recipient);
    }
	
	public static void addGroupNotification(String groupId, String sender, String message, String messageId, List<String> groupMembers) {
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		
		for(String member : groupMembers) {
			String notificationKey = "notifications:" + member;
			if(member.equals(sender)) continue;
 			String notification = groupId + ":" + messageId + ":" + sender + ":" + message + ":unread";
			redisCommands.rpush(notificationKey, notification);
		}
	}
	
	public static void removeNotification(String recipient, String messageId) {
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		String notificationKey = "notifications:" + recipient;
		
		List<String> notifications = redisCommands.lrange(notificationKey, 0, -1);
		
		for(int i=0;i<notifications.size();i++) {
			if(notifications.get(i).startsWith(messageId)) {
				redisCommands.lrem(notificationKey, 1, notifications.get(i));
				//DFSUtil.removeNotificationFromDFS(recipient, notifications.get(i));
				break;
			}
		}
	}
	
	private void removeGroupNotification(String groupId, String email) {
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();

        String groupMembersKey = "group:" + groupId.split(":")[0]  + ":" + groupId;
        List<String> groupMembers = redisCommands.lrange(groupMembersKey, 0, -1);

        for (String member : groupMembers) {
            String notificationKey = "notifications:" + member;
            List<String> notifications = redisCommands.lrange(notificationKey, 0, -1);

            for (String notification : notifications) {
                if (notification.contains(groupId)) {
                    redisCommands.lrem(notificationKey, 1, notification);
                    System.out.println("Notification removed for member: " + member);
                    break;
                }
            }
        }
    }
	
	private int getUnreadNotificationCount(String email) {
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + email;

        List<String> notifications = redisCommands.lrange(notificationKey, 0, -1);
        int count = 0;
		
		if(notifications.isEmpty()) {
			notifications = DFSUtil.fetchNotifications(email);
		}

        for (String notification : notifications) {
            if (notification.endsWith("unread")) {
                count++;
            }
        }
        return count;
    }
}
