import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import io.lettuce.core.api.sync.RedisCommands;

public class AddMembersToGroupServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
        String groupId = request.getParameter("groupId");
        String[] selectedUsers = request.getParameterValues("selectedUsers");

        if (selectedUsers == null || selectedUsers.length == 0) {
            response.getWriter().println("No users selected.");
            return;
        }

        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        for (String user : selectedUsers) {
            redisCommands.rpush(groupId, user);
        }

        response.sendRedirect("welcome.jsp?groupId=" + groupId);
    }
}
