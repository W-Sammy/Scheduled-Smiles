package Users;

import java.util.*;
import com.google.gson.annotations.SerializedName;
import Users.Message;
/* 
Chat and Message classes, Chat behaves as a log that stores all previous messages between two users,
defined by senderID & receiverID. ID variables are hashed and stored in DB for query. 
Message class holds the message content defined by String and timestamp defined by Int. 
These messages are stored in the Chat class through an ArrayList.
*/   

public class Chat {
    // Attributes
    @SerializedName("pairID")
    private byte[] pairID = new byte[32];
    @SerializedName("senderID")
    private byte[] senderID = new byte[32];
    @SerializedName("receiverID")
    private byte[] receiverID = new byte[32];
    @SerializedName("messages")
    private final ArrayList<Message> messages = new ArrayList<>();
    // Constructor 
    public Chat(byte[] senderID, byte[] receiverID, byte[] pairID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.pairID = pairID;
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
        messages.add(new Message(pairID, content, timestamp));
    }
}