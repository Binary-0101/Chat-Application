<%@ page import="java.util.*" %>
<html>
	<head>
		<title>Create Group Chat</title>
		
		<style>
		body {
            font-family: 'Varela Round', sans-serif;
            background: linear-gradient(135deg, #E8BCB9, #F5EFE7);
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            padding: 20px;
            color: #333;
            box-sizing: border-box;
            flex-direction: column;
        }

        h2 {
            text-align: center;
            color: #2A3663; 
            margin-bottom: 30px;
            font-size: 26px;
            width: 100%; 
        }

        .user-container {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin: 15px 0;
            padding: 12px 20px;
            background-color: #fff;
            border-radius: 8px;
            border: 1px solid #ddd;
            width: 80%;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s;
        }

        .user-container:hover {
            transform: scale(1.02);
        }

        .user-container p {
            margin-right: 15px;
            font-size: 16px;
            color: #555;
            font-weight: bold;
            flex: 1;
        }

        .user-container input {
            margin-right: 15px;
        }

        .create-group-btn {
            text-align: center;
            margin-top: 40px;
            display: inline-block;
            width: 180px;
            padding: 12px 18px;
            background-color: #2A3663; 
            color: white;
            border-radius: 5px;
            border: none;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s;
        }

        .create-group-btn:hover {
            background-color: #4A628A; 
        }

        .back-btn {
            margin: 20px 0;
            padding: 12px 20px;
            background-color: #2A3663; 
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            text-align: center;
        }

        .back-btn a {
            text-decoration: none;
            color: white;
        }

        .back-btn:hover {
            background-color: #4A628A;
        }

        .no-users-message {
            color: #E38E49;
            font-weight: bold;
            text-align: center;
            margin-top: 30px;
        }

        .background-gradient {
            background: linear-gradient(135deg, #E8BCB9, #F5EFE7);
            height: 100vh;
            position: absolute;
            width: 100%;
            z-index: -1;
        }

        form {
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 80%;
            margin: 0 auto;
        }

        input[type="text"] {
            width: 50%;
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 5px;
            border: 1px solid #ddd;
            font-size: 16px;
        }

        label {
            font-size: 16px;
            font-weight: bold;
            color: #2A3663; 
            margin-bottom: 10px;
        }
		</style>
	</head>
	
	<body>
		<h2>Select Users for Group Chat</h2>
		
		<form action="CreateGroupChatServlet" method="post">
			<%
            Map<String, String> users = (Map<String, String>) request.getSession().getAttribute("users");
            String email = (String) request.getSession().getAttribute("email");

            if (users != null && !users.isEmpty()) {
                for (Map.Entry<String, String> user : users.entrySet()) {
                    if (user.getValue().equals(email)) continue; 
        %>
		
		<div class="user-container">
                <p><%= user.getKey() %> (<%= user.getValue() %>)</p>
                <input type="checkbox" name="selectedUsers" value="<%= user.getValue() %>">
          </div>
		  
		<%
                }
            }
        %>
		
		<label for="groupName">Group name:</label>
		<input type="text" name="groupName" id="groupName" required>
		
		<div style="text-align: center;">
            <button type="submit" class="create-group-btn">Create Group</button>
        </div>
			
		</form>
	</body>
</html>
