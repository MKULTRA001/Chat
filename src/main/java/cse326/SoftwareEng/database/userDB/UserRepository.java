/**
 * Allows the creation of MYSQL queries that can be used in the controller class to complete operations on the user table
 */


package cse326.SoftwareEng.database.userDB;
import cse326.SoftwareEng.database.userDB.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/*JpaRepository provides a set of standard CRUD (Create, Read, Update, Delete) methods */
@Repository("userRepo")
public interface UserRepository extends JpaRepository<User, Long> {
    /*@Query annotation is used to define the query that will be passed to the database.*/
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    User findByUsername(String Username);
    /*Indicates that method modifies the database. Typically used in UPDATE or DELETE*/
    @Modifying
    /*Used to make sure changes persist. Also, if an exception is thrown, it will roll back the transaction to the previous state */
    @Transactional
    @Query("DELETE FROM User u WHERE u.username = ?1")
    void deleteByUsername(String userName);
}
