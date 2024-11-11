import java.util.*;
/* 
Chat and Message classes, Chat behaves as a log that stores all previous messages between two users,
defined by senderID & receiverID. ID variables are hashed and stored in DB for query. 
Message class holds the message content defined by String and timestamp defined by Int. 
These messages are stored in the Chat class through an ArrayList.
*/   

public class Chat {
    // Attributes
    private byte[] senderID = new byte[32];
    private byte[] receiverID = new byte[32];
    private final ArrayList<Message> messages = new ArrayList<>();
    // Constructor 
    public Chat(byte[] senderID, byte[] receiverID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    // Getter 
    public byte[] getSenderID() {
        return senderID;
    }    
    public byte[] getReceiverID() {
        return receiverID;
    }
    public Iterator<Message> getMessages() {
        return messages.iterator();
    }
    // Setter
    public void setSenderID(byte[] senderID) {
        this.senderID = senderID;
    }
    public void setReceiverID(byte[] receiverID) {
        this.receiverID = receiverID;
    }   
    // Add messages into chatlogs
    public void addMessages(String content, int timestamp) {
        messages.add(new Message(content, timestamp));
    }

    class Message {
        // Attributes
        private String content; 
        private int timestamp;

        // Constructor
        public Message(String content, int timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }
        
        // Getter
        public String getContent() {
            return this.content;
        }
        public int getTimestamp() {
            return this.timestamp;
        }

        // Setter
        public void setContent(String content) {
            this.content = content;
        }
        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }
        
        // toString
        public String toString() {
            return "Content: " + this.content + "\nTimestamp: " + this.timestamp;
        }
    }
}
