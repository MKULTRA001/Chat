package cse326.SoftwareEng.backEnd;

/**
 * Allows message text to be treated as JSON for transfer
 */
public class TextMessage {
    private String message;

    public TextMessage(){}
    public TextMessage(String message){
        this.message = message;
    }

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}
}
