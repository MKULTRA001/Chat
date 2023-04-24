package cse326.SoftwareEng.backEnd;

import cse326.SoftwareEng.database.messageDB.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Test controller
 * <p>mapped to /app/chat</p>
 * <p>Sends to /chat/hello</p>
 */
@Controller
public class HelloController{
    private final UserRepositoryMessageDB userRepositoryMessageDB;
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserChannelRepository userChannelRepository;
    public HelloController(UserRepositoryMessageDB userRepositoryMessageDB,
                           MessageRepository messageRepository,
                           ChannelRepository channelRepository,
                           UserChannelRepository userChannelRepository) {
        this.userRepositoryMessageDB = userRepositoryMessageDB;
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userChannelRepository = userChannelRepository;
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
    @MessageMapping("/chat/{channelId}")
    @SendTo("/chat/message/{channelId}")
    public TextMessage helloWorld(@DestinationVariable String channelId, TextMessage message){
        String username = message.getMessage().split(":")[0];
        UserMessageDB user = userRepositoryMessageDB.findByUsername(username);
        UserChannel userChannel = userChannelRepository.findByUserIdAndChannelId(user.getId(), channelId);
        if (userChannel == null || message.getChannel() == null) {
            return null; // Do not send the message if the user is not in the channel or the channel field is null
        }
        Date date = new Date();
        Message dbMessage = new Message(message.getMessage(), date, user, message.getChannel());
        messageRepository.save(dbMessage);
        String messageId = dbMessage.getMessage_id();
        String channel = message.getChannel();
        TextMessage message1 = new TextMessage(message.getMessage(),username, date.toString(), messageId, channel);
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
    @MessageMapping("/name/{channelId}")
    @SendTo("/chat/login/{channelId}")
    public TextMessage login(@DestinationVariable String channelId, TextMessage message) {
        String[] messageParts = message.getMessage().split(":", 2);
        String username = messageParts[0];
        System.out.println("Message in login: " + message.getMessage());
        UserMessageDB user = userRepositoryMessageDB.findByUsername(username);
        System.out.println("userID: " + user.getId() + " ChannelID: " + channelId);
        UserChannel userChannel = userChannelRepository.findByUserIdAndChannelId(user.getId(), channelId);
        System.out.println("UserChannel: " + userChannel + " Channel: " + channelId);
        if (userChannel == null) {
            System.out.println("User " + username + " is not in channel " + channelId);
            return null;
        }
        StringBuilder response = new StringBuilder("Welcome back, " + username + "! This channel's old messages are:\n");
        // Fetch old messages using the new method in MessageRepository
        List<Message> oldMessages = messageRepository.findAllMessagesByChannelIdSortedByTimeDesc(channelId);
        for (Message oldMessage : oldMessages) {
            String[] messageContentParts = oldMessage.getMessage().split(":", 2);
            String messageContent = messageContentParts.length > 1 ? messageContentParts[1].trim() : "";
            response.append(oldMessage.getUser().getUsername())
                    .append(": ")
                    .append(messageContent)
                    .append("\n");
        }
        System.out.println(response.toString());
        return new TextMessage(username + ":" + response.toString());
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
    @PostMapping("/createChannel")
    public ResponseEntity<Map<String, String>> createChannel(@RequestBody Map<String, Object> payload) {
        String currentUser = currentUserName();
        String inviteLink = UUID.randomUUID().toString(); // Generate a unique invite link
        String channelName = (String) payload.get("channelName");
        List<String> userList = (List<String>) payload.get("userList");
        Channel channel = new Channel(channelName, currentUser, inviteLink);
        UserMessageDB cUser = userRepositoryMessageDB.findByUsername(currentUser);
        UserChannel userChannel = new UserChannel(cUser.getId(), channel.getChannel_id());
        channelRepository.save(channel);
        userChannelRepository.save(userChannel);
        for (String username : userList) {
            UserMessageDB user = userRepositoryMessageDB.findByUsername(username);
            if (user != null) {
                UserChannel userChannel1 = new UserChannel(user.getId(), channel.getChannel_id());
                userChannelRepository.save(userChannel1);
            }
        }
        Map<String, String> response = new HashMap<>();
        response.put("inviteLink", inviteLink);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/joinChannel")
    public ResponseEntity<String> joinChannel(@RequestParam String inviteLink) {
        Channel channel = channelRepository.findByInviteLink(inviteLink);
        if (channel == null) {
            return new ResponseEntity<>("Invalid invite link.", HttpStatus.BAD_REQUEST);
        }
        String currentUser = currentUserName();
        System.out.println(currentUser);
        UserMessageDB user = userRepositoryMessageDB.findByUsername(currentUser);
        UserChannel userChannel = new UserChannel(user.getId(), channel.getChannel_id());
        userChannelRepository.save(userChannel);
        return new ResponseEntity<>("Joined channel successfully.", HttpStatus.OK);
    }

    @GetMapping("/getChannels")
    @ResponseBody
    public List<Channel> getChannels() {
        String currentUser = currentUserName();
        UserMessageDB user = userRepositoryMessageDB.findByUsername(currentUser);
        return userChannelRepository.findChannelsByUserId(user.getId());
    }


    @GetMapping("/getChannelUsers/{channelId}")
    public List<UserMessageDB> getChannelUsers(@PathVariable String channelId) {
        return userChannelRepository.findUsersByChannelId(channelId);
    }
}
