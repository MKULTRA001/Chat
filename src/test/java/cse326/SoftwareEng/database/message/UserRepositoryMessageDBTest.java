/**This class tests the MessageRepository class using H2 database*/
package cse326.SoftwareEng.chat.message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@DataJpaTest
class UserRepositoryMessageDBTest {


    @Autowired
    private UserRepositoryMessageDB userRepoMessageDB;

    /**Initialize the database with a user*/
    @BeforeEach
    public void setUp() {
        UserMessageDB testUser1 = new UserMessageDB("mkultra");
        userRepoMessageDB.save(testUser1);


        UserMessageDB testUser2 = new UserMessageDB("valhalla");
        userRepoMessageDB.save(testUser2);

        UserMessageDB testUser3 = new UserMessageDB("Zjwghg");
        userRepoMessageDB.save(testUser3);







    }
    @Test
    void findByUsername() {

        UserMessageDB found = userRepoMessageDB.findByUsername("mkultra");
        assertEquals("mkultra", found.getUsername());

        UserMessageDB found2 = userRepoMessageDB.findByUsername("valhalla");
        assertEquals("valhalla", found2.getUsername());

        UserMessageDB found3 = userRepoMessageDB.findByUsername("Zjwghg");
        assertEquals("Zjwghg", found3.getUsername());

    }
}