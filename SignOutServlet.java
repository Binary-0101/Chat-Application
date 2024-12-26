import jakarta.servlet.http.*;
import java.io.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SignOutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        String email = (String) request.getSession().getAttribute("email");
        System.out.println(email);

        if (email != null) {
            try {
                RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
                redisCommands.set("User:" + email + ":Status", "last seen: " + time.format(formatter));
				
				DFSUtil.updateOnlineStatus(email, "last seen: " + time.format(formatter));
                request.getSession().invalidate();
                response.sendRedirect("signin.jsp");
            } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().write("Error during sign-out. Please try again.");
            }
        }
    }
}
