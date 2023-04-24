package cse326.SoftwareEng.database.messageDB;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Date;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     *
     * @param message_id UUID message_id
     * @return Message with  passed message_id. Returns null if no message with passed message_id exists
     *
     */
    @Query("SELECT m FROM Message m WHERE m.message_id = ?1")
    Message findByMessageID(String message_id);

    /**
     *
     * @param message_id UUID message_id
     * @return  Deletes Message with message_id.
     *
     */

    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.message_id = ?1")
    int deleteByMessageID(String message_id);

    @Transactional
    @Query("DELETE FROM Message m WHERE m.user.username = ?1")
    void deleteUser(String user_name);

    boolean existsByUserId(String userId);

    @Query("SELECT u.message FROM Message u WHERE u.user.username = ?1")
    List<String> findAllMessagesByUsername(String userName);
    /**
     *
     * @param user_id UUID user_id
     * @return List of all messages with passed user_id sorted by time in descending order ex 2024,today,1928
     *
     */
    @Query("SELECT m FROM Message m WHERE m.user.user_id = ?1 ORDER BY m.sendTime DESC")
    List<Message> findAllMessagesByUserIdSortedByTimeDesc(String user_id);

    /**
     *
     * @return List of all messages in  Message table by time in ascending order
     *
     */
    @Query("SELECT m FROM Message m ORDER BY m.sendTime DESC ")
    List<Message> findAllByOrderByTimeDesc();

    @Query("SELECT m FROM Message m WHERE m.user.user_id = ?1 AND m.channel.channel_id = ?2")
    List<Message> findAllMessagesByUserIdAndChannelId(String user_id, String channel_id);
    @Query("SELECT m FROM Message m WHERE m.channel.channel_id = ?1 ORDER BY m.sendTime DESC")
    List<Message> findAllMessagesByChannelIdSortedByTimeDesc(String channel_id);

}
