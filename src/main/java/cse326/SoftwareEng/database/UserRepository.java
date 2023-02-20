package cse326.SoftwareEng.database;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    User findByUsername(String Username);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.username = ?1")
    void deleteByUsername(String userName);
}
