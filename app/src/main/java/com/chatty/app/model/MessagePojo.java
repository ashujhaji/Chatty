package com.chatty.app.model;

public class MessagePojo {
    private String message_id;
    private String create_at;
    private String sender;
    private String message;

    public MessagePojo(String message_id, String create_at, String sender, String message) {
        this.message_id = message_id;
        this.create_at = create_at;
        this.sender = sender;
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
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
}
