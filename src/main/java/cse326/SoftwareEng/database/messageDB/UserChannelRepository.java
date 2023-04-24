package cse326.SoftwareEng.database.messageDB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannel, UserChannelId> {
    @Query("SELECT uc.channel FROM UserChannel uc WHERE uc.user_id = ?1")
    List<Channel> findChannelsByUserId(String userId);

    @Query("SELECT uc.user FROM UserChannel uc WHERE uc.channel_id = ?1")
    List<UserMessageDB> findUsersByChannelId(String channelId);

    @Query("SELECT uc FROM UserChannel uc WHERE uc.user_id = ?1 AND uc.channel_id = ?2")
    UserChannel findByUserIdAndChannelId(String userId, String channelId);
}