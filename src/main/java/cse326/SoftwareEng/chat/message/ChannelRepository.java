package cse326.SoftwareEng.chat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, String> {
    @Query("SELECT c FROM Channel c WHERE c.invite_link = ?1")
    Channel findByInviteLink(String inviteLink);
    @Query("SELECT c FROM Channel c WHERE c.channel_id = ?1")
    Channel findByChannelID(String channel_id);
    @Query("SELECT c.created_by FROM Channel c WHERE c.channel_id = ?1")
    String findCreatorByChannelId(String channel_id);
}