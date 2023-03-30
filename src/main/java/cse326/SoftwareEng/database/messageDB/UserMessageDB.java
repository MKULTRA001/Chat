/*This class is the meant to be the user class within the the user_message database*/

package cse326.SoftwareEng.database.messageDB;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "userMessageDB")
public class UserMessageDB {
    @Id
    @Column(name = "user_id")
    private String user_id;


    @Column(name = "username", nullable = false, length = 25)
    private String username;



    @OneToMany(mappedBy = "user")
    private List<Message> messages;

    /*Constructor for UserMessageDB class where id and username are instantiated respectively*/
    public UserMessageDB(String username) {
        /*sample UUID  123e4567-e89b-12d3-a456-426655440000*/
        this.user_id = UUID.randomUUID().toString();
        this.username = username;
    }

    /*Default constructor for UserMessageDB class*/
    public UserMessageDB() {

    }


    public String getId() {
        return user_id;
    }


    public void setId(String user_id) {
        this.user_id = user_id;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + user_id +
                ", username='" + username + '\'' +
                '}';
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        message.setUser(this);
    }


    public void removeMessage(Message message) {
        this.messages.remove(message);
        message.setUser(null);
    }
}
