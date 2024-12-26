import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import io.lettuce.core.api.sync.RedisCommands;
import com.google.gson.*;

public class FetchGroupsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = (String) request.getSession().getAttribute("email");
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
        List<String> groupKeys = redisCommands.keys("group:*");
        List<String> userGroups = new ArrayList<>();

        for (String groupKey : groupKeys) {
            List<String> members = redisCommands.lrange(groupKey, 0, -1);
            if (members.contains(email)) {
                String groupName = groupKey.split(":")[1];
                userGroups.add(groupName);
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(userGroups));
    }
}
