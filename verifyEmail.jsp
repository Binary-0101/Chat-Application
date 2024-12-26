<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Email</title>
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
            color: #2A3663
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
        }

        .form-container form button:hover {
            background-color: #4A628A; 
            transform: translateY(-2px);
        }

        .form-container form button:active {
            transform: translateY(0);
        }

        .error {
            color: red; 
            font-size: 14px;
            margin-top: 10px;
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
        <h2>Verify Email</h2>
        <form id="otpForm">
            <input type="hidden" name="email" value="<%= request.getParameter("email") %>" />
            <input type="text" id="otp" name="otp" placeholder="Enter OTP" required />
            <button type="submit">Verify</button>
        </form>
        <div id="error-message"></div>
    </div>
</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        $("#otpForm").submit(function(event) {
            event.preventDefault(); 

            var otp = $("#otp").val();
            var email = $("input[name='email']").val();

            $.ajax({
                url: 'VerifyEmailServlet',
                method: 'POST',
                data: { otp: otp, email: email },
                success: function(response) {
                    try {
                        if (response.error) {
                            $("#error-message").text(response.error).css("color", "red");
                        } else {
                            window.location.href = "FetchUsersServlet";
                        }
                    } catch (e) {
                        $("#error-message").text("Invalid server response.").css("color", "red");
                    }
                },
                error: function() {
                    $("#error-message").text("An error occurred. Please try again later.").css("color", "red");
                }
            });
        });
    });
</script>
</html>
