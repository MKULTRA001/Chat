// Declare a variable to store the Stomp client object, initially set to null
let stompClient = null;

// Declare a boolean variable to keep track of whether the socket is open, initially set to false
let open = false;
let currentChannelId = null;
// An asynchronous function that sends a GET request to the server to get the current user's username
async function getUname() {
    const response = await fetch('/myUsername'); // Send GET request to the server at the specified URL
    const text = await response.text(); // Get the response as text
    console.log(text); // Log the username to the console
    return text; // Return the username as a string
}

// A function that appends a new message to the chat window
function appendMessage(message, color) {
    // Append a new row to the table with the specified background color and message content
    let sanitized = $('<div>').text(message).html();
    $("#text").append(`<tr style = 'color: ${color}' data-id = 'id' data-uid = 'uid' data-ts = 'ts'><td>${sanitized}</td></tr>`);
}

// Metadata included message; send message as full json object
function appendMessage2(packet, color) {
    // The message is first sanitized to prevent any potential security vulnerabilities
    let sanitized = $('<div>').text(packet.message).html();
    // A timestamp is also added to the message
    let ts = 'data-ts = \"' + packet.time + '\"';
    // The message is then appended to the chat window using jQuery
    $("#text").append(`<tr style = 'color: ${color}' data-id = ${packet.messageId} data-uid = ${packet.uname} ${ts}><td>${sanitized}</td></tr>`);
}

// This function is used to display old messages in the chat window.
function displayOldMessages(message, uname) {
    // The response message is parsed as JSON
    const response = JSON.parse(message.body);
    // The old messages are then extracted and split based on newlines
    const oldMessages = response.message.split('\n');
    console.log("old messages: " + oldMessages);
    // If the first message in the old messages array starts with the current user's name followed by a colon,
    // then the chat window is cleared before displaying the old messages.
    if (oldMessages[0].startsWith(uname + ":")) {
        clearChatWindow();
        // Each old message is looped through and appended to the chat window
        for (const oldMessage of oldMessages) {
            if (oldMessage) {
                // The old message is split into an array containing the sender and the message content
                const msgArray = oldMessage.split(':');
                // If the recipient of the message is the current user or if there is no recipient specified,
                // the message is appended to the chat window using the appendMessage function with the color
                // of the sender's name being green and the color of the recipient's name being red.
                if (response.recipient === uname || !response.recipient) {
                    appendMessage(oldMessage, uname === msgArray[0] ? 'green' : 'red');
                }
            }
        }
    }
}



// Retrieve metadata from a message. article is DOMStringMap of HTML message element
// This element should be obtained by whatever edit selector is applied to that element and passed to the function
// For example, document.querySelector("#whatever identifier tag");
// Updates should be sent to some new endpoint and automatically handled
function getMessageData(article, requestType) {
    let uid = article.dataset.id;
    let mid = article.dataset.messageId;
    getUname().then(function (uname) {
        if(uname === uid){
            if(requestType === "D"){
                //perform deletion request to server
                console.log("Delete request processed");
            } else if(requestType === "E") {
                //perform deletion request to server
                console.log("Edit request processed");
            } else {
                console.log("Request of type \"" + requestType + "\" is unknown; request ignored");
            }
        } else {
            console.log("Request of type \"" + requestType + "\" made on message not owned by user; request ignored.");
        }
    });
}

// A function that establishes a WebSocket connection  to the chat server and subscribes to the current channel.
async function connect() {
    // The user's name is retrieved asynchronously from the server.
    const uname = await getUname();
    // If the WebSocket connection is not open or does not exist, a new connection is created.
    if (stompClient == null || !open) {
        // A new SockJS object is created using the chat-websocket endpoint.
        const socket = new SockJS('/chat-websocket');
        // The connection status is set to open.
        open = true;
        // A new Stomp client is created using the SockJS socket.
        stompClient = Stomp.over(socket);
        // The Stomp client connects to the server and subscribes to the current channel.
        stompClient.connect({}, async function (frame) {
            console.log('Connected: ' + frame);
            // The user's name is displayed in the chat window.
            name();
            // The user subscribes to the current channel.
            await subscribeToChannel(currentChannelId);
        });
        // The send button is enabled.
        $("#send").prop("disabled", false);
        // The chat window is scrolled to the bottom after a short delay.
        setTimeout(function(){
            let m = document.querySelector('.message');
            m.scrollTop =  m.scrollHeight;
        }, 500);
    } else {
        // If the WebSocket connection is already open, a message is logged to the console.
        console.log("Could not connect: already connected");
    }
}

