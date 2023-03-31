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
import org.springframework.test.util.AssertionErrors;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;


@ExtendWith(SpringExtension.class)
@DataJpaTest
public class MessageRepositoryTest {



    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepositoryMessageDB userRepoMessageDB;

    private UserMessageDB user;

    private Message message1;

    private Message message2;


    /*Initialize the database with a user and a message*/
    @BeforeEach
    public void setUp() {
        user = new UserMessageDB("alex");
        userRepoMessageDB.saveAndFlush(user);



        message1 = new Message("hello,",new Date(),user);
        messageRepository.save(message1);
        message2 = new Message("world!",new Date(),user);
        messageRepository.save(message2);
    }




    /*Test that findByMessageID returns the correct message*/
    @Test
    public void testFindByMessageID() {
        Message found = messageRepository.findByMessageID(message1.getMessage_id());
        Assertions.assertEquals(found.getMessage_id(),message1.getMessage_id());
        Assertions.assertEquals(found.getUser().getId(),user.getId());



        /*Test that findByMessageID returns null if no message with passed message_id exists*/
        Message fail =  messageRepository.findByMessageID("123");
        Assertions.assertNull(fail);



    }


    @Test
    public void deleteByMessageID() {
        /*Used to show that the found id exists in repository and is the same id as the original message*/
        Message found = messageRepository.findByMessageID(message1.getMessage_id());
        Assertions.assertEquals(found.getMessage_id(),message1.getMessage_id());




        AssertionErrors.assertTrue("Successful Deletion", messageRepository.deleteByMessageID(found.getMessage_id()) > 0);
        /*Delete is successful if the message is not found*/
        Assertions.assertNull(messageRepository.findByMessageID(found.getMessage_id()));


        Message fail = messageRepository.findByMessageID("123");
        /*finds nothing*/
        Assertions.assertNull(fail);
        /*deleteByMessageID returns 0 if no message with passed message_id exists*/
        AssertionErrors.assertFalse("deleteByMessageID did not find a id to delete ", messageRepository.deleteByMessageID(found.getMessage_id()) > 0);


    }


    @Test
    public void findAllMessagesByUserIdTest(){
        List<Message> messages = messageRepository.findAllMessagesByUserId(user.getId());
        Assertions.assertEquals(messages.get(0).getMessage_id(),message1.getMessage_id());
        Assertions.assertEquals(messages.get(1).getMessage_id(),message2.getMessage_id());

    }



}