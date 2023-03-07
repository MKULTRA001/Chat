/**
 * Allows the creation of MYSQL queries that can be used in the controller class to complete operations on the user table
 */


package cse326.SoftwareEng.database.messageDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/*JpaRepository provides a set of standard CRUD (Create, Read, Update, Delete) methods */
@Repository("userRepoMessageDB")
public interface UserRepositoryMessageDB extends JpaRepository<UserMessageDB, Long> {
    @Query("SELECT u FROM UserMessageDB u WHERE u.username = ?1")
    UserMessageDB findByUsername(String Username);

}
