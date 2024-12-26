import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Random;

public class SignInServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
		System.out.println("Came to SignInHandler");
			
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
		
		try {
			boolean emailExists = DBUtil.checkEmailExists(email);
			
			if (!emailExists) {
                out.write("{\"error\": \"Email not found\"}");
				out.flush();
                return;
            }
			
			boolean checkPasswordValid = DBUtil.verifyUserPassword(email, password);
			
			if (!checkPasswordValid) {
                out.write("{\"error\": \"Invalid password\"}");
                out.flush();
                return;
            }
			
			String otp = generateOTP();
			RedisUtil.storeOTP(email, otp);
			
			String subject = "Login OTP";
			String body = "Your OTP is: " + otp + " and is only valid for 5 minutes";
			EmailUtil.sendEmail(email, subject, body);
			
			out.write("{\"success\": true, \"email\": \"" + email + "\"}");
            out.flush();
		}  catch (Exception e) {
            e.printStackTrace();
			out.write("{\"error\": \"Something went wrong\"}");
            out.flush();
        }
	}
	
	private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); 
    }
}