// Run connect() when js is loaded
window.onload = connect;

function disconnect() {
    if (stompClient !== null && open) {
        stompClient.disconnect();
        open = false;
        $("#text").empty();
        console.log("Disconnected");
        // Disable text button and name button
        $("#send").prop("disabled", true);
        $("#sendName").prop("disabled", true);
    } else {
        console.log("Could not disconnect: no connection was established");
    }
}
// This function sends a message to the chat server.
function send(message) {
    // The user's name is retrieved asynchronously from the server.
    getUname().then(function (uname) {
        // The username display in the chat window is updated.
        $("#username_Update").replaceWith("Username: " + uname);
        // The value of the message input field is retrieved.
        const value = $("#input").val();
        // If the message starts with "/msg", it is treated as a direct message and sent to the specified user.
        if (value.startsWith('/msg')) {
            // The target user and the message text are extracted from the message input field.
            const [, target, ...payload] = value.split(' ');
            const messageText = payload.join(' ');
            console.log("Direct Message to: " + target);
            // A message is sent to the target user with the sender's name and the message text.
            stompClient.send(`/app/chat/private/${target}`, {}, JSON.stringify({'message': `(${uname} messaged you: ${messageText})`}));
            // A message is sent to the sender with the recipient's name and the message text.
            stompClient.send(`/app/chat/private/${uname}`, {}, JSON.stringify({
                'message': `(you messaged ${
                    target}: ${messageText})`
            }));
        } else {
            // If no target user is specified, the message is sent to the current channel.
            if (currentChannelId) {
                // The destination is set to the current channel.
                const destination = `/app/chat/${currentChannelId}`;
                // A message is sent to the channel with the sender's name and the message text.
                stompClient.send(destination, {}, JSON.stringify({'message': `${uname}: ${value}`, 'channel': currentChannelId}));
            } else {
                console.log("No channel selected");
            }
        }
    });
}

// A function that sends the current user's username to the server
function name() {
    // Call the getUname function to get the current user's username, and then continue with the following code
    getUname().then(function (uname) {
        // Check if the STOMP client is not null and the socket is open
        if (stompClient !== null && open) {
            console.log("Username: " + uname); // Log the current username to the console

            // Send a STOMP message to the server with the destination "/app/name" and the current username as the message body
            stompClient.send("/app/name", {}, JSON.stringify({'message': uname}));

            // Enable the "Send" button and disable the "Send Name" button
            $("#send").prop("disabled", false);
            $("#sendName").prop("disabled", true);

            // Replace the "Username:" text in the HTML with the current username
            $("#username_Update").replaceWith("Username: " + uname);
        }
    });
}


// This async function sends a POST request to the server to create a new channel with the specified name and user list.
async function createChannel(channelName, userList) {
    // A POST request is sent to the server with the channel name and user list in the request body.
    const response = await fetch('/createChannel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ channelName, userList })
    });
    // The response from the server is parsed as JSON and logged to the console.
    const data = await response.json();
    console.log(data);
    // The invite link for the new channel is returned.
    return data.inviteLink;
}

// This async function sends a POST request to the server to join a channel with the specified invite link.
async function joinChannel(inviteLink) {
    // A POST request is sent to the server with the invite link in the request body.
    const response = await fetch('/joinChannel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `inviteLink=${encodeURIComponent(inviteLink)}`
    });
    // The response from the server is returned as plain text and logged to the console.
    const text = await response.text();
    console.log(text);
    return text;
}

// This async function sends a GET request to the server to retrieve a list of all channels.
async function getChannels() {
    // A GET request is sent to the server to retrieve the channel list.
    const response = await fetch('/getChannels');
    // The response from the server is parsed as JSON and logged to the console.
    const channels = await response.json();
    console.log(channels);
    // The channel list is returned.
    return channels;
}

