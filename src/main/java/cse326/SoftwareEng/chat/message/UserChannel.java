package cse326.SoftwareEng.chat.message;
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
    @Column(name = "encrypted_symmetric_key")
    private String encryptedSymmetricKey;

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
    public UserChannel(String user_id, String channel_id, String encrypted_symmetric_key) {
        this.user_id = user_id;
        this.channel_id = channel_id;
        this.encryptedSymmetricKey = encrypted_symmetric_key;
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
    public String getEncryptedSymmetricKey() {
        return encryptedSymmetricKey;
    }

    public void setEncryptedSymmetricKey(String encryptedSymmetricKey) {
        this.encryptedSymmetricKey = encryptedSymmetricKey;
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}