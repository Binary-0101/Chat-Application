<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link href="https://fonts.googleapis.com/css2?family=Varela+Round&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Varela Round', sans-serif;
            background-color: #f4f4f4; 
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            padding: 20px;
            color: #333;
        }

        .form-container {
            background-color: #ffffff; 
            padding: 40px 35px;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
            border-radius: 15px;
            width: 100%;
            max-width: 420px;
            text-align: center;
            border: 1px solid #ddd;
        }

        .form-container h2 {
            font-size: 36px;
            font-weight: 700;
            color: #2A3663; 
            margin-bottom: 30px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .form-container form input {
            margin-bottom: 15px;
            padding: 12px;
            font-size: 16px;
            border: 1px solid #ddd;
            border-radius: 8px;
            transition: border-color 0.3s;
            width: 100%;
        }

        .form-container form input:focus {
            border-color: #6a1b9a; 
        }

        .form-container form button {
            padding: 14px;
            font-size: 18px;
            background-color: #2A3663; 
            color: #fff;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            transition: background-color 0.3s, transform 0.2s;
            width: 100%;
            text-transform: uppercase;
            font-weight: 600;
            position: relative;
        }

        .form-container form button:hover {
            background-color: #4A628A; 
            transform: translateY(-2px);
        }

        .form-container form button:active {
            transform: translateY(0);
        }

        .spinner {
            display: none;
            margin-left: 10px;
        }

        .spinner img {
            width: 30px;
            height: 30px;
        }
		
		.css-spinner {
        border: 4px solid #f3f3f3;
        border-top: 4px solid #3498db;
        border-radius: 50%;
        width: 16px;
        height: 16px;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

        .form-container .switch {
            margin-top: 20px;
            font-size: 14px;
        }

        .form-container .switch a {
            color: #2A3663; 
            text-decoration: none;
        }

        .form-container .switch a:hover {
            text-decoration: underline;
        }

        .error {
            color: red; 
            font-size: 14px;
            margin-bottom: 15px;
        }

        .background-gradient {
            background: linear-gradient(135deg, #E8BCB9, #F5EFE7); 
            height: 100vh;
            position: absolute;
            width: 100%;
            z-index: -1;
        }
    </style>
</head>
<body>
    <div class="background-gradient"></div> 

    <div class="form-container">
        <h2>Login</h2>
        <form id="loginForm">
            <input type="email" id="email" name="email" placeholder="Email" required />
            <input type="password" id="password" name="password" placeholder="Password" required />
            <button type="submit">
                <span id="buttonText">Login</span>
                <span id="spinner" class="spinner" style="display: none">
                    <div class="css-spinner"></div>
                </span>
            </button>
        </form>

        <div id="error-message" class="error"></div>

        <div class="switch">
            <p>Don't have an account? <a href="signup.jsp">Sign up</a></p>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function() {
            $("#loginForm").submit(function(event) {
                event.preventDefault(); 

                var email = $("#email").val();
                var password = $("#password").val();

                $("#buttonText").text("Logging in...");
                $("#spinner").show();
                $("button").prop("disabled", true);

                $.ajax({
                    url: 'SignInServlet',
                    method: 'POST',
                    data: { email: email, password: password },
                    success: function(response) {
                        if (response.error) {
                            $("#error-message").text(response.error);
                        } else if (response.success) {
                            window.location.href = "verifyEmail.jsp?email=" + response.email;
                        }
                    },
                    error: function() {
                        $("#error-message").text("An error occurred. Please try again later.");
                    },
                    complete: function() {
                        $("#buttonText").text("Login");
                        $("#spinner").hide();
                        $("button").prop("disabled", false);
                    }
                });
            });
        });
    </script>
</body>
</html>
