/*This class contains the Message table within the user_message database. Notably different from the UserMessageDB class as that is part of a different database*/




package cse326.SoftwareEng.database.messageDB;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(generator="increment")
    @Column(name = "message_id")
    private int message_id;

    @Column(name = "message")
    private String message;

    @Column(name = "send_time",updatable = false)
    /*Temporal is used to annotate a Java date with TIMESTAMP corresponding to the data and time*/
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendTime;

    /*Mapping foreign key from UserMessageDB user_id to Messages Entity*/
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserMessageDB user;

    /*Constructor for Message class where message_id, message, sendTime, and user are instantiated respectively*/
    public Message(int message_id, String message, Date sendTime, UserMessageDB user) {
        this.message_id = message_id;
        this.message = message;
        this.sendTime = sendTime;
        this.user = user;
    }
    /*Default constructor for Message class*/
    public Message() {

    }


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

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public UserMessageDB getUser() {
        return user;
    }

    public void setUser(UserMessageDB user) {
        this.user = user;
    }
}
