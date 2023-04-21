package cse326.SoftwareEng.database.messageDB;
import java.io.Serializable;
import java.util.Objects;

public class UserChannelId implements Serializable {
    private String user_id;
    private String channel_id;

    public UserChannelId() {
    }

    public UserChannelId(String user_id, String channel_id) {
        this.user_id = user_id;
        this.channel_id = channel_id;
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
}