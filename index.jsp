<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat Application</title>
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

        .container {
            background: #fff; 
            padding: 40px 35px;
            border-radius: 15px;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 420px;
            text-align: center;
            border: 1px solid #ddd;
        }

        h1 {
            font-size: 36px;
            font-weight: 700;
            color: #2A3663; 
            margin-bottom: 30px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .button {
            display: block;
            width: 100%;
            padding: 16px;
            font-size: 18px;
            font-weight: 600;
            background-color: #2A3663; 
            color: white; 
            border: 1px solid #2A3663; 
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-bottom: 20px;
            text-transform: uppercase;
        }

        .button:hover {
            background-color: #4A628A;
        }

        .button:focus {
            outline: none;
        }

        .small-text {
            font-size: 14px;
            color: #2A3663;
            margin-top: 10px;
        }

        a {
            color: #6a1b9a;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
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

    <div class="container">
        <h1>Welcome to My Chat Application</h1>

        <div class="form-container">
            <form action="signup.jsp" method="get">
                <button type="submit" class="button">Sign Up</button>
            </form>

            <form action="signin.jsp" method="get">
                <button type="submit" class="button">Sign In</button>
            </form>
        </div>
    </div>
</body>
</html>
