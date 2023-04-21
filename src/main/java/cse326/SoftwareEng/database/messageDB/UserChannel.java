package cse326.SoftwareEng.database.messageDB;
import jakarta.persistence.*;

@Entity
@Table(name = "UserChannel")
@IdClass(UserChannelId.class)
public class UserChannel {
    @Id
    @Column(name = "user_id")
    private String user_id;

    @Id
    @Column(name = "channel_id")
    private String channel_id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserMessageDB user;

    @ManyToOne
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private Channel channel;

    public UserChannel(String user_id, String channel_id) {
        this.user_id = user_id;
        this.channel_id = channel_id;
    }

    public UserChannel() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public UserMessageDB getUser() {
        return user;
    }

    public void setUser(UserMessageDB user) {
        this.user = user;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}