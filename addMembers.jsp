<%@ page import="java.util.*" %>
<%@ page import="io.lettuce.core.RedisClient" %>
<%@ page import="io.lettuce.core.api.sync.RedisCommands" %>
<%@ page import="io.lettuce.core.api.StatefulRedisConnection" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Members</title>
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

        .add-members-btn {
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

        .add-members-btn:hover {
            background-color: #4A628A; 
        }

        .background-gradient {
            background: linear-gradient(135deg, #E8BCB9, #F5EFE7);
            height: 100vh;
            position: absolute;
            width: 100%;
            z-index: -1;
        }

        .no-members-message {
            color: #E38E49;
            font-weight: bold;
            text-align: center;
            margin-top: 30px;
        }

        form {
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 80%;
            margin: 0 auto;
        }
    </style>
</head>
<body>
    <div class="background-gradient"></div>
    <h2>Add Members to Group</h2>
    <form action="AddMembersToGroupServlet" method="post">
        <%
            Map<String, String> nonGroupUsers = (Map<String, String>) request.getAttribute("nonGroupUsers");
            String groupId = (String) request.getAttribute("groupId");

            if (nonGroupUsers != null && !nonGroupUsers.isEmpty()) {
                for (Map.Entry<String, String> user : nonGroupUsers.entrySet()) {
        %>
        <div class="user-container">
            <p><%= user.getKey() %> (<%= user.getValue() %>)</p>
            <input type="checkbox" name="selectedUsers" value="<%= user.getValue() %>">
        </div>
        <%
                }
            } else {
        %>
        <p class="no-members-message">No users available to add to this group.</p>
        <%
            }
        %>
        <input type="hidden" name="groupId" value="<%= groupId %>">
        <div style="text-align: center;">
            <button type="submit" class="add-members-btn">Add Selected Members</button>
        </div>
    </form>
</body>
</html>
