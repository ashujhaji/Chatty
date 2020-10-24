package lets.digi.talk.model;

public class MessagePojo {
    private String sender;
    private String message;
    private Long timestamp;

    public MessagePojo(String sender, String message,Long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp(){return  timestamp;}

    public void  setTimestamp(Long timestamp){this.timestamp = timestamp;}
}
