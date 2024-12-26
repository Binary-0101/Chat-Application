import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import io.lettuce.core.api.sync.RedisCommands;

public class CheckOnlineStatusServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Came to CheckOnlineStatusServlet");
		String recipientEmail = (String) request.getSession().getAttribute("recipient");
        
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		String onlineStatusKey = "User:" + recipientEmail + ":Status";
		String onlineStatus = redisCommands.get(onlineStatusKey);
		
		if(onlineStatus == null || onlineStatus.equals("null")) {
			try {
                String line = DFSUtil.fetchOnlineStatus(recipientEmail);
                if (line == null) {
                    onlineStatus = "";
                } else {
                    String[] parts = line.split(":");
                    onlineStatus = parts[3]; 
                }
            } catch (Exception e) {
                onlineStatus = "Unknown"; 
                e.printStackTrace();
            }
		}
		
        System.out.println("onlineStatus "+onlineStatus);
        response.getWriter().write(onlineStatus);  
    }
}
