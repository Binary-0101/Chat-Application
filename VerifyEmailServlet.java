import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.lettuce.core.api.sync.RedisCommands;

public class VerifyEmailServlet extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException{
		String otp = request.getParameter("otp");
		String email = request.getParameter("email");
		
		String storedOTP = RedisUtil.getOTP(email);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
		
		if(otp != null && otp.equals(storedOTP)) {
			RedisUtil.deleteOTP(email);
			
			String[] userDetails = RedisUtil.getUserDetails(email);
			if(userDetails != null) {
				String name = userDetails[0];
				String password = userDetails[1];
				
				DBUtil.saveUser(email, name, password);
			}
			
			RedisCommands<String, String> redisCommands = RedisUtil.getConnection();
			String onlineStatusKey = "User:" + email + ":Status";
			redisCommands.set(onlineStatusKey, "online");
			
			DFSUtil.storeOnlineStatus(email, "online");
			
			request.getSession().setAttribute("email", email);
			out.write("{\"success\" : true}");
		} else {
			out.write("{\"error\": \"Invalid OTP\"}");
        }
		out.flush();
	}
}