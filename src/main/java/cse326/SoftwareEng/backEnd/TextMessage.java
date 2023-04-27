package cse326.SoftwareEng.backEnd;

/**
 * Allows message text to be treated as JSON for transfer
 */
public class TextMessage {
    private String message;
    private String uname;
    private String uname2;
    private String time;
    private String messageId;
    private String channel;
    public TextMessage(){}
    public TextMessage(String message){
        this.message = message;
        this.uname = "";
    }
    public TextMessage(String message, String uname){
        System.out.println(message + " " + uname);
        this.message = message;
        this.uname = uname;
    }

    public TextMessage(String message, String uname, String time, String messageId, String uname2, String channel){
        this.message = message;
        this.uname = uname;
        this.uname2 = uname2;
        this.time = time;
        this.messageId = messageId;
        this.channel = channel;
        System.out.println("Message: " + message + " uname: " + uname + " time: " + time + " messageId: " + messageId);
    }
    public TextMessage(String message, String uname, String time, String messageId, String channel){
        this.message = message;
        this.uname = uname;
        this.time = time;
        this.messageId = messageId;
        this.channel = channel;
        System.out.println("Message: " + message + " uname: " + uname + " time: " + time + " messageId: " + messageId + " channel: " + channel);
    }
    public String getMessage() {return message;}
    public String getUname() {return uname;}
    public String getTime() {return time;}
    public String getMessageId() {return messageId;}

    public String getChannel() {
        return channel;
    }

}
