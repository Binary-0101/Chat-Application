import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import io.lettuce.core.api.sync.RedisCommands;

public class CreateGroupChatServlet extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	throws IOException, ServletException {
		String email = (String) request.getSession().getAttribute("email");
		
		String[] selectedUsers = request.getParameterValues("selectedUsers");
		
		if (selectedUsers == null || selectedUsers.length == 0) {
            response.getWriter().println("No users selected for the group.");
            return;
        }
		
		List<String> groupMembers = new ArrayList<>(Arrays.asList(selectedUsers));
		groupMembers.add(email);
		
		System.out.println(groupMembers);
		
		String groupName = request.getParameter("groupName");
		String groupId = groupName + ":" + UUID.randomUUID().toString();
		
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        String redisGroupKey = "group:" + groupName + ":" + groupId;

		for (String member : groupMembers) {
            redisCommands.rpush(redisGroupKey, member);
        }
		
		DFSUtil.storeGroup(redisGroupKey, groupName, groupMembers);
		
		response.sendRedirect("welcome.jsp?groupId=" + groupId);
	}
}