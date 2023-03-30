/**This class tests the MessageRepository class using an H2 database*/

package cse326.SoftwareEng.database.messageDB;

import cse326.SoftwareEng.database.userDB.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;


@ExtendWith(SpringExtension.class)
@DataJpaTest
public class MessageRepositoryTest {



    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepositoryMessageDB userRepoMessageDB;

    private UserMessageDB user;

    private Message message;


    /**Initialize the database with a user and a message*/
    @BeforeEach
    public void setUp() {
        user = new UserMessageDB("alex");
        userRepoMessageDB.saveAndFlush(user);


        message = new Message();
        message.setMessage("hello");
        message.setSendTime(new Date());
        message.setUser(user);
        message = new Message("hello",new Date(),user);
        messageRepository.save(message);
    }





    @Test
    public void testFindByMessageID() {


        Message found = messageRepository.findByMessageID(message.getMessage_id());
        Assertions.assertEquals(found.getMessage_id(),message.getMessage_id());
        Assertions.assertEquals(found.getUser().getId(),user.getId());


    }



}