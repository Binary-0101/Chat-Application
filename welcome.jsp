<%@ page import="java.util.*" %>
<%@ page import="io.lettuce.core.RedisClient" %>
<%@ page import="io.lettuce.core.api.sync.RedisCommands" %>
<%@ page import="io.lettuce.core.api.StatefulRedisConnection" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    session.setAttribute("recipient", null);
    session.setAttribute("groupId", null);
%>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome</title>
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

        nav {
            background-color: linear-gradient(135deg, #E8BCB9, #F5EFE7);
            width: 100%;
            padding: 20px 0;
            position: fixed;
            top: 0;
            left: 0;
            z-index: 100;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        nav ul {
            list-style: none;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            gap: 20px;
        }

        nav ul li {
            display: inline;
        }

        nav ul li a {
            color: #2A3663;
            text-decoration: none;
            font-size: 16px;
            padding: 8px 15px;
            border-radius: 5px;
            transition: background-color 0.3s;
        }

        nav ul li a:hover {
            background-color: #F5EFE7;
        }

        .container {
            width: 100%;
            max-width: 1100px;
            margin-top: 80px;
            padding: 20px;
        }

        h2 {
            text-align: center;
            color: #2A3663;
            margin-bottom: 30px;
            font-size: 26px;
        }

        .user-container {
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

        .user-container:hover {
            transform: scale(1.02);
        }

        .user-container p {
            margin-right: 15px;
            font-size: 16px;
            color: #555;
            font-weight: bold;
        }

        .user-container form button {
            padding: 10px 15px;
            background-color: #2A3663;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s, transform 0.2s;
        }

        .user-container form button:hover {
            background-color: #4A628A;
            transform: translateY(-2px);
        }

        .user-container form button:active {
            transform: translateY(0);
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

    .group-container form button {
        padding: 10px 15px;
        background-color: #2A3663;
        color: white;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        font-size: 16px;
        transition: background-color 0.3s, transform 0.2s;
    }

    .group-container form button:hover {
        background-color: #4A628A;
        transform: translateY(-2px);
    }

    .group-container form button:active {
        transform: translateY(0);
    }
	
	#successMessage {
            display: none;
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border-radius: 5px;
            font-size: 16px;
            z-index: 1000;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
    </style>
</head>
<body>
    <div id="message"></div>
    <div class="background-gradient"></div>

    <nav>
        <ul>
            <li><a href="createGroup.jsp">Create Group Chat</a></li>
            <li>
				<a href="notification.jsp">
					Notifications  <span id="notification-count" style="color: red; font-weight: bold;">0</span>
				</a>
			</li>
            <li><a href="SignOutServlet">Sign Out</a></li>
        </ul>
    </nav>
	
	 <div id="successMessage"></div>

    <div class="container">
        <h2>Start Chat</h2>

        <%
            String email = (String) request.getSession().getAttribute("email");
            Map<String, String> users = (Map<String, String>) request.getSession().getAttribute("users");

            if (users != null && !users.isEmpty()) {
                for (Map.Entry<String, String> user : users.entrySet()) {
                    if (user.getValue().equals(email)) continue;
        %>

        <div class="user-container">
            <p><%= user.getKey() %> (<%= user.getValue() %>)</p>
            <form action="startchat.jsp" method="post">
                <input type="hidden" name="recipient" value="<%= user.getValue() %>">
                <button type="submit">Chat</button>
            </form>
        </div>

        <%
                }
            } else {
        %>
            <p class="no-users-message">No users found to chat with.</p>
        <%
            }
        %>

        <div id="group-container"></div>
    </div>
	
	<script>
	
		  let lastNotificationCount = 0;
		
			function fetchNotificationCount() {
				$.ajax({
				url: 'NotificationServlet',
				method: 'GET',
				data: { action: 'fetchCount' },
				dataType: 'json',
				success: function (data) {
					const unreadCount = data.unreadCount;
					const notificationBadge = document.getElementById("notification-count");
					if (unreadCount > 0) {
						notificationBadge.textContent = unreadCount;
						notificationBadge.style.display = "block";
					} else {
						notificationBadge.style.display = "none";
					}
					if (unreadCount > lastNotificationCount) {
						//const notificationSound = new Audio('notification.mp3');
						//notificationSound.play();
				    }
				lastNotificationCount = unreadCount;
			},
			error: function (err) {
				console.error('Error fetching notification count:', err);
			}
		});
	 }
		
	setInterval(fetchNotificationCount, 1000);
		
		$(document).ready(function () {
			let displayedGroups = new Set(); 

			function fetchGroups() {
				$.ajax({
					url: 'FetchGroupsServlet',
					method: 'GET',
					dataType: 'json',
					success: function (groups) {
						console.log("Groups fetched from backend:", groups);
						const groupsContainer = $("#group-container");

						const noGroupsMessage = groupsContainer.find('.no-users-message');
						if (noGroupsMessage.length > 0 && groups.length > 0) {
							noGroupsMessage.remove();
						}

						if (groups.length > 0 && !groupsContainer.find('h2').length) {
							groupsContainer.prepend('<h2>Your Groups</h2>');
						}

						groups.forEach(group => {
							console.log("Processing group:", group);
							console.log("group-id", group.groupId);
							console.log("group-name", group.groupName);
							if (!displayedGroups.has(group.groupId)) { 
								displayedGroups.add(group.groupId); 
								const groupName = group.groupName;
								const groupHtml = 
									'<div class="group-container">' +
										'<p>' + group.groupName + '</p>' +
										'<form action="startchat.jsp" method="post">' +
											'<input type="hidden" name="groupId" value="' +  group.groupId + '">' +
											'<button type="submit">Chat</button>' +
										'</form>' +
									'</div>'; 
									
								if (!groupsContainer.find('[data-group-id="' + group.groupId + '"]').length) {
									groupsContainer.append(groupHtml);
								}
							}
						});

						if (displayedGroups.size === 0 && !groupsContainer.find('.no-users-message').length) {
							groupsContainer.html('<p class="no-users-message">No groups found.</p>');
						}

						if (displayedGroups.size === 0 && groupsContainer.find('h2').length) {
							groupsContainer.find('h2').remove();
						}
					},
					error: function (xhr, status, error) {
						console.error("Failed to fetch groups:", error, xhr.responseText); 
					}
				});
			}
			setInterval(fetchGroups, 500);
		});
</script>
</body>
</html>
