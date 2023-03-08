/*This class is the meant to be the user class within the the user_message database*/

package cse326.SoftwareEng.database.messageDB;
import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "userMessageDB")
public class UserMessageDB {
    @Id
    @GeneratedValue(generator="increment")
    @Column(name = "user_id")
    private int id;


    @Column(name = "username", nullable = false, length = 25)
    private String username;



    @OneToMany(mappedBy = "user")
    private List<Message> messages;


    public long getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
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
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}