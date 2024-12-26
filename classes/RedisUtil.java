import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.RedisClient;

public class RedisUtil {
    private static final String REDIS_URI = "redis://localhost:6379"; 
    private static RedisClient redisClient = RedisClient.create(REDIS_URI);
    private static StatefulRedisConnection<String, String> connection = redisClient.connect();
    private static RedisCommands<String, String> commands = connection.sync();
	
	 public static RedisCommands<String, String> getConnection() {
        return commands;
    }

    public static void storeOTP(String email, String otp) {
        String key = "OTP:" + email;
        commands.setex(key, 300, otp); 
    }

    public static String getOTP(String email) {
        String key = "OTP:" + email;
        return commands.get(key); 
    }

    public static void deleteOTP(String email) {
        String key = "OTP:" + email;
        commands.del(key);
    }
	
	public static void storeUserDetails(String email, String name, String password) {
		String key = "USER:" + email;
		String value = name + " | " + password;
		commands.setex(key, 300, value);
	}
	
	public static String[] getUserDetails(String email) {
        String key = "USER:" + email;
        String value = commands.get(key);
        if (value != null) {
            return value.split("\\|");
        }
        return null;
    }

    public static void shutdown() {
        connection.close();
        redisClient.shutdown();
    }
}
