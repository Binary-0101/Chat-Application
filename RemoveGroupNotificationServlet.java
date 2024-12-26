import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.*;
import java.io.*;
import com.google.gson.*;

public class RemoveGroupNotificationServlet extends HttpServlet {
	private static final Gson gson = new Gson();
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader reader = request.getReader();
        MessageRemoveNotification removeGroupNotification = gson.fromJson(reader, MessageRemoveNotification.class);
		
        String groupId = removeGroupNotification.groupId;
        String messageId = removeGroupNotification.messageId;
        String email = (String) request.getSession().getAttribute("email");
		
		System.out.println(email + "dsnfk " + messageId + "ed " + groupId);
        if (email == null || groupId == null || messageId == null) {
			System.out.println("Invalid data: email=" + email + ", groupId=" + groupId + ", messageId=" + messageId);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();

		String groupMembersKey = "group:" + groupId.split(":")[0] + ":" + groupId;
        List<String> groupMembers = redisCommands.lrange(groupMembersKey, 0, -1);
		System.out.println(groupMembers);
        for (String member : groupMembers) {
            String notificationKey = "notifications:" + member;
            List<String> notifications = redisCommands.lrange(notificationKey, 0, -1);

            for (String notification : notifications) {
				System.out.println("notification from iteration" + notification);
                if (notification.contains(groupId)) {
                    redisCommands.lrem(notificationKey, 1, notification);
                    System.out.println("Notification removed for member: " + member);
                    break;
                }
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
	private static class MessageRemoveNotification {
        String groupId;
        String messageId;
    }
}
