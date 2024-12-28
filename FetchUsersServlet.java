import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import io.lettuce.core.api.sync.RedisCommands;

public class FetchUsersServlet extends HttpServlet {
	
	private static final String DFS_ROOT_GROUP = "local/dfs/chat/group_members/";
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        Map<String, String> users = new HashMap<>();
		RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
		
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("select name, email from users")) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                users.put(name, email);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.getSession().setAttribute("users", users);

        RequestDispatcher dispatcher = request.getRequestDispatcher("welcome.jsp");
        dispatcher.forward(request, response);
    }
}