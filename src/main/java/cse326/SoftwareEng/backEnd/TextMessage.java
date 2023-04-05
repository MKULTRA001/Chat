package cse326.SoftwareEng.backEnd;

/**
 * Allows message text to be treated as JSON for transfer
 */
public class TextMessage {
    private String message;
    private String uname;
    private String time;
    private String messageId;

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

    public TextMessage(String message, String uname, String time, String messageId){
        this.message = message;
        this.uname = uname;
        this.time = time;
        this.messageId = messageId;
        System.out.println("Message: " + message + " uname: " + uname + " time: " + time + " messageId: " + messageId);
    }

    public String getMessage() {return message;}
    public String getUname() {return uname;}
    public String getTime() {return time;}
    public String getMessageId() {return messageId;}
}
