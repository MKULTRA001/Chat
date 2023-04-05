package cse326.SoftwareEng.backEnd;

import cse326.SoftwareEng.database.messageDB.Message;
import cse326.SoftwareEng.database.messageDB.MessageRepository;
import cse326.SoftwareEng.database.messageDB.UserMessageDB;
import cse326.SoftwareEng.database.messageDB.UserRepositoryMessageDB;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;

/**
 * Test controller
 * <p>mapped to /app/chat</p>
 * <p>Sends to /chat/hello</p>
 */
@Controller
public class HelloController{
    private final UserRepositoryMessageDB userRepositoryMessageDB;
    private final MessageRepository messageRepository;
    public HelloController(UserRepositoryMessageDB userRepositoryMessageDB, MessageRepository messageRepository) {
        this.userRepositoryMessageDB = userRepositoryMessageDB;
        this.messageRepository = messageRepository;
    }
    private String convertMessagesToJson(List<Message> messages) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "[]"; // Return an empty JSON array if there's an error
        }
    }

    /**
     * Basic controller to respond to messages
     * <p>In: /app/chat</p>
     * <p>Out: /chat/hello</p>
     * Please note that these are separate endpoints
     * @param message incoming message
     * @return response to payload
     */
    @MessageMapping("/chat")
    @SendTo("/chat/hello")
    public TextMessage helloWorld(TextMessage message){
        String username = message.getMessage().split(":")[0];
        UserMessageDB user = userRepositoryMessageDB.findByUsername(username);
        Date date = new Date();
        Message dbMessage = new Message(message.getMessage(), date, user);
        messageRepository.save(dbMessage);
        String messageId = dbMessage.getMessage_id();
        TextMessage message1 = new TextMessage(message.getMessage(),username, date.toString(), messageId);
        System.out.println(message1);
        return message1;
    }

    /**
     * Respond to login attempts
     * <p>In: /app/name</p>
     * <p>Out: /chat/hello</p>
     * @param message incoming message (in this it should be a name)
     * @return response to payload
     */
    @MessageMapping("/name")
    @SendTo("/chat/login")
    public TextMessage login(TextMessage message) {
        String username = message.getMessage();
        UserMessageDB user = userRepositoryMessageDB.findByUsername(username);
        StringBuilder response = new StringBuilder("Welcome back, " + username + "! Your old messages are:\n");
        // Fetch old messages using the new method in MessageRepository
        List<String> oldMessages = messageRepository.findAllMessagesByUsername(username);
        for (String oldMessage : oldMessages) {
            response.append(oldMessage).append("\n");
        }
        System.out.println(response.toString());
        return new TextMessage(response.toString());
    }
    @RequestMapping("/chat_index")
    public String TestIndex(){
        return "chat_index";
    }
    @RequestMapping(value = "/myUsername", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Handle direct message channel
     * <p>In: /app/chat/private/{user1}/{user2}</p>
     * <p>Out: /chat/private/{user1}/{user2}</p>
     * @param message incoming message
     * @return response to payload
     */
    @MessageMapping("/chat/private/{user1}")
    @SendTo({"/chat/private/{user1}"})
    public TextMessage directMessage(@DestinationVariable String user1, TextMessage message){
        return new TextMessage(message.getMessage());
    }
}
