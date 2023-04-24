package cse326.SoftwareEng.database.messageDB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, String> {
    @Query("SELECT c FROM Channel c WHERE c.invite_link = ?1")
    Channel findByInviteLink(String inviteLink);
}