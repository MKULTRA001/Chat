package cse326.SoftwareEng.backEnd;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Test controller
 * <p>mapped to /app/test</p>
 * <p>Sends to /test/hello</p>
 */
@Controller
public class HelloController {

    /**
     * Basic controller to respond to messages
     * <p>In: /app/test</p>
     * <p>Out: /test/hello</p>
     * Please note that these are separate endpoints
     * @param message incoming message (in this text example it should be a name)
     * @return response to payload
     */
    @MessageMapping("/test")
    @SendTo("/test/hello")
    public TestMessage helloWorld(TestMessage message){
        return new TestMessage(message.getMessage());
    }


    /**
     * Respond to """login""" attempts
     * <p>In: /app/name</p>
     * <p>Out: /test/hello</p>
     * @param message incoming message (in this text example it should be a name)
     * @return response to payload
     */
    @MessageMapping("/name")
    @SendTo("/test/hello")
    public TestMessage login(TestMessage message){
        //You can do whatever handling you want with this uname via message.getMessage()
        //Currently, this is managed by a client-side variable for display only.
        //In theory, though, you should be able to message other objects and have them do whatever processes you need
        return new TestMessage(message.getMessage() + " Has Connected!");
    }
}
