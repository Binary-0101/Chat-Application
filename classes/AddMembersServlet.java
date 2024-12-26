import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.*;

public class AddMembersServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
        String groupId = request.getParameter("groupId");

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        List<String> groupMembers = redisCommands.lrange(groupId, 0, -1);
        
        Map<String, String> allUsers = (Map<String, String>) request.getSession().getAttribute("users");
        if (allUsers == null || groupMembers == null) {
            response.getWriter().println("Error fetching users or group members.");
            return;
        }
        
        // Filter users not in the group
        Map<String, String> nonGroupUsers = new HashMap<>();
        for (Map.Entry<String, String> user : allUsers.entrySet()) {
            if (!groupMembers.contains(user.getValue())) {
                nonGroupUsers.put(user.getKey(), user.getValue());
            }
        }

        request.setAttribute("nonGroupUsers", nonGroupUsers);
        request.setAttribute("groupId", groupId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("addMembers.jsp");
        dispatcher.forward(request, response);
    }
}
