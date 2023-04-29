package cse326.SoftwareEng.chat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannel, UserChannelId> {
    @Query("SELECT uc.channel FROM UserChannel uc WHERE uc.user_id = ?1")
    List<Channel> findChannelsByUserId(String userId);

    @Query("SELECT uc.user.username FROM UserChannel uc WHERE uc.channel_id = ?1")
    List<String> findUsernamesByChannelId(String channelId);


    @Query("SELECT uc FROM UserChannel uc WHERE uc.user_id = ?1 AND uc.channel_id = ?2")
    UserChannel findByUserIdAndChannelId(String userId, String channelId);

    @Query("SELECT uc1.channel FROM UserChannel uc1, UserChannel uc2 WHERE uc1.user.username = ?1 AND uc2.user.username = ?2 AND uc1.channel.channel_id = uc2.channel.channel_id AND uc1.channel.privateChannel = true")
    Channel findPrivateChannelByUsernames(String username1, String username2);
}