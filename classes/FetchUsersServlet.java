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
		
		File dfsRootDirectory = new File(DFS_ROOT_GROUP);

        if (dfsRootDirectory.exists() && dfsRootDirectory.isDirectory()) {
            File[] shardDirectories = dfsRootDirectory.listFiles(File::isDirectory);

            if (shardDirectories != null) {
                for (File shardDir : shardDirectories) {
                    File[] groupFiles = shardDir.listFiles((dir, name) -> name.endsWith(".txt"));

                    if (groupFiles != null) {
                        for (File groupFile : groupFiles) {
                            String groupName = groupFile.getName().replace(".txt", "");
                            List<String> groupMembers = new ArrayList<>();
                            String redisGroupKey = null;

                            try (BufferedReader reader = new BufferedReader(new FileReader(groupFile))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.startsWith("Group Key: ")) {
                                        redisGroupKey = line.substring("Group Key: ".length()).trim();
                                    } else {
                                        groupMembers.add(line.trim());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("Failed to read group file: " + groupFile.getName());
                                continue;
                            }

                            if (redisGroupKey != null && !groupMembers.isEmpty()) {
                                redisCommands.del(redisGroupKey);
                                redisCommands.rpush(redisGroupKey, groupMembers.toArray(new String[0]));
                                System.out.println("Stored group in Redis: " + redisGroupKey);
                            } else {
                                System.out.println("Invalid group data in file: " + groupFile.getName());
                            }
                        }
                    }
                }
            } else {
                System.out.println("No shard directories found in DFS root: " + DFS_ROOT_GROUP);
            }
        } else {
            System.out.println("DFS root directory does not exist or is not a directory: " + DFS_ROOT_GROUP);
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("welcome.jsp");
        dispatcher.forward(request, response);
    }
}