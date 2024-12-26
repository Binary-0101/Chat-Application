import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import io.lettuce.core.api.sync.RedisCommands;
import com.google.gson.*;


@WebServlet("/FetchGroupsServlet")
public class FetchGroupsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
          RedisCommands<String, String> redisCommands = RedisUtil.getConnection();;
            
            List<String> groupKeys = redisCommands.keys("group:*");
            List<String> userGroups = new ArrayList<>();

            String email = (String) request.getSession().getAttribute("email");
            for (String groupKey : groupKeys) {
                List<String> members = redisCommands.lrange(groupKey, 0, -1);
                if (members.contains(email)) {
                    userGroups.add(groupKey);
                }
            }

            response.getWriter().write(new Gson().toJson(userGroups));
        }
}
