import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import io.lettuce.core.api.sync.RedisCommands;

public class ExitGroupServlet extends HttpServlet {
	private static final Gson gson = new Gson();
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	throws IOException, ServletException{
		BufferedReader reader = request.getReader();
        Map<String, String> requestBody = gson.fromJson(reader, Map.class);
        String groupId = requestBody.get("groupId");
		String groupName = requestBody.get("groupName");
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
			List<String> temp = redisCommands.lrange(redisGroupKey, 0, -1);
		
		System.out.println("temporayr "+temp);
			System.out.println("you are not the part of the group." + email);
            response.getWriter().println("you are not the part of the group.");
            return;
        }
		
		redisCommands.lrem(redisGroupKey, 1, email);
		//DFSUtil.updateGroupFile(redisGroupKey, groupName, email);
		
		response.getWriter().println("User successfully removed from the group.");
	}
}