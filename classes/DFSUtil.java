import java.io.*;
import java.util.*;
import java.nio.file.*;

public class DFSUtil {
    private static final String DFS_GROUP_MEMBERS = "local/dfs/chat/group_members/";
    private static final String DFS_ONE_ONE_MESSAGES = "local/dfs/chat/one_one_messages/";
    private static final String DFS_GROUP_MESSAGES = "local/dfs/chat/group_messages/";
    private static final String DFS_ATTACHMENTS = "local/dfs/chat/attachments/";
	private static final String DFS_NOTIFICATIONS = "local/dfs/chat/notifications/";
	private static final String DFS_ONLINE_STATUS = "local/dfs/chat/online_status/";
    private static final int NUM_SHARDS = 3; 
    private static final int REPLICATION_FACTOR = 3;

    static {
        try {
			System.out.println("Static block executed");
            for (int i = 0; i < NUM_SHARDS; i++) {
                createDirectoryIfNotExists(DFS_GROUP_MEMBERS + "shard" + i);
                createDirectoryIfNotExists(DFS_ONE_ONE_MESSAGES + "shard" + i);
                createDirectoryIfNotExists(DFS_GROUP_MESSAGES + "shard" + i);
                createDirectoryIfNotExists(DFS_ATTACHMENTS + "shard" + i);
                createDirectoryIfNotExists(DFS_ONLINE_STATUS + "shard" + i);
                createDirectoryIfNotExists(DFS_NOTIFICATIONS + "shard" + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private static void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
		System.out.println("Trying to create directory: " + dir.getAbsolutePath());
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory: " + path);
            }
        }
    }

    private static int getShardIndex(String key) {
        return Math.abs(key.hashCode()) % NUM_SHARDS;
    }

    private static List<Integer> getReplicationShards(String key) {
        List<Integer> shards = new ArrayList<>();
        int primaryShard = getShardIndex(key);
        shards.add(primaryShard);

        for (int i = 1; i < REPLICATION_FACTOR; i++) {
            int replicaShard = (primaryShard + i) % NUM_SHARDS;
            shards.add(replicaShard);
        }
        return shards;
    }
	
	public static void storeNotifications(String notificationContent, String recipient) {
		List<Integer> shards = getReplicationShards(recipient);

		for (int shard : shards) {
			String filePath = DFS_NOTIFICATIONS + "shard" + shard + "/" + recipient + ".txt";
			writeFile(filePath, notificationContent);
		}
	}

	public static void storeOnlineStatus(String email, String status) {
		String statusContent = "User:" + email + ":Status:" + status;
		List<Integer> shards = getReplicationShards(email);
		
		for (int shard : shards) {
			String filePath = DFS_ONLINE_STATUS + "shard" + shard + "/" + email + "-status.txt";
			writeFile(filePath, statusContent);
		}
	}
	
	public static String fetchOnlineStatus(String email) {
		List<String> statuses = new ArrayList<>();
		List<Integer> shards = getReplicationShards(email);
		
		int primaryShard = shards.get(0);
		String primaryPath = DFS_ONLINE_STATUS + "shard" + primaryShard + "/" + email + "-status.txt";
		
		if (readMessagesFromFile(primaryPath, statuses)) {
			return statuses.isEmpty() ? null : statuses.get(0); 
		}
		
		for (int i = 1; i < shards.size(); i++) {
			String replicaPath = DFS_ONLINE_STATUS + "shard" + shards.get(i) + "/" + email + "-status.txt";
			if (readMessagesFromFile(replicaPath, statuses)) {
				return statuses.isEmpty() ? null : statuses.get(0); 
			}
		}
		return null;
	}
	
	public static void updateOnlineStatus(String email, String status) {
		List<Integer> shards = getReplicationShards(email);

		for (int shard : shards) {
			String filePath = DFS_ONLINE_STATUS + "shard" + shard + "/" + email + "-status.txt";
			try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
				String line;
				long pointer = 0;
				boolean updated = false;

				while ((line = raf.readLine()) != null) {
					if (line.startsWith("User:" + email + ":Status:")) {
						long currentPos = pointer;
						raf.seek(currentPos);
						String updatedStatus = "User:" + email + ":Status:" + status;
						raf.writeBytes(updatedStatus + System.lineSeparator());
						updated = true;
						break;
					}
					pointer = raf.getFilePointer();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	}
	
    public static void storeGroup(String redisGroupKey, String groupName, List<String> groupMembers) {
		System.out.println("came for store group");
        String groupContent = "Group Key: " + redisGroupKey + "\n" + String.join("\n", groupMembers);
        List<Integer> shards = getReplicationShards(groupName);

        for (int shard : shards) {
            String filePath = DFS_GROUP_MEMBERS + "shard" + shard + "/" + groupName + ".txt";
            writeFileForGroup(filePath, groupContent);
        }
    }
	
	public static void writeFileForGroup(String filePath, String content) {
		try (FileWriter writer = new FileWriter(filePath, false)) {
			writer.write(content);
		} catch (IOException e) {
			System.err.println("Error writing to file: " + filePath);
			e.printStackTrace();
		}
	}
	
	public static List<String> fetchNotifications(String email) {
		List<String> notifications = new ArrayList<>();
		List<Integer> shards = getReplicationShards(email);

		int primaryShard = shards.get(0);
		String primaryPath = DFS_NOTIFICATIONS + "shard" + primaryShard + "/" + email + "-notifications.txt";
		if (readMessagesFromFile(primaryPath, notifications)) {
			return notifications;
		}

		for (int i = 1; i < shards.size(); i++) {
			String replicaPath = DFS_NOTIFICATIONS + "shard" + shards.get(i) + "/" + email + "-notifications.txt";
			if (readMessagesFromFile(replicaPath, notifications)) {
				break;
			}
		}
		return notifications;
	}

    public static void storeGroupMessage(String groupName, String messageId, String sender, String text, String readStatus) {
		try {
			String messageContent = messageId + ": " + sender + ": " + text + ": " + readStatus;
			String encryptedMessage = EncryptionUtil.encrypt(messageContent);
			List<Integer> shards = getReplicationShards(groupName);

			for (int shard : shards) {
				String filePath = DFS_GROUP_MESSAGES + "shard" + shard + "/" + groupName + "-messages.txt";
				writeFile(filePath, encryptedMessage);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	public static void storeGroupMessage(String sender, String groupName, String attachmentContent) {
		try {
			String encryptedMessage = EncryptionUtil.encrypt(attachmentContent);
			List<Integer> shards = getReplicationShards(groupName);

			for (int shard : shards) {
				String filePath = DFS_GROUP_MESSAGES + "shard" + shard + "/" + groupName + "-messages.txt";
				writeFile(filePath, encryptedMessage);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

    public static List<String> fetchGroupMessages(String groupName) {
        List<String> messages = new ArrayList<>();
		try {
			List<Integer> shards = getReplicationShards(groupName);

			int primaryShard = shards.get(0);
			String primaryPath = DFS_GROUP_MESSAGES + "shard" + primaryShard + "/" + groupName + "-messages.txt";
			if (readMessagesFromFile(primaryPath, messages)) {
				for (int i = 0; i < messages.size(); i++) {
					messages.set(i, EncryptionUtil.decrypt(messages.get(i)));
				}
				return messages; 
			}

			for (int i = 1; i < shards.size(); i++) {
				String replicaPath = DFS_GROUP_MESSAGES + "shard" + shards.get(i) + "/" + groupName + "-messages.txt";
				if (readMessagesFromFile(replicaPath, messages)) {
					for (int j = 0; j < messages.size(); j++) {
						messages.set(j, EncryptionUtil.decrypt(messages.get(j)));
					}
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return messages;
    }

    public static void storeMessage(String messageId, String sender, String recipient, String text, String readStatus) {
		try {
			String fileName = generateFileName(sender, recipient);
			String messageContent = messageId + ": " + sender + ": " + text + ": " + readStatus;
			String encryptedMessage = EncryptionUtil.encrypt(messageContent);
			List<Integer> shards = getReplicationShards(fileName);

			for (int shard : shards) {
				String filePath = DFS_ONE_ONE_MESSAGES + "shard" + shard + "/" + fileName + ".txt";
				writeFile(filePath, encryptedMessage);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	public static void storeMessage(String sender, String recipient, String attachmentContent) {
		try {
			String fileName = generateFileName(sender, recipient);
			String encryptedMessage = EncryptionUtil.encrypt(attachmentContent);
			List<Integer> shards = getReplicationShards(fileName);

			for (int shard : shards) {
				String filePath = DFS_ONE_ONE_MESSAGES + "shard" + shard + "/" + fileName + ".txt";
				writeFile(filePath, encryptedMessage);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

    public static List<String> fetchMessages(String sender, String recipient) {
		List<String> messages = new ArrayList<>();
		try {
			String fileName = generateFileName(sender, recipient);
			List<Integer> shards = getReplicationShards(fileName);

			int primaryShard = shards.get(0);
			int count = 0;
			String primaryPath = DFS_ONE_ONE_MESSAGES + "shard" + primaryShard + "/" + fileName + ".txt";
			if (readMessagesFromFile(primaryPath, messages)) {
				for (int i = 0; i < messages.size(); i++) {
					String message = messages.get(i);
					messages.set(i, EncryptionUtil.decrypt(messages.get(i)));
				}
				return messages; 
			}

			for (int i = 1; i < shards.size(); i++) {
				String replicaPath = DFS_ONE_ONE_MESSAGES + "shard" + shards.get(i) + "/" + fileName + ".txt";
				if (readMessagesFromFile(replicaPath, messages)) {
					for (int j = 0; j < messages.size(); j++) {
						String message = messages.get(j);
						messages.set(j, EncryptionUtil.decrypt(message));
					}
					break; 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}


    public static void updateMessageStatusInFile(String sender, String recipient, String messageId, String attachmentId, String newStatus) {
        String fileName = generateFileName(sender, recipient);
        List<Integer> shards = getReplicationShards(fileName);

        for (int shard : shards) {
            String filePath = DFS_ONE_ONE_MESSAGES + "shard" + shard + "/" + fileName + ".txt";
			if (messageId != null) {
				updateStatusInFile(filePath, messageId, newStatus, false);
			} else if (attachmentId != null) {
				updateStatusInFile(filePath, attachmentId, newStatus, true);
			}
        }
    }

    public static void deleteMessageFromFile(String sender, String recipient, String messageId) {
        String fileName = generateFileName(sender, recipient);
        List<Integer> shards = getReplicationShards(fileName);

        for (int shard : shards) {
            String filePath = DFS_ONE_ONE_MESSAGES + "shard" + shard + "/" + fileName + ".txt";
            deleteMessage(filePath, messageId);
        }
    }
	
	public static void deleteGroupMessageFromFile(String groupName, String messageId) {
		try {
			List<Integer> shards = getReplicationShards(groupName);

			for (int shard : shards) {
				String filePath = DFS_GROUP_MESSAGES + "shard" + shard + "/" + groupName + "-messages.txt";
				deleteMessage(filePath, messageId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public static void editMessageInFile(String sender, String recipient, String messageId, String newText) {
        String fileName = generateFileName(sender, recipient);
        List<Integer> shards = getReplicationShards(fileName);
		System.out.println("came to edit method ");
        for (int shard : shards) {
            String filePath = DFS_ONE_ONE_MESSAGES + "shard" + shard + "/" + fileName + ".txt";
            editMessage(filePath, messageId, newText);
        }
    }
	
	public static void editGroupMessageInFile(String groupName, String messageId, String newText) {
		System.out.println("came to editgroupMessgnvlksdmnsvlkn" + groupName);
		try {
			List<Integer> shards = getReplicationShards(groupName);
			System.out.println("came to editgroupMessg" + groupName);
			for (int shard : shards) {
				String filePath = DFS_GROUP_MESSAGES + "shard" + shard + "/" + groupName + "-messages.txt";
				editMessage(filePath, messageId, newText);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private static void writeFile(String filePath, String content) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(content + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean readMessagesFromFile(String filePath, List<String> messages) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        messages.add(line);
                    }
					return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return false;
    }

    private static void updateStatusInFile(String filePath, String id, String newStatus, boolean isAttachment) {
		try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            String line;
            long pointer = 0;

            while ((line = raf.readLine()) != null) {
                String decryptedLine = EncryptionUtil.decrypt(line); 
				String[] parts;
				
				if(!isAttachment)
					parts = decryptedLine.split(": ");
				else
					parts = decryptedLine.split("\\|");
				
                if (!isAttachment && parts.length == 4 && parts[0].equals(id)) {
                    long currentPos = pointer;
                    raf.seek(currentPos);

                    String updatedLine = parts[0] + ": " + parts[1] + ": " + parts[2] + ": " + newStatus;
                    String encryptedLine = EncryptionUtil.encrypt(updatedLine); 
                    raf.writeBytes(encryptedLine + System.lineSeparator());
                    break;
                } else if(isAttachment && parts.length == 6 && parts[0].equals("[attachment]" + id)) {
					long currentPos = pointer;
					raf.seek(currentPos);
					
					parts[4] = newStatus;
					String updatedLine = String.join("|", parts);
					String encryptedLine = EncryptionUtil.encrypt(updatedLine); 
					raf.writeBytes(encryptedLine + System.lineSeparator());
					break;
				}
                pointer = raf.getFilePointer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteMessage(String filePath, String messageId) {
       try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = raf.readLine()) != null) {
                String decryptedLine = EncryptionUtil.decrypt(line); 
                
				if (decryptedLine.startsWith("[attachment]")) {
					String[] parts = decryptedLine.split("\\|");
					if (parts.length > 0 && parts[0].equals("[attachment]" + messageId)) {
						continue; // Skip it
					}
				} else {
					String[] parts = decryptedLine.split(": ");
					if (parts.length > 0 && parts[0].equals(messageId)) {
						continue; // Skip it
					}
				}
                content.append(line).append(System.lineSeparator());
            }

            raf.setLength(0); 
            raf.writeBytes(content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private static void editMessage(String filePath, String messageId, String newText) {
		try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            String line;
            long pointer = 0;

            while ((line = raf.readLine()) != null) {
                String decryptedLine = EncryptionUtil.decrypt(line);
                String[] parts = decryptedLine.split(": ");
                if (parts.length == 4 && parts[0].equals(messageId)) {
                    long currentPos = pointer;
                    raf.seek(currentPos);

                    String updatedLine = parts[0] + ": " + parts[1] + ": " + newText + ": " + parts[3];
					System.out.println("updatedLine " + updatedLine);
                    String encryptedLine = EncryptionUtil.encrypt(updatedLine);					
                    raf.writeBytes(encryptedLine + System.lineSeparator());
                    break;
                }
                pointer = raf.getFilePointer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	
    private static String generateFileName(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "-" + user2 : user2 + "-" + user1;
    }
}
