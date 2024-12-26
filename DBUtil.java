import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class DBUtil {
	private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_application";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Sanjay@_17";
	
	static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
		}
	}
	
	public static Connection getConnection() throws SQLException {
		try {
			//System.out.println("Connecting to the database");
			return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		} catch(SQLException e) {
			e.printStackTrace();
			//System.out.println("Error in connecting to the database: " + e.getMessage());
			throw new SQLException("Error in connecting to the db");
		}
	}
		
		public static boolean saveUser(String email, String name, String password)  {
			String insertQuery = "insert into users (email, name, password) values (?, ?, ?)";
			try (Connection conn = getConnection()) {
				try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
					String hashedPassword = BCrypt.hashpw(password.trim(), BCrypt.gensalt());
					System.out.println("hashedPassword:"+hashedPassword);
					stmt.setString(1, email);
					stmt.setString(2, name);
					stmt.setString(3, hashedPassword);
					
					int rowsAffected = stmt.executeUpdate();
					return rowsAffected > 0;
				}
			} catch (SQLException e) {
					e.printStackTrace();
					//System.out.println("Error in executing SQL query or preparing statement: " + e.getMessage());
					return false;
			}
		}
		
		public static boolean verifyUserPassword(String email, String password) {
			String getEmailQuery = "select password from users where email = ?";
			try (Connection conn = getConnection()) {
				try (PreparedStatement stmt = conn.prepareStatement(getEmailQuery)) {
						stmt.setString(1, email);
						try(ResultSet rs = stmt.executeQuery()) {
							if(rs.next()) {
								String storedHashedPassword = rs.getString("password");
								//System.out.println("Stored Hashed Password: " + storedHashedPassword);
								//System.out.println("Entered Password: " + password);
								//System.out.println("Comparison Result: " + BCrypt.checkpw(password, storedHashedPassword));

								return BCrypt.checkpw(password, storedHashedPassword);
							}
						}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("SQL Exception occured");
			}
			return false;
		}
		
		public static boolean checkEmailExists(String email) {
			String checkEmailQuery = "select 1 from users where email = ?";
			try(Connection conn = getConnection()) {
				try(PreparedStatement stmt = conn.prepareStatement(checkEmailQuery)) {
					stmt.setString(1, email);
					try(ResultSet rs = stmt.executeQuery())  {
						return rs.next();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("SQL Exception occured");
			}
			return false;
		}
	}