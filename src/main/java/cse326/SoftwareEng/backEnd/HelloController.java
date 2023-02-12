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
        return new TestMessage("Received: " + message.getMessage());
    }
}
