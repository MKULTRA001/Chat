/*This class contains the Message table within the user_message database */




package cse326.SoftwareEng.database.messageDB;
import jakarta.persistence.*;
@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(generator="increment")
    @Column(name = "message_id")
    private int message_id;

    @Column(name = "message")
    private String message;



    /*Mapping foreign key from UserMessageDB user_id to Messages Entity*/
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserMessageDB user;


    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
