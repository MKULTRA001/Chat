package cse326.SoftwareEng.backEnd;

/**
 * Allows message text to be treated as JSON for transfer
 */
public class TextMessage {
    private String message;
    private String uname;

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

    public String getMessage() {return message;}
    public String getUname() {return message;}
}
