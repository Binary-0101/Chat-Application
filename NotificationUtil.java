import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotificationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");

        if (email == null) {
            response.sendRedirect("signin.jsp");
            return;
        }

        // Fetch notifications
        List<String[]> notifications = getNotifications(email);

        // Pass notifications to JSP
        request.setAttribute("notifications", notifications);
        RequestDispatcher dispatcher = request.getRequestDispatcher("notifications.jsp");
        dispatcher.forward(request, response);
    }

    private List<String[]> getNotifications(String email) {
        List<String[]> notifications = new ArrayList<>();
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + email;

        List<String> storedNotifications = redisCommands.lrange(notificationKey, 0, -1);
        for (String notificationData : storedNotifications) {
            String[] parts = notificationData.split(": ", 3); // Split sender, message, read status
            if (parts.length == 3) {
                notifications.add(new String[] { parts[0], parts[1], parts[2] });
            }
        }
        return notifications;
    }

    public static void markNotificationAsRead(String email, String sender) {
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + email;

        List<String> notifications = redisCommands.lrange(notificationKey, 0, -1);
        for (int i = 0; i < notifications.size(); i++) {
            String notification = notifications.get(i);
            if (notification.contains(sender)) {
                String updatedNotification = notification.replace(": unread", ": read");
                redisCommands.lset(notificationKey, i, updatedNotification);
                break;
            }
        }
    }

    public static void addNotification(String sender, String recipient, String message) {
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String notificationKey = "notifications:" + recipient;

        String notification = sender + ": " + message + ": unread";
        redisCommands.rpush(notificationKey, notification);
    }
}
