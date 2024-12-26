
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="java.util.*" %>
<%@ page import="io.lettuce.core.RedisClient" %>
<%@ page import="io.lettuce.core.api.sync.RedisCommands" %>
<%@ page import="io.lettuce.core.api.StatefulRedisConnection" %>
<%
		String recipient = request.getParameter("recipient");
		if (recipient != null) {
			request.getSession().setAttribute("recipient", recipient);
		}
	
		String groupId = request.getParameter("groupId");
		if (groupId != null) {
			session.setAttribute("groupId", groupId);
		}
			
		RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> redisCommands = connection.sync();
	
		 String groupName = groupId != null ? groupId.split(":")[1] : "";
		 List<String> groupMembers = groupId != null ? redisCommands.lrange(groupId, 0, -1) : new ArrayList<>();
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
    <title>Start Chat</title>
	<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Varela Round', sans-serif; /* Consistent font style */
            background: white; /* Gradient background matching the first code */
            color: #333;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            padding: 20px;
        }

        .chat-container {
            width: 80%;
            max-width: 1100px;
            margin: 30px auto;
            background:  linear-gradient(135deg, #E8BCB9, #F5EFE7); /* Semi-transparent white background */
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .chat-header {
            font-size: 1.5rem;
			color: #2A3663;/* White color for header */
            margin-bottom: 20px;
        }

        .messages {
            height: 300px;
            overflow-y: auto;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 10px;
            background:  linear-gradient(135deg, #E8BCB9, #F5EFE7); /* Slightly transparent background for messages */
        }

        .sent {
            text-align: right;
            background-color: rgba(255, 255, 255, 0.7);
            color: #000;
            border-radius: 15px 15px 0 15px;
            margin-left: auto;
            padding: 10px 15px;
            margin-bottom: 10px;
            max-width: 80%;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .received {
            text-align: left;
            background-color: rgba(255, 255, 255, 0.7);
            color: #000;
            border-radius: 15px 15px 15px 0;
            margin-right: auto;
            padding: 10px 15px;
            margin-bottom: 10px;
            max-width: 80%;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .chat-input {
            margin-top: 20px;
            display: flex;
        }

        .chat-input input {
            width: 85%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }

        .chat-input button {
            width: 15%;
            padding: 10px;
            border: none;
            background-color: #2A3663;
            color: white;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        .chat-input button:hover {
            background-color: #4A628A; 
        }

        #uploadForm {
            margin-top: 10px;
        }

        #uploadForm input[type="file"] {
            display: inline-block;
            margin-right: 10px;
        }

        #uploadForm button {
            background-color: #2A3663;
            padding: 10px;
            color: white;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        #uploadForm button:hover {
            background-color: #4A628A; 
        }

        /* Exit Group Button */
        button {
            padding: 10px;
            background-color: #2A3663;
            color: white;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        button:hover {
            background-color: #4A628A;
        }

        .back-btn {
            margin-top: 20px;
        }

        #onlineStatus {
            font-size: 0.9em;
            color: green; /* Online status in green */
        }

        .background-gradient {
            background:  linear-gradient(135deg, #E8BCB9, #F5EFE7);
            height: 100vh;
            position: absolute;
            width: 100%;
            z-index: -1;
        }
		
		.status {
			font-size: 14px; 
			color: gray; 
			vertical-align: middle; 
		}
    </style>
</head>
<body>
    <div class="chat-container">
        <% if (groupId != null) { %>
            <div class="chat-header">Group: <%= groupName %> </div>
            <span id="onlineStatus" style="font-size: 0.9em; color: green;"></span>
        <% } else { %>
            <div class="chat-header">Chat with <%= recipient %></div>
            <span id="onlineStatus" style="font-size: 0.9em; color: green;"></span>
        <% } %>
        
        <div class="messages" id="chatMessages"></div>
        
        <form class="chat-input" id="chatForm" onsubmit="return false;">
            <input type="text" id="messageText" placeholder="Type your message..." />
            <button type="button" onclick="sendMessage()">Send</button>
        </form>
        
        <form id="uploadForm" enctype="multipart/form-data">
            <input type="file" name="attachment" id="attachment" />
            <button type="button" onclick="sendMessage()">Upload</button>
        </form>
        
		 <% if (groupId != null) { %>
        <button onclick="leaveGroup('<%= groupId %>', '<%= groupName %>')">Exit Group</button>
		 <% }
        %>
		
        <div class="back-btn">
            <button onclick="window.location.href='welcome.jsp'">Back</button>
        </div>
    </div>
	<script>
		const recipient = "<%= request.getSession().getAttribute("recipient") %>";
		const groupId = "<%= request.getSession().getAttribute("groupId") %>";
		
		console.log(recipient);
		console.log(groupId);
		
		const checkOnlineStatus = async () => {
            try {
				console.log("Checking online status for: " + recipient);
                const response = await fetch(`checkOnlineStatus?recipient=${recipient}`);
                if (response.ok) {
                    const statusInfo = await response.text(); 
					const isOnline = statusInfo.trim();
                    const statusElement = document.getElementById("onlineStatus");
            
				if (isOnline === "online") {
					statusElement.textContent = "Online";
					statusElement.style.color = "green";
				} else {
					statusElement.textContent = isOnline === '' ? "Offline" : isOnline;
					statusElement.style.color = "black";
				}
				}
				} catch (error) {
					console.error("Error fetching online status:", error);
				}
        };
		
		const messagesContainer = document.getElementById("chatMessages");
		let lastFetchedTime = null;
		
        const fetchMessages = async () => {
			
            try {
				let url;
				
				if (recipient && recipient.trim() !== "") {
					url = `fetchMessages?recipient=${recipient}&lastFetchedTime=`+ lastFetchedTime;
					console.log("url ", url);
				} else {
					url = `fetchMessages?groupId=${groupId}&lastFetchedTime=` + lastFetchedTime;
				}

                const response = await fetch(url);
                if (!response.ok) {
                    console.error("Failed to fetch messages");
                    return;
                }

                const messages = await response.json();
				const existingElements = new Map();
				
				document.querySelectorAll("#chatMessages > div[data-id]").forEach(el => {
					existingElements.set(el.getAttribute("data-id"), el);
				});

				const newDisplayedMessageIds = new Set();
								
                messages.forEach(msg => {
					const uniqueId = msg.attachmentId || msg.messageId;
					newDisplayedMessageIds.add(uniqueId);
					
					const existingMessageElement = existingElements.get(uniqueId);
					
					//console.log("Debug Info:", {
							//newDisplayedMessageIds: Array.from(newDisplayedMessageIds),
							//existingMessageElement
						//});
					
					if(existingMessageElement) {
						const contentElement = existingMessageElement.querySelector(".content");
						const statusElement = existingMessageElement.querySelector(".status");
						
						//console.log("contentElement.textContent ", contentElement.textContent);
						//console.log("msg.text ", msg.text);
						
						if (!msg.text.includes("[attachment]") && contentElement && contentElement.textContent !== msg.sender + ":" + msg.text + ":" + msg.timestamp) {
							console.log("eidted");
							contentElement.textContent = msg.sender + ":" + msg.text + ":" + msg.timestamp; // Update message content
						}

						if (statusElement) {
							const status = msg.readStatus === "read" ? '✔️✔️' : '✔️';
							if (statusElement.textContent !== status) {
								statusElement.textContent = status; // Update read status
							}
						}
					}
					
					
			else {
                    const messageDiv = document.createElement("div");
                    messageDiv.className = msg.type === "sent" ? "sent" : "received";
					messageDiv.setAttribute("data-id", uniqueId);
					
					if (msg.text.includes("[attachment]")) {
						console.log("msgtext" + msg.text);
						const attachmentMatch = msg.text.match(/\[attachment\](.*?)\|(.*?)\|(.*?)\|(.*?)$/);
						console.log("attachmentMatch: ", attachmentMatch);
						 if(attachmentMatch) {
							const filePath = attachmentMatch[1];          // File path (e.g., URL or local path)
							const fileName = attachmentMatch[2];          // Original file name
							const mimeType = attachmentMatch[3];          // MIME type (image/video type)
							const attachmentId = attachmentMatch[4];      // Attachment ID
							
							console.log("fuck me", attachmentId);
							if(!attachmentId || attachmentId === "null") return; 
							console.log("File Path:", attachmentMatch[0]);
							console.log("File Path:", attachmentMatch[1]);
							console.log("File Name:", attachmentMatch[2]);
							console.log("Mime Type:", attachmentMatch[3]);
							console.log("Attachment ID:", attachmentMatch[4]);
						  
						  if(mimeType.startsWith("image")) {
							  console.log("fuck you");
							const imageContainer = document.createElement("div");
						
							const image = document.createElement("img");
							image.src = `AttachmentServlet?attachmentId=`+attachmentId;
							image.alt = "fuck";
							image.style.maxWidth = "200px";
							image.style.maxHeight = "200px";

							messageDiv.appendChild(image);
						
					} else if (mimeType.startsWith('video')) {
						console.log("msgtext fuck" + msg.text);
						console.log("vidoe");
						const video = document.createElement("video");
						video.src = `AttachmentServlet?attachmentId=`+attachmentId;
						video.controls = true;
						video.style.maxWidth = "100%"; 
						video.style.height = "300px";
						 
						messageDiv.appendChild(video);
						
					} else {
						const fileCard = document.createElement("div");
							fileCard.style.display = "flex";
							fileCard.style.alignItems = "center";
							fileCard.style.border = "1px solid #ccc";
							fileCard.style.borderRadius = "8px";
							fileCard.style.padding = "8px";
							fileCard.style.margin = "8px 0";
							fileCard.style.backgroundColor = "#f9f9f9";
							fileCard.style.cursor = "pointer";

							const fileIcon = document.createElement("img");
							fileIcon.src = "file-icon.png"; 
							fileIcon.alt = "File Icon";
							fileIcon.style.width = "40px";
							fileIcon.style.height = "40px";
							fileIcon.style.marginRight = "10px";

							const fileNameSpan = document.createElement("span");
							fileNameSpan.textContent = fileName;
							fileNameSpan.style.fontWeight = "bold";

							fileCard.addEventListener("click", () => {
								window.open(`AttachmentServlet?attachmentId=`+attachmentId, "_blank");
							});

							fileCard.appendChild(fileIcon);
							fileCard.appendChild(fileNameSpan);
							messageDiv.appendChild(fileCard);
						}
					} } else if(msg.text !== "null"){
						const status = msg.readStatus === "read" ? '✔️✔️' : '✔️';

                const messageContent = document.createElement("div");
				messageContent.className = "content";
                messageContent.textContent = msg.sender + ":" + msg.text + ":" + msg.timestamp;
                messageContent.style.display = "inline-block"; // Ensures the content and status are on the same line
                messageContent.style.marginRight = "8px"; // Add spacing between text and status

             
                messageDiv.appendChild(messageContent);
				}
				
				const statusSpan = document.createElement("span");
                statusSpan.className = "status";
                statusSpan.textContent = status;
				                messageDiv.appendChild(statusSpan);

				
				if(msg.type === "sent" && msg.text !== "null" && !msg.text.includes("[attachment]")) {
					const editButton = document.createElement("button");
					editButton.textContent = "Edit";
					editButton.onclick = () => editMessage(groupId, msg.messageId, msg.text, msg.sender);

					messageDiv.appendChild(editButton);
				}
				const deleteButton = document.createElement("button");
				deleteButton.textContent = "Delete";
				deleteButton.onclick = () => deleteMessage(msg.messageId, msg.attachmentId);
				
				messageDiv.appendChild(deleteButton);
				messagesContainer.appendChild(messageDiv);
					
				if (msg.type === "received" && msg.readStatus === "unread") {
					if(msg.messageId && msg.messageId !== null) {
						markMessageAsRead(msg.messageId, null);
					}
					else {
						console.log("id", msg.attachmentId);
						markMessageAsRead(null, msg.attachmentId);
					}
				}
				
			}
				const messageTimestamp = msg.timestamp;
				if(!lastFetchedTime || new Date(messageTimestamp) > new Date(lastFetchedTime)) {
					lastFetchedTime = messageTimestamp;
					console.log("lastFetchedTime", lastFetchedTime);
				}
			});
			
			existingElements.forEach((el, uniqueId) => {
				if (!newDisplayedMessageIds.has(uniqueId)) {
					el.remove();
				}
			});
			
			displayedMessageIds = newDisplayedMessageIds;
		
            } catch (error) {
                console.error("Error fetching messages:", error);
            }
        };
		
		const sendMessage = async () => {
			const messageText = document.getElementById("messageText").value.trim();
			const attachment = document.getElementById("attachment").files[0];

			if (!messageText && !attachment) return;

			try {
				const formData = new FormData();

        if (recipient && recipient.trim() !== "" && recipient !== "null") {
            formData.append("recipient", recipient);

            if (messageText) {
                formData.append("text", messageText);
            }

            if (attachment) {
                formData.append("attachment", attachment);
            }
			
			console.log("calling sendMessage", recipient);

            const response = await fetch("sendMessage", {
                method: "POST",
                body: formData,
            });

            if (!response.ok) {
                console.error("Failed to send message");
                return;
            }
        } else if (groupId && groupId.trim() !== "") {
            formData.append("groupId", groupId);

            if (messageText) {
                formData.append("text", messageText);
            }

            if (attachment) {
                formData.append("attachment", attachment);
            }

            const response = await fetch("sendGroupMessage", {
                method: "POST",
                body: formData,
            });

            if (!response.ok) {
                console.error("Failed to send group message");
                return;
            }
        }
		
		console.log(formData);

        document.getElementById("messageText").value = "";
        document.getElementById("attachment").value = "";
        fetchMessages();
    } catch (error) {
        console.error("Error sending message:", error);
    }
};

const editMessage = (groupId, messageId, text, sender) => {
    const newText = prompt("Edit your message:", text);
    if (newText) {
        fetch("editMessage", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ groupId, recipient, messageId, newText }),
        })
            .then((response) => {
                if (response.ok) {
					fetchMessages();
                } else {
                    console.error("Failed to edit message on server");
                }
            })
            .catch((error) => console.error("Error editing message:", error));
    }
};


        const deleteMessage = (messageId, attachmentId) => {
            if (confirm("Are you sure you want to delete this message?")) {
                fetch("deleteMessage", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ groupId, recipient, messageId, attachmentId }),
                })
				.then((response) => {
						if (response.ok) {
							fetchMessages();
						}
						else {
							console.error("Failed to delete message");
						}
					})
					.catch((error) => console.error("Error deleting message:", error));
				}
        };

        const markMessageAsRead = async (messageId, attachmentId) => {
            try {
                const response = await fetch("markAsRead", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ recipient, messageId, attachmentId })
                });
			if (!response.ok) {
            console.error("Failed to mark message as read");
        } else {
            console.log("Message marked as read");
        }
            } catch (error) {
                console.error("Error marking message as read:", error);
            }
        };
		
		const leaveGroup = async(groupId, groupName) => {
			try {
				const response = await fetch("ExitGroupServlet", {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify( {groupId, groupName} )
				});
				
				if(!response.ok) {
					console.error("Failed to leave the group");
				} else {
					console.log("Left the group successfully");
				}
			} catch (error) {
				console.error("Error leaving the group:", error);
			}
		};
		
		if (recipient !== null && recipient !== "null") {
			setInterval(checkOnlineStatus, 2000);
		} else {
			console.log("Recipient is null, skipping online status check.");
		}
		
		setInterval(fetchMessages, 5000);
		
		window.addEventListener("unload", () => {
			navigator.sendBeacon("SignOutServlet");
		});
    </script>
</body>
</html>