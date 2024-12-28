import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import io.lettuce.core.api.sync.RedisCommands;
import com.google.gson.*;

@WebServlet("/FetchGroupsServlet")
public class FetchGroupsServlet extends HttpServlet {
	private static final String DFS_GROUP_MEMBERS = "local/dfs/chat/group_members/";
	 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        RedisCommands<String, String> redisCommands = RedisUtil.getConnection();;
            
        List<String> groupKeys = redisCommands.keys("group:*");
		
		if(groupKeys.isEmpty()) {
			List<Map<String, String>> userGroups  = fetchGroupsFromDFS(request);
			response.getWriter().write(new Gson().toJson(userGroups));
            return;
		}
		
		List<Map<String, String>> userGroups = new ArrayList<>();
		String email = (String) request.getSession().getAttribute("email");
		
		for (String groupKey : groupKeys) {
			List<String> members = redisCommands.lrange(groupKey, 0, -1);
			if (members.contains(email)) {
				Map<String, String> groupData = new HashMap<>();
				groupData.put("groupName", groupKey.split(":")[1]); 
				groupData.put("groupId", groupKey); 
				userGroups.add(groupData);
			}
		}

            response.getWriter().write(new Gson().toJson(userGroups));
        }
		
	private List<Map<String,String>> fetchGroupsFromDFS(HttpServletRequest request) {
		List<Map<String, String>> userGroups = new ArrayList<>();	
        File dfsRootDirectory = new File(DFS_GROUP_MEMBERS);
        String email = (String) request.getSession().getAttribute("email");

        if (dfsRootDirectory.exists() && dfsRootDirectory.isDirectory()) {
            File[] shardDirectories = dfsRootDirectory.listFiles(File::isDirectory);

            if (shardDirectories != null) {
                for (File shardDir : shardDirectories) {
                    File[] groupFilesInShard = shardDir.listFiles((dir, name) -> name.endsWith(".txt"));
                    
                    if (groupFilesInShard != null) {
                        for (File groupFile : groupFilesInShard) {
                            String groupName = groupFile.getName().replace(".txt", "");
                            List<String> groupMembers = new ArrayList<>();
                            String redisGroupKey[] = new String[1];

                            String primaryPath = shardDir.getAbsolutePath() + "/" + groupFile.getName();
                            if (readGroupDataFromFile(primaryPath, groupMembers, redisGroupKey)) {
                                if (groupMembers.contains(email)) {
                                    Map<String, String> groupData = new HashMap<>();
									groupData.put("groupName", groupName);
									groupData.put("groupId", redisGroupKey[0]);
									userGroups.add(groupData);
									
									storeGroupInRedis(redisGroupKey[0], groupMembers);
                                }
                                continue;
                            }

                            for (File replicaShardDir : shardDirectories) {
                                if (!replicaShardDir.equals(shardDir)) {
                                    String replicaPath = replicaShardDir.getAbsolutePath() + "/" + groupFile.getName();
                                    if (readGroupDataFromFile(replicaPath, groupMembers, redisGroupKey)) {
                                        if (groupMembers.contains(email)) {
                                            Map<String, String> groupData = new HashMap<>();
											groupData.put("groupName", groupName);
											groupData.put("groupId", redisGroupKey[0]); 
											userGroups.add(groupData);
											
											storeGroupInRedis(redisGroupKey[0], groupMembers);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("No shard directories found in DFS root: " + DFS_GROUP_MEMBERS);
            }
        } else {
            System.out.println("DFS root directory does not exist or is not a directory: " + DFS_GROUP_MEMBERS);
        }

        return userGroups;
    }

    private boolean readGroupDataFromFile(String filePath, List<String> groupMembers, String[]  redisGroupKey) {
        File groupFile = new File(filePath);
        if (groupFile.exists() && groupFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(groupFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Group Key: ")) {
                        redisGroupKey[0] = line.substring("Group Key: ".length()).trim();
                    } else {
                        groupMembers.add(line.trim());
                    }
                }
                return redisGroupKey[0] != null && !groupMembers.isEmpty();
            } catch (IOException e) {
                System.out.println("Failed to read group file: " + filePath);
                return false;
            }
        }
        return false;
    }

    private void storeGroupInRedis(String redisGroupKey, List<String> groupMembers) {
        if (redisGroupKey != null && !groupMembers.isEmpty()) {
            RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
            redisCommands.del(redisGroupKey);
            redisCommands.rpush(redisGroupKey, groupMembers.toArray(new String[0]));
            System.out.println("Stored group in Redis: " + redisGroupKey);
        } else {
            System.out.println("Invalid group data.");
        }
    }
}
