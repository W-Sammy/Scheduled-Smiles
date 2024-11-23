package Users;
import com.google.gson.annotations.SerializedName;

public class Message {
    // Attributes
    @SerializedName("pairID")
    private byte[] pairID = new byte[32];
    @SerializedName("textContent")
    private String content; 
    @SerializedName("createdAt")
    private int timestamp;

    // Constructor
    public Message(byte[] pairID, String content, int timestamp) {
        this.pairID = pairID;
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
    public byte[] getPairID() {
        return this.pairID;
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