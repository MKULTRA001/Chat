/**
 * Allows the creation of MYSQL queries that can be used in the controller class to complete operations on the user table
 */


package cse326.SoftwareEng.chat.message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/*JpaRepository provides a set of standard CRUD (Create, Read, Update, Delete) methods */
@Repository("userRepoMessageDB")
public interface UserRepositoryMessageDB extends JpaRepository<UserMessageDB, String> {
    @Query("SELECT u FROM UserMessageDB u WHERE u.username = ?1")
    UserMessageDB findByUsername(String Username);


    @Query("SELECT u FROM UserMessageDB u WHERE u.user_id = ?1")
    UserMessageDB findByID(String id);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserMessageDB u WHERE u.username = ?1")
    void deleteByUsername(String userName);
    @Modifying
    @Transactional
    @Query("UPDATE UserMessageDB u SET u.publicKey = ?1 WHERE u.username = ?2")
    void updatePublicKeyByUsername(byte[] publicKey, String username);
    @Query("SELECT u.publicKey FROM UserMessageDB u WHERE u.username = ?1")
    byte[] findPublicKeyByUsername(String username);

}
