<%@ page import="java.util.*, io.lettuce.core.RedisClient, io.lettuce.core.api.sync.RedisCommands, io.lettuce.core.api.StatefulRedisConnection" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Group Details</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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
            width: 100%;
            max-width: 1100px;
            margin-top: 20px;
            padding: 20px;
        }

        h2 {
            text-align: center;
            color: #2A3663;
            margin-bottom: 30px;
            font-size: 26px;
        }

        .group-container {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin: 15px auto;
            padding: 12px 20px;
            background-color: #fff;
            border-radius: 8px;
            border: 1px solid #ddd;
            width: 80%;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s;
        }

        .group-container:hover {
            transform: scale(1.02);
        }

        .group-container p {
            margin-right: 15px;
            font-size: 16px;
            color: #555;
            font-weight: bold;
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
    </style>
</head>
<body>
    <div id="message"></div>
    <div class="background-gradient"></div>

    <div class="container">
        <h2>Group Members</h2>

        <%
            String groupId = request.getParameter("groupId");
            if (groupId != null) {
                RedisClient redisClient = RedisClient.create("redis://localhost:6379");
                try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
                    RedisCommands<String, String> syncCommands = connection.sync();

                    List<String> groupMembers = syncCommands.lrange(groupId, 0, -1);

                    if (groupMembers != null && !groupMembers.isEmpty()) {
                        for (String member : groupMembers) {
        %>

        <div class="group-container">
            <p><%= member %></p>
        </div>

        <%
                        }
                    } else {
        %>
                    <p class="no-users-message">No members found in this group.</p>
        <%
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    out.println("<p>Error fetching group details.</p>");
                }
            } else {
        %>
                <p class="no-users-message">Invalid group ID.</p>
        <%
            }
        %>

    </div>
</body>
</html>
