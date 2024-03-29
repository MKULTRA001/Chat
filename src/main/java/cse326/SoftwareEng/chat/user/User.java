/**
 * Directly called to manifest the MYSQL table "user" in the database "user_info". It is able to define constraints and rename elements of the table using Spring's own syntax
 * Note: If the table "user" does not exist, upon running Spring the table will automatically be generated with the specified parameters below*
 *
 */


package cse326.SoftwareEng.chat.user;
import jakarta.persistence.*;

import java.util.Date;


/*Designates this file as an entity and assigns the table with the name "user" */
@Entity
@Table(name = "user")
public class User {
    /*Assigns "user_id" as the primary key for this table. It will be generated automatically and is currently set to increment with each new user*/
    @Id
    @GeneratedValue(generator="increment")
    /*You are able to keep the variable name as the column name in the database or choose to assign it yourself. It will be automatically be updated. This field is also where MYSQL constraints can be defined */
    @Column(name = "user_id")
    private int id;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(name = "username", nullable = false, length = 25)
    private String username;

    @Column(name = "created_at",updatable = false)
    /*Temporal is used to annotate a Java date with TIMESTAMP corresponding to the data and time*/
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated_at;

    @Column(name = "verification_code")
    private int verification_code;
    @Column(name = "code_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date code_timestamp;

    @Column(name = "verification")
    private boolean verification;

    /*Default constructor for User class*/
    public User(){

    }
    /*Constructor for User class where id, email, password, username, createdAt, updated_at, verification_code, code_timestamp, and verification are instantiated respectively*/
    public User(int id, String email, String password, String username, Date createdAt, Date updated_at, int verification_code, Date code_timestamp, boolean verification) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.createdAt = createdAt;
        this.updated_at = updated_at;
        this.verification_code = verification_code;
        this.code_timestamp = code_timestamp;
        this.verification = verification;
    }



    public int getId() {
        return id;
    }

    /**
     * Used to set a user id. Since it is the primary key of the table, it must be unique.
     * @param id user id
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Used to set a user email. It is specified in its constraints that the email must be unique
     * @param email user email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * Date is in YYYY-MM-DD format and a time in the HH:MM:SS.ssssss format
     * @param updated_at time password was changed
     */
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public int getVerificationCode(){
        return this.verification_code;
    }
    public void setVerificationCode(int verification_code) {
        this.verification_code = verification_code;
    }

    public Date getCode_timestamp(){
        return this.code_timestamp;
    }
    public void setCode_timestamp(Date code_timestamp) {
        this.code_timestamp = code_timestamp;
    }

    public Boolean getVerification(){
        return this.verification;
    }
    public void setVerification(Boolean verification) {
        this.verification = verification;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", updated_at=" + updated_at +
                '}';
    }
}