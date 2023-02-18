package cse326.SoftwareEng.backEnd;

/**
 * Allows message text to be treated as JSON for transfer
 */
public class TestMessage {
    private String message;

    public TestMessage(){}
    public TestMessage(String message){
        this.message = message;
    }

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}
}
