/*This class contains the Message table within the user_message database. Notably different from the UserMessageDB class as that is part of a different database*/




package cse326.SoftwareEng.chat.message;
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
    @Column(name = "channel_id")
    private String channel_id;
    @Column(name = "iv")
    private String iv;

    @ManyToOne
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private Channel channel;

    /*Constructor for Message class where message_id, message, sendTime, and user are instantiated respectively*/
    public Message(String message, Date sendTime, UserMessageDB user, String channel_id) {
        this.message_id = UUID.randomUUID().toString();
        this.message = message;
        this.sendTime = sendTime;
        this.user = user;
        this.channel_id = channel_id;
    }
    public Message(String message, Date sendTime, UserMessageDB user, String channel_id, String iv) {
        this.message_id = UUID.randomUUID().toString();
        this.message = message;
        this.sendTime = sendTime;
        this.user = user;
        this.channel_id = channel_id;
        this.iv = iv;
    }
    /*Default constructor for Message class*/
    public Message() {
        this.message_id = UUID.randomUUID().toString();
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
    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message_id='" + message_id + '\'' +
                ", message='" + message + '\'' +
                ", sendTime=" + sendTime +
                ", user=" + user.getUsername() +
                ", channel_id='" + channel_id + '\'' +
                ", iv='" + iv + '\'' +
                '}';
    }
}
