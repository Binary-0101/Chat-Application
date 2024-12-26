import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import io.lettuce.core.api.sync.RedisCommands;

public class MarkNotificationReadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        String sender = request.getParameter("sender");

        if (email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (sender == null || sender.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
		
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + email;
		
		for (String notification : redisCommands.lrange(notificationKey, 0, -1)) {
            if (notification.startsWith(sender + ":")) {
                String updatedNotification = notification.replace(": unread", ": read");
                redisCommands.lrem(notificationKey, 1, notification); // Remove old
                redisCommands.rpush(notificationKey, updatedNotification); // Add updated
                break;
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
