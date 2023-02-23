package cse326.SoftwareEng.backEnd;

import cse326.SoftwareEng.database.AppController;
import cse326.SoftwareEng.database.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Test controller
 * <p>mapped to /app/chat</p>
 * <p>Sends to /chat/hello</p>
 */
@Controller
public class HelloController{

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
        return new TextMessage(message.getMessage());
    }

    /**
     * Respond to login attempts
     * <p>In: /app/name</p>
     * <p>Out: /chat/hello</p>
     * @param message incoming message (in this it should be a name)
     * @return response to payload
     */
    @MessageMapping("/name")
    @SendTo("/chat/hello")
    public TextMessage login(TextMessage message){
        //You can do whatever handling you want with this uname via message.getMessage()
        //Currently, this is managed by a client-side variable for display only.
        //In theory, though, you should be able to message other objects and have them do whatever processes you need
        return new TextMessage(message.getMessage() + " Has Connected!");
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
