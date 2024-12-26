<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String email = (String) request.getSession().getAttribute("email");
    if (email == null) {
        response.sendRedirect("signin.jsp");
        return;
    }

    List<String[]> notifications = (List<String[]>) request.getAttribute("notifications");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications</title>
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

        .notification-container {
            width: 80%;
            margin: 30px auto;
            background: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .notification-item {
            padding: 10px;
            border-bottom: 1px solid #ddd;
            cursor: pointer;
            border-radius: 5px;
            margin-bottom: 5px;
        }

        .notification-item:hover {
            background-color: #f9f9f9;
        }

        .notification-item strong {
            color: #2A3663; 
            font-weight: bold;
        }

        .back-btn {
            margin: 20px;
            padding: 12px 20px;
            background-color: #2A3663; 
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
        }

        .back-btn a {
            text-decoration: none;
            color: white;
        }

        .back-btn:hover {
            background-color: #4A628A;
        }

        .no-notifications {
            text-align: center;
            color: #888;
        }

        .background-gradient {
            background:  linear-gradient(135deg, #E8BCB9, #F5EFE7); 
            height: 100vh;
            position: absolute;
            width: 100%;
            z-index: -1;
        }
    </style>
</head>
<body>
    <div class="background-gradient"></div> 

    <div class="notification-container">
        <h2>Notifications</h2>
        <% 
        if (notifications != null && !notifications.isEmpty()) {
            for (String[] notification : notifications) {
				String messageId = "", sender = "", message = "", groupName = "", groupId = "";
				if(notification.length == 3) {
					System.out.println("qqqq "+Arrays.toString(notification));
					messageId = notification[0];
					sender = notification[1];
					message = notification[2];
        %>
        <div class="notification-item" onclick="markAsReadAndOpenChat('<%= sender %>', '<%= messageId %>')">
            <strong><%= sender %></strong>: <%= message %>
        </div>
        <% 
            } else {
				 groupName = notification[0];
				 groupId = notification[0] + ":" + notification[1];
				 messageId = notification[2];
				 sender = notification[3];
				 message = notification[4];
		%>
		 <div class="notification-item" onclick="markAsReadAndOpenChat('<%= sender %>' , '<%= messageId%>', '<%= groupId %>')">
            <strong><%= groupName %></strong>: <%= sender %> : <%= message %>
        </div>
		<%
			   }
			}
        } else {
        %>
        <p class="no-notifications">No notifications.</p>
        <% 
        }
        %>
		 <button class="back-btn">
        <a href="welcome.jsp">Back</a>
    </button> 
    </div>

    <script>
        function markAsReadAndOpenChat(sender, messageId, groupId) {
			console.log(groupId);
			
			if(!groupId) {
				console.log("markasread called");
				fetch("markAsRead", {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ recipient: sender, messageId })
				}).catch(err => {
					console.error("Error marking as read:", err);
				});
			} else {
				console.log("removeGroupNotification called");
				console.log(groupId, "hkjd" + messageId);
					fetch("removeGroupNotification", {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ groupId, messageId })
				}).catch(err => {
					console.error("Error removing group notification:", err);
				});
				window.location.href = "startchat.jsp?groupId=" + "group:" + groupId;
				return;
			}
			window.location.href = "startchat.jsp?recipient=" + sender;
        }
    </script>
</body>
</html>
