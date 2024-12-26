import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Random;


public class SignUpServlet extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
   		throws IOException, ServletException  
	{
		System.out.println("Came to SignUpSerevlet");
		
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		response.setContentType("application/json");
		
		try {
			if (DBUtil.checkEmailExists(email)) {
				response.getWriter().write("{\"error\": \"Email already exists.\"}");
                return;
            }
			
			String otp = generateOTP();
			
			RedisUtil.storeOTP(email, otp);
			RedisUtil.storeUserDetails(email, name, password);
			
			String subject = "Email verification";
			String body = "Your OTP is: " + otp + " only valid for 5 minutes";
			EmailUtil.sendEmail(email, subject, body);
			
			response.getWriter().write("{\"success\": true, \"email\": \"" + email + "\"}");
		}  catch (Exception e) {
            e.printStackTrace();
			response.getWriter().write("{\"error\": \"Something went wrong.\"}");
        }
	}
	
	private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); 
    }
}