// Add this function to handle subscribing to a channel
async function subscribeToChannel(channelId) {
    if (stompClient && channelId) {
        const uname = await getUname();
        if (currentChannelId) {
            // unsubscribe from old channel
            await stompClient.unsubscribe(`/chat/login/${currentChannelId}`);
            await stompClient.unsubscribe(`/chat/hello/${currentChannelId}`);
            await stompClient.unsubscribe(`/chat/message/${currentChannelId}`);
            clearChatWindow(); // clear the chat window when switching channels
        }
        if (channelId !== currentChannelId) { // subscribe only if not already subscribed
            await stompClient.subscribe(`/chat/login/${channelId}`, function (message) {
                displayOldMessages(message, uname);
            });
            await stompClient.subscribe(`/chat/hello/${channelId}`, function (message) {
                console.log("sent");
            });
            // subscribe to message topic to receive new messages
            await stompClient.subscribe(`/chat/message/${channelId}`, function (message) {
                const msg = JSON.parse(message.body);
                if (msg.uname === uname) {
                    appendMessage2(msg, 'green'); // display messages from user in green
                } else {
                    appendMessage2(msg, 'red'); // display messages from others in red
                }
            });
            // send username to server on the new channel
            stompClient.send(`/app/name/${channelId}`, {}, JSON.stringify({ 'message': uname }));
            // set the current channel name in UI
            const channelName = $("#userChannels button[data-channel-id='" + channelId + "']").text();
            $("#channel-name").text(channelName);
        }
        currentChannelId = channelId; // set current channel id
    }
}



// This function clears all rows from the chat window.
function clearChatWindow() {
    $("#text").empty(); // remove all rows from the chat window
}

// This function renders the list of channels in the userChannels element in the HTML document.
function renderChannels(channels) {
    const channelList = document.getElementById("userChannels");
    channelList.innerHTML = "";

    // For each channel in the channels array, a button element is created and added to the channelList element.
    channels.forEach(channel => {
        const li = document.createElement("li");
        const button = document.createElement("button");
        button.textContent = channel.channel_name;
        button.setAttribute("data-channel-id", channel.channel_id);
        button.classList.add("channel-button"); // Add a class for styling
        // When the button is clicked, the user subscribes to the corresponding channel.
        button.addEventListener("click", () => {
            subscribeToChannel(channel.channel_id);
        });
        li.appendChild(button);
        channelList.appendChild(li);
    });
}

// This async function fetches the list of channels from the server and renders them in the userChannels element.
async function fetchChannels() {
    // The list of channels is retrieved from the server using the getChannels function.
    const userChannels = await getChannels();
    console.log("userChannels:", userChannels);
    // The list of channels is rendered in the userChannels element.
    renderChannels(userChannels);
    // If there are any channels in the list, the user is subscribed to the first channel after a short delay.
    if (userChannels.length > 0) {
        const channelId = userChannels[0].channel_id;
        setTimeout(() => {
            subscribeToChannel(channelId);
        }, 200); // delay for half a second before subscribing to the first channel
    }
}

// A function that runs when the page is loaded
$(function () {
    // Prevent the form from submitting when the "Enter" key is pressed
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    // Call the getUname function to get the current user's username, and then replace the "Username:" text in the HTML with the current username
    getUname().then(uname => $("#username_Update").replaceWith("Username: " + uname));
    // Call the name function to set up the "Send" button
    name();
    fetchChannels().then(() => {});
    // Set up event listeners for the "Connect", "Disconnect", and "Send" buttons
    $("#connect").click(connect);
    $("#disconnect").click(disconnect);
    $("#send").click(send);

// Add channel button click event
    $("#addChannel").on("click", async function () {
        const channelName = prompt("Enter channel name:");
        if (channelName) {
            const users = prompt("Enter usernames separated by commas:");
            if (users) {
                const userList = users.split(',').map(u => u.trim());
                const response = await createChannel(channelName, userList);
                console.log(response);
                await fetchChannels(); // Fetch and update the channel list after creating a new channel
            }
        }
    });

// Join channel button click event
    $("#joinChannel").on("click", async function () {
        const inviteLink = prompt("Enter invite link:");
        if (inviteLink) {
            const response = await joinChannel(inviteLink);
            console.log(response);
            await fetchChannels(); // Fetch and update the channel list after joining a channel
        }
    });

});