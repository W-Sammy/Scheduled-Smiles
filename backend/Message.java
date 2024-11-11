public class Message {
    // Attributes
    private String content;
    private int timestamp;

    // Contructor
    public Message(String content, int timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }
    
    // Getters
    public String getContent() {
        return this.content;
    }
    public int getTimestamp() {
        return this.timestamp;
    }
    // Setters 
    public void setContent(String content) {
        this.content = content;
    }
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
