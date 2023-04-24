package cse326.SoftwareEng.database.messageDB;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Channel")
public class Channel {
    @Id
    @Column(name = "channel_id")
    private String channel_id;

    @Column(name = "channel_name", nullable = false, unique = true)
    private String channel_name;

    @Column(name = "created_by", nullable = false)
    private String created_by;

    @Column(name = "invite_link")
    private String invite_link;

    public Channel(String channel_name, String created_by, String invite_link) {
        this.channel_id = UUID.randomUUID().toString();
        this.channel_name = channel_name;
        this.created_by = created_by;
        this.invite_link = invite_link;
    }

    public Channel() {
        this.channel_id = UUID.randomUUID().toString();
    }


    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getInvite_link() {
        return invite_link;
    }

    public void setInvite_link(String invite_link) {
        this.invite_link = invite_link;
    }
    @Override
    public String toString() {
        return "Channel{" +
                "channel_id='" + channel_id + '\'' +
                ", channel_name='" + channel_name + '\'' +
                ", created_by='" + created_by + '\'' +
                ", invite_link='" + invite_link + '\'' +
                '}';
    }
}
