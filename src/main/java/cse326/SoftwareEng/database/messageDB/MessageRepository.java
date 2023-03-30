package cse326.SoftwareEng.database.messageDB;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.message_id = ?1")
    Message findByMessageID(String message_id);

    @Transactional
    @Query("DELETE FROM Message m WHERE m.message_id = ?1")
    void deleteByMessageID(String message_id);






}
