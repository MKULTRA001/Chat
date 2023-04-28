package cse326.SoftwareEng.backEnd;

import cse326.SoftwareEng.database.messageDB.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @RequestMapping("/chat")
    public String TestIndex(){
        return "chat";
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
    @MessageMapping("/chat/private/{user1}/{user2}")
    @SendTo({"/chat/message/private/{user1}/{user2}"})
    public TextMessage directMessage(@DestinationVariable String user1, @DestinationVariable String user2, TextMessage message) {
        System.out.println("Message in directMessage: " + message.getMessage());
        String username = message.getMessage().split(":")[0];
        UserMessageDB user = userRepositoryMessageDB.findByUsername(user1);

        if (!username.equals(user1)) {
            System.out.println("ERROR UEQ");
            return null; // Do not send the message if the sender is not user1
        }
        System.out.println("user1 username: " + user.getUsername());
        UserMessageDB users2 = userRepositoryMessageDB.findByUsername(user2);
        System.out.println("user2 username: " + users2.getUsername());
        Channel privateChannel = userChannelRepository.findPrivateChannelByUsernames(user.getUsername(), users2.getUsername());
        System.out.println("Private channel: " + privateChannel);
        if (privateChannel == null) {
            System.out.println("PVC");
            return null; // Do not send the message if the private channel does not exist
        }

        Date date = new Date();
        Message dbMessage = new Message(message.getMessage(), date, user, privateChannel.getChannel_id());
        System.out.println("Before saving message: " + dbMessage);
        messageRepository.save(dbMessage);
        System.out.println("After saving message: " + dbMessage);
        String messageId = dbMessage.getMessage_id();
        TextMessage message1 = new TextMessage(message.getMessage(), user1, date.toString(), messageId, privateChannel.getChannel_id());
        System.out.println("Message: "+ message1);
        return (message1);
    }

    @PostMapping("/createChannel")
    public ResponseEntity<Map<String, String>> createChannel(@RequestBody Map<String, Object> payload) {
        String currentUser = currentUserName();
        String inviteLink = UUID.randomUUID().toString(); // Generate a unique invite link
        String channelName = (String) payload.get("channelName");
        List<String> userList = (List<String>) payload.get("userList");
        Channel channel = new Channel(channelName, currentUser, inviteLink, Boolean.FALSE);
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
    @PostMapping("/createPrivateChannel/{user1}/{user2}")
    public ResponseEntity<String> createPrivateChannel(@PathVariable String user1, @PathVariable String user2) {
        String channelName = "DM:" + user1 + " and " + user2;
        Channel channel = new Channel(channelName, user1, null, Boolean.TRUE);
        channelRepository.save(channel);
        UserMessageDB users1 = userRepositoryMessageDB.findByUsername(user1);
        UserMessageDB users2 = userRepositoryMessageDB.findByUsername(user2);
        UserChannel userChannel1 = new UserChannel(users1.getId(), channel.getChannel_id());
        UserChannel userChannel2 = new UserChannel(users2.getId(), channel.getChannel_id());
        userChannelRepository.save(userChannel1);
        userChannelRepository.save(userChannel2);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/checkPrivateChannel/{channel_ID}")
    @ResponseBody
    public boolean checkPrivateChannel(@PathVariable String channel_ID) {
        System.out.println("checkPrivateChannel Channel ID: " + channel_ID);
        Channel channel = channelRepository.findByChannelID(channel_ID);
        if (channel == null) {
            return false;
        }
        System.out.println("checkPrivateChannel Channel: " + channel.isPrivateChannel());
        return channel.isPrivateChannel();
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
    @PostMapping(value = "/savePublicKey", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> savePublicKey(@RequestBody Map<String, List<Integer>> payload) {
        List<Integer> publicKeyList = payload.get("publicKey");
        if (publicKeyList == null) {
            return new ResponseEntity<>("Public key not provided.", HttpStatus.BAD_REQUEST);
        }
        System.out.println("savePublicKey: " + publicKeyList);
        // Convert List<Integer> to byte[]
        byte[] publicKey = new byte[publicKeyList.size()];
        for (int i = 0; i < publicKeyList.size(); i++) {
            publicKey[i] = publicKeyList.get(i).byteValue();
        }
        System.out.println("savePublicKey: " + publicKey.length);
        String currentUser = currentUserName();
        System.out.println("savePublicKey: " + currentUser);
        UserMessageDB user = userRepositoryMessageDB.findByUsername(currentUser);
        if (user == null) {
            return new ResponseEntity<>("User not found.", HttpStatus.BAD_REQUEST);
        }
        userRepositoryMessageDB.updatePublicKeyByUsername(publicKey, user.getUsername());
        return new ResponseEntity<>("Public key saved successfully.", HttpStatus.OK);
    }
    @GetMapping("/getPublicKey/{username}")
    @ResponseBody
    public String getPublicKey(@PathVariable String username) {
        UserMessageDB user = userRepositoryMessageDB.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        System.out.println("getPublicKey: " + user.getUsername());
        return Base64.getEncoder().encodeToString(user.getPublicKey());
    }
    @GetMapping("/getChannels")
    @ResponseBody
    public List<Channel> getChannels() {
        String currentUser = currentUserName();
        UserMessageDB user = userRepositoryMessageDB.findByUsername(currentUser);
        return userChannelRepository.findChannelsByUserId(user.getId());
    }


    @GetMapping("/getChannelUsers/{channelId}")
    @ResponseBody
    public List<String> getChannelUsers(@PathVariable String channelId) {
        List<String> usernames = userChannelRepository.findUsernamesByChannelId(channelId);
        System.out.println("getChannelUsers: " + usernames);
        return usernames;
    }

}
