import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
import io.lettuce.core.api.sync.RedisCommands;

public class ExitGroupServlet extends HttpServlet {
	private static final Gson gson = new Gson();
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	throws IOException, ServletException{
		BufferedReader reader = request.getReader();
		ExitGroupData exitData = gson.fromJson(reader, ExitGroupData.class);
		
        String groupId = exitData.groupId;
		String groupName = exitData.groupName;
        String email = (String) request.getSession().getAttribute("email");
		
		if (groupId == null || groupId.trim().isEmpty() || groupName == null || groupName.trim().isEmpty()) {
            response.getWriter().println("Group ID is not there.");
            return;
        }
		
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		
		String redisGroupKey = groupId;
		System.out.println("keyyyyyyy:" + redisGroupKey);
        boolean userExistsInGroup = redisCommands.lrange(redisGroupKey, 0, -1).contains(email);
		
		if (!userExistsInGroup) {
			response.getWriter().println("You are not part of the group.");
			return;
        }
		
		List<String> groupMembers = redisCommands.lrange(redisGroupKey, 0, -1);
		
		redisCommands.lrem(redisGroupKey, 1, email);
		DFSUtil.storeGroup(redisGroupKey, groupName, groupMembers);
		
		response.setContentType("text/plain"); 
        response.getWriter().write("You have successfully exited the group: " + groupName);


	}
	private static class ExitGroupData {
		private String groupId, groupName;
	}
}