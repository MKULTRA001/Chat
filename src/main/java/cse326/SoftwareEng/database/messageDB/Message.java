/*This class contains the Message table within the user_message database. Notably different from the UserMessageDB class as that is part of a different database*/




package cse326.SoftwareEng.database.messageDB;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Message")
public class Message {
    @Id
    @Column(name = "message_id")
    private String message_id;

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
    public Message(String message, Date sendTime, UserMessageDB user) {
        this.message_id = UUID.randomUUID().toString();
        this.message = message;
        this.sendTime = sendTime;
        this.user = user;
    }
    /*Default constructor for Message class*/
    public Message() {

    }

    /**
     * Used to fetch UUID message_id
     * @return message_id
     * @throws  NullPointerException when on ID is found
     */
    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
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

    @Override
    public String toString() {
        return "Message{" +
                "message_id='" + message_id + '\'' +
                ", message='" + message + '\'' +
                ", sendTime=" + sendTime +
                ", user=" + user.getUsername() +
                '}';
    }
}
