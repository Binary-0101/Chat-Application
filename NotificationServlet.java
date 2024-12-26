import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.ArrayList;
import java.util.*;

public class NotificationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");

        if (email == null) {
            response.sendRedirect("signin.jsp");
            return;
        }

        List<String[]> notifications = getNotifications(email);

        request.setAttribute("notifications", notifications);
        RequestDispatcher dispatcher = request.getRequestDispatcher("notification.jsp");
        dispatcher.forward(request, response);
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
				parts = notificationData.split(": ", 4); 
				if (parts.length == 4 && parts[3].equals("unread")) {
					notifications.add(new String[] { parts[0], parts[1], parts[2] });
				} 
			}
        }
		System.out.println("fetchNotifications " + notifications);
        return notifications;
    }

    public static void addNotification(String sender, String recipient, String message, String messageId) {
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + recipient;

        String notification = messageId + ": " + sender + ": " + message + ": unread";
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
				break;
			}
		}
	}
}
