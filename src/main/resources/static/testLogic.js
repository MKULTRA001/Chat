// Declare a variable to store the Stomp client object, initially set to null
let stompClient = null;

// Declare a boolean variable to keep track of whether the socket is open, initially set to false
let open = false;

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
    let sanitized = $('<div>').text(packet.message).html();
    $("#text").append(`<tr style = 'color: ${color}' data-id = ${packet.messageId} data-uid = ${packet.uname} data-ts = ${packet.time}><td>${sanitized}</td></tr>`);
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

// A function that establishes a WebSocket connection to the server and sets up subscriptions to different endpoints
function connect() {
    // Call the getUname function to get the current user's username, and then continue with the following code
    getUname().then(function (uname) {
        // Check if the Stomp client is null or the socket is not open
        if (stompClient == null || !open) {
            // Create a new SockJS object with the endpoint URL
            const socket = new SockJS('/chat-websocket');
            open = true; // Set the socket open variable to true

            // Bind the STOMP client to the socket
            stompClient = Stomp.over(socket);

            // Connect to the server and set up subscriptions to different endpoints
            stompClient.connect({}, function (frame) {
                console.log('Connected: ' + frame); // Log a message to the console when the connection is established

                // Subscription to the '/chat/login' endpoint
                stompClient.subscribe('/chat/login', function (message) {
                    // Parse the message body and split it into individual messages
                    const oldMessages = JSON.parse(message.body).message.split('\n');
                    // Loop through each message and append it to the chat window
                    for (const oldMessage of oldMessages) {
                        if (oldMessage) {
                            const msgArray = oldMessage.split(':');
                            appendMessage(oldMessage, uname === msgArray[0] ? 'green' : 'red');
                        }
                    }
                });

                // Subscription to the '/chat/hello' endpoint
                stompClient.subscribe('/chat/hello', function (message) {
                    // Parse the message body and split it into individual messages
                    // const msg = JSON.parse(message.body).message;
                    // const msgArray = msg.split(':');
                    // Append the message to the chat window
                    // appendMessage(msg, uname === msgArray[0] ? 'green' : 'red');
                    appendMessage2(JSON.parse(message.body), 'green');
                });

                // Subscription to the '/chat/private' endpoint with the user's own username appended
                stompClient.subscribe(`/chat/private/${uname}`, function (message) {
                    // Parse the private message and append it to the chat window in italics
                    const privateMsg = JSON.parse(message.body).message;
                    $("#text").append(`<tr><td><i>${privateMsg}</i></td></tr>`);
                });

                name(); // Call the name function to set up the "Send" button
            });

            // Enable the "Send" button
            $("#send").prop("disabled", false);
        } else {
            console.log("Could not connect: already connected");
        }
    });
}
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
// A function that sends a message to the server
function send() {
    // Call the getUname function to get the current user's username, and then continue with the following code
    getUname().then(function (uname) {
        // Replace the "Username:" text in the HTML with the current username
        $("#username_Update").replaceWith("Username: " + uname);

        // Get the input text from the "input" element
        const value = $("#input").val();

        // Check if the input starts with "/msg" (indicating a direct message)
        if (value.startsWith('/msg')) {
            // Split the input text into an array of strings, with the first element being "/msg"
            // and the second element being the target user's name
            const [, target, ...payload] = value.split(' ');
            const message = payload.join(' '); // Join the remaining elements of the array into a single string (the message content)

            console.log("Direct Message to: " + target); // Log a message to the console indicating that a direct message is being sent to the target user

            // Send a STOMP message to the server with the target user's username as the destination
            stompClient.send(`/app/chat/private/${target}`, {}, JSON.stringify({'message': `(${uname} messaged you: ${message})`}));

            // Send a STOMP message to the server with the current user's username as the destination (to show the message in the current user's chat window)
            stompClient.send(`/app/chat/private/${uname}`, {}, JSON.stringify({
                'message': `(you messaged ${
                    target}: ${message})`
            }));
        } else {
            // Send a STOMP message to the server with the destination "/app/chat" (to broadcast the message to all users)
            stompClient.send("/app/chat", {}, JSON.stringify({'message': `${uname}: ${value}`}));
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
    // Set up event listeners for the "Connect", "Disconnect", and "Send" buttons
    $("#connect").click(connect);
    $("#disconnect").click(disconnect);
    $("#send").click(send);
});


//Hide "Connect to Chat Room" button and display chat messages
function displaychat() {
    document.getElementById("chat").style.visibility = "visible";
    document.getElementById("connect").style.display = "none";
}