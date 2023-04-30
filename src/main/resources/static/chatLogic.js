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
    console.log("in append message: ");
    // Append a new row to the table with the specified background color and message content
    let sanitized = $('<div>').text(message).html();
    console.log("sanitized: " + sanitized);
    $("#text").append(`<tr style = 'color: ${color}' data-id = 'id' data-uid = 'uid' data-ts = 'ts'><td>${sanitized}</td></tr>`);
}

// Metadata included message; send message as full json object
function appendMessage2(packet, color) {
    console.log("in append message 2: ");
    // The message is first sanitized to prevent any potential security vulnerabilities
    let sanitized = $('<div>').text(packet.message).html();
    // A timestamp is also added to the message
    let ts = 'data-ts = \"' + packet.time + '\"';
    // The message is then appended to the chat window using jQuery
    console.log("sanitized: " + sanitized);
    $("#text").append(`<tr style = 'color: ${color}' data-id = ${packet.messageId} data-uid = ${packet.uname} ${ts}><td>${sanitized}</td></tr>`);
}
//
function isEncryptedMessage(message) {
    try {
        return btoa(atob(message)) === message;
    } catch (error) {
        return false;
    }
}
// This function is used to display old messages in the chat window.
async function displayOldMessages(message, uname, currentChannelId) {
    // The response message is parsed as JSON
    const response = JSON.parse(message.body);
    // The old messages are then extracted and split based on newlines
    const oldMessages = response.message.split('\n');
    console.log("old messages: " + oldMessages);
    const privateKey = await loadCurve25519KeyFromLocalStorage("privateKey");
    const publicKey = await getRecipientsPublicKey(await getChannelCreator(currentChannelId));
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
                    if (msgArray[3] === "ENC") {
                        const encryptedKey = await getEncryptedKey(uname, currentChannelId);
                        const symKey = await KeyDecryption(encryptedKey, publicKey, privateKey);
                        importSymmetricKey(symKey).then(async (importedKey) => {
                            console.log('Imported symmetric key:', importedKey);
                            const decryptedMessage = await decryptDirectMessage(msgArray[2], msgArray[1], importedKey);
                            if(msgArray[0] !== uname) {
                                appendMessage(`${msgArray[0]}: ${decryptedMessage}`, uname === msgArray[0] ? 'var(--accents-0)' : 'var(--text)');
                            }
                            else{
                                appendMessage(`${msgArray[0]}: ${decryptedMessage}`, uname === msgArray[0] ? 'var(--accents-0)' : 'var(--text)');
                            }
                        });
                    }
                    else{
                        appendMessage(oldMessage, uname === msgArray[0] ? 'var(--accents-0)' : 'var(--text)');
                    }
                }
            }
        }
    }
    // The chat window is scrolled to the bottom after a short delay.
    setTimeout(function(){
        let m = document.querySelector('.chat-body');
        m.scrollTop =  1000;//m.scrollHeight;
    }, 500);
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
    if (currentChannelId) {
        // The user's name is retrieved asynchronously from the server.
        getUname().then(async function (uname) {
            const response = await fetch(`/checkPrivateChannel/${currentChannelId}`);
            console.log(response);
            // The username display in the chat window is updated.
            $("#username_Update").replaceWith("Username: " + uname);
            // The value of the message input field is retrieved.
            const value = $("#input").val();
            console.log("Value: " + value);
            if (response.ok) {
                const isPrivate = await response.json();
                console.log(isPrivate);
                if (isPrivate) {
                    await sendDirectMessage(currentChannelId, value);
                    document.getElementById("input").value = "";
                } else {
                    // If no target user is specified, the message is sent to the current channel.
                    // The destination is set to the current channel.
                    const destination = `/app/chat/${currentChannelId}`;
                    // A message is sent to the channel with the sender's name and the message text.
                    stompClient.send(destination, {}, JSON.stringify({
                        'message': `${uname}: ${value}`,
                        'channel': currentChannelId
                    }));
                    document.getElementById("input").value = "";
                }
            }
            else{
                console.log("No Response");
            }
        });
    }
    else {
        console.log("No channel selected");
    }
}
async function getEncryptedKey(username, channelId) {
    try {
        const response = await fetch(`/getEncryptedSymmetricKey/${username}/${channelId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
        });

        if (response.ok) {
            const encryptedSymmetricKey = await response.text();
            return encryptedSymmetricKey;
        } else {
            console.error(`Error getting encrypted symmetric key: ${response.status}`);
            return null;
        }
    } catch (error) {
        console.error('Error fetching encrypted symmetric key:', error);
        return null;
    }
}
async function getChannelCreator(channelId) {
    try {
        const response = await fetch(`/getChannelCreator/${channelId}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const creatorUsername = await response.text();
        console.log("Channel creator:", creatorUsername);
        return creatorUsername;
    } catch (error) {
        console.error("Error fetching channel creator:", error);
    }
}

async function sendDirectMessage(currentChannelId, message) {
    const users = await getChannelUsers(currentChannelId);
    const uname = await getUname();
    console.log("Send DM: " + uname);
    const encryptedKey = await getEncryptedKey(uname, currentChannelId);
    console.log(encryptedKey)
    console.log("Current Channel ID: " + currentChannelId);
    console.log("Users: " + users);
    // Check if the list has 2 users
    if (users.length === 2) {
        // Find the target user (the one that isn't 'uname')
        const target = users.find(user => user !== uname);
        if (target) {
            const destination = `/app/chat/private/${uname}/${target}`;
            const publicKey = await getRecipientsPublicKey(await getChannelCreator(currentChannelId));
            const privateKey = await loadCurve25519KeyFromLocalStorage("privateKey");
            const symKey = await KeyDecryption(encryptedKey, publicKey, privateKey);
            importSymmetricKey(symKey).then(async (importedKey) => {
                console.log('Imported symmetric key:', importedKey);
                // Encrypt the message
                console.log("Message: " + message);
                const encryptedMessage = await directMessageEncryption(message, importedKey);
                // Print the encrypted message
                console.log("Encrypted message:", encryptedMessage);
                //
                //  const encryptedMessage = await directMessageEncryption(privateKey, publicKey, message);
                // console.log("Encrypted Message: " + encryptedMessage);
                stompClient.send(destination, {}, JSON.stringify({
                    'message': `${uname}: ${encryptedMessage.encryptedData}`,
                    'channel': currentChannelId,
                    'iv': encryptedMessage.iv,
                }));
            });
        } else {
            console.error("Error: Target user not found.");
        }
    } else {
        console.error("Error: The channel must have exactly 2 users.");
    }
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
async function createPrivateChannel(user2) {
    const user1 = await getUname();
    console.log(user2);
    // Generate a symmetric key
    const symmetricKey = await generateSymmetricKey();

    // Get user1's private key and public key, and user2's public key
    const user1PrivateKey = await loadCurve25519KeyFromLocalStorage("privateKey");
    const user1PublicKey = await getRecipientsPublicKey(user1);
    const user2PublicKey = await getRecipientsPublicKey(user2);

    // Encrypt the symmetric key with user1's private key and user2's public key
    const user1EncryptedSymmetricKey = await KeyEncryption(symmetricKey, user1PublicKey, user1PrivateKey);
    const user2EncryptedSymmetricKey = await KeyEncryption(symmetricKey, user2PublicKey, user1PrivateKey);

    // A POST request is sent to the server with the encrypted symmetric keys in the request body.
    const response = await fetch(`/createPrivateChannel/${user1}/${user2}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ user1EncryptedSymmetricKey, user2EncryptedSymmetricKey })
    });
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
async function getChannelUsers(channelId) {
    try {
        console.log("Channel ID get users: " + channelId);
        const response = await fetch(`/getChannelUsers/${channelId}`);

        if (response.ok) {
            const users = await response.json();
            console.log(users);
            // The user list is returned.
            return users;
        } else {
            console.error(`Error fetching channel users: ${response.statusText}`);
            return [];
        }
    } catch (error) {
        console.error(`Error fetching channel users: ${error.message}`);
        return [];
    }
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
    let Subscription1;
    let Subscription2;
    let Subscription3;
    if (stompClient && channelId) {
        const uname = await getUname();
        if (currentChannelId) {
            if (Subscription1) {
                await Subscription1.unsubscribe();
                Subscription1 = null;
            }
            if (Subscription2) {
                await Subscription2.unsubscribe();
                Subscription2 = null;
            }
            if (Subscription3) {
                await Subscription3.unsubscribe();
                Subscription3 = null;
            }
            clearChatWindow();
        }
        const response = await fetch(`/checkPrivateChannel/${channelId}`);
        console.log(response);
        if (response.ok) {
            const isPrivate = await response.json();
            console.log(isPrivate);
            if (isPrivate) {
                console.log("channel id: " + channelId);
                const privateChannelName = $("#userChannels button[data-channel-id='" + channelId + "']").text();
                $("#channel-name").text(privateChannelName);
                const [_, user1, user2] = privateChannelName.match(/DM:(\w+) and (\w+)/);
                Subscription1 = await stompClient.subscribe(`/chat/login/${channelId}`, async function (message) {
                    await displayOldMessages(message, uname, currentChannelId);
                    await Subscription1.unsubscribe();
                });
                // Subscribe to messages from user1 to user2
                Subscription2 = await stompClient.subscribe(`/chat/message/private/${user1}/${user2}`, async function (message) {
                    console.log("2user1: " + user1);
                    console.log("user2: " + user2);
                    const messageId = JSON.parse(message.body).messageId;
                    if ($("#text tr[data-id='" + messageId + "']").length === 0) {
                        const msg = await handlePrivateMessage(message, uname,  JSON.parse(message.body).uname);
                        console.log(msg)
                        if (msg.uname === uname) {
                            appendMessage2(msg, 'green');
                        } else {
                            appendMessage2(msg, 'red');
                        }
                    }
                });
                Subscription3 = await stompClient.subscribe(`/chat/message/private/${user2}/${user1}`, async function (message) {
                    console.log("2user1: " + user1);
                    console.log("user2: " + user2);
                    const messageId = JSON.parse(message.body).messageId;
                    if ($("#text tr[data-id='" + messageId + "']").length === 0) {
                        const msg = await handlePrivateMessage(message, uname,  JSON.parse(message.body).uname);
                        console.log(msg)
                        if (msg.uname === uname) {
                            appendMessage2(msg, 'green');
                        } else {
                            appendMessage2(msg, 'red');
                        }
                    }
                });
                stompClient.send(`/app/name/${channelId}`, {}, JSON.stringify({'message': uname}));
            }else {
                Subscription1= await stompClient.subscribe(`/chat/login/${channelId}`, function (message) {
                    displayOldMessages(message, uname, currentChannelId);
                });
                Subscription2 = await stompClient.subscribe(`/chat/message/${channelId}`, function (message) {
                    const msg = JSON.parse(message.body);
                    if (msg.uname === uname) {
                        appendMessage2(msg, 'green');
                    } else {
                        appendMessage2(msg, 'red');
                    }
                });
                Subscription3 = stompClient.send(`/app/name/${channelId}`, {}, JSON.stringify({'message': uname}));
                const channelName = $("#userChannels button[data-channel-id='" + channelId + "']").text();
                $("#channel-name").text(channelName);
            }
        }
        currentChannelId = channelId;
    }
}

async function handlePrivateMessage(message, uname, target) {
    const encryptedKey = await getEncryptedKey(uname, currentChannelId);
    const publicKey = await getRecipientsPublicKey(await getChannelCreator(currentChannelId));
    const privateKey = await loadCurve25519KeyFromLocalStorage("privateKey");
    const symKey = await KeyDecryption(encryptedKey, publicKey, privateKey);
    const messageId = JSON.parse(message.body).messageId;
    const iv = JSON.parse(message.body).iv;
    return importSymmetricKey(symKey).then(async (importedKey) => {
        console.log('Imported symmetric key:', importedKey);
        // Check if the message has already been displayed
        if ($("#text tr[data-id='" + messageId + "']").length === 0) {
            const msg = JSON.parse(message.body);
            console.log("Encrypted message:", msg.message);
            // Extract the encrypted message from the msg.message property
            const encryptedMessage = msg.message.split(': ')[1];

            try {
                const decryptedMessage = await decryptDirectMessage(iv, encryptedMessage,importedKey);
                console.log("Decrypted message:", decryptedMessage);

                // Update the msg.message property with the decrypted message
                msg.message = `${msg.uname}: ${decryptedMessage}`;

                console.log(msg);

                return msg;
            } catch (error) {
                console.error("Error:", error.message);
            }
        }
    });
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
    channels.forEach((channel) => {
        const li = document.createElement("li");
        const button = document.createElement("button");
        button.textContent = channel.channel_name;
        button.setAttribute("data-channel-id", channel.channel_id);
        button.classList.add("channel-button"); // Add a class for styling
        button.onclick = async () => {
            // Remove the 'selected' class from all channel buttons
            const allChannelButtons = document.getElementsByClassName("channel-button");
            Array.from(allChannelButtons).forEach((btn) => btn.classList.remove("selected"));

            // Add the 'selected' class to the clicked channel button
            button.classList.add("selected");

            // Subscribe to the corresponding channel
            const channelId = button.getAttribute("data-channel-id");
            await subscribeToChannel(channelId);
        };
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
async function loadCurve25519KeyFromLocalStorage(keyName) {
    const keyArray = JSON.parse(localStorage.getItem(keyName));
    const key = new Uint8Array(keyArray);
    console.log(`Loaded ${keyName} from local storage:`, key);

    // Send the public key as a JSON object if it's the public key
    if (keyName === "publicKey") {
        const data = {
            publicKey: Array.from(key) // Convert Uint8Array to a regular array
        };
        await fetch('/savePublicKey', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
    }

    return key;
}

async function loadKeyFromLocalStorage(keyName, keyType) {
    const jwk = JSON.parse(localStorage.getItem(keyName));
    let importOptions;
    console.log(`Loaded ${keyName} from local storage:`, jwk);
    if (keyType === 'AES') {
        importOptions = { name: "AES-GCM", length: 256 };
    } else if (keyType === 'Ed25519') {
        importOptions = {
            name: "Ed25519",
        };
    } else {
        throw new Error('Invalid key type.');
    }
    return await window.crypto.subtle.importKey(
        "jwk",
        jwk,
        importOptions,
        true,
        keyType === 'AES' ? ["encrypt", "decrypt"] : ["decrypt"]
    );
}
async function getRecipientsPublicKey(username) {

    const response = await fetch(`/getPublicKey/${username}`);
    if (!response.ok) {
        throw new Error(`Error getting public key: ${response.status} ${response.statusText}`);
    }
    const publicKeyBase64 = await response.text();
    return new Uint8Array(atob(publicKeyBase64).split('').map(char => char.charCodeAt(0)));
}
async function importSymmetricKey(rawKey) {
    return await window.crypto.subtle.importKey(
        'raw',
        rawKey.slice(32), // Use the second half of the decrypted key
        {name: 'AES-GCM', length: 256},
        true,
        ['encrypt', 'decrypt']
    );
}
async function KeyEncryption(symmetricKey, recipientPublicKey, senderPrivateKey) {
    console.log('key encryption: ', symmetricKey, recipientPublicKey, senderPrivateKey)
    const encoder = new TextEncoder();
    const arrayBuffer = await crypto.subtle.exportKey('raw', symmetricKey);
    const keyUint8Array = new Uint8Array(arrayBuffer);

    const nonce = nacl.randomBytes(nacl.box.nonceLength);

    const encryptedMessage = nacl.box(keyUint8Array, nonce, recipientPublicKey, senderPrivateKey);

    const encryptedMessageWithNonce = new Uint8Array(nacl.box.nonceLength + encryptedMessage.length);
    encryptedMessageWithNonce.set(nonce);
    encryptedMessageWithNonce.set(encryptedMessage, nonce.length);

    const uint8ToString = (buf) => String.fromCharCode.apply(null, buf);
    return btoa(uint8ToString(encryptedMessageWithNonce));
}
async function KeyDecryption(encryptedSymmetricKey, senderPublicKey, recipientPrivateKey) {
    const stringToUint8 = (str) => new Uint8Array(str.split('').map((char) => char.charCodeAt(0)));
    const encryptedMessageWithNonce = stringToUint8(atob(encryptedSymmetricKey));
    const nonce = encryptedMessageWithNonce.slice(0, nacl.box.nonceLength);
    const encryptedMessage = encryptedMessageWithNonce.slice(nacl.box.nonceLength);

    const decryptedUint8Array = nacl.box.open(encryptedMessage, nonce, senderPublicKey, recipientPrivateKey);

    if (!decryptedUint8Array) {
        throw new Error("Decryption failed");
    }

    return decryptedUint8Array.buffer;
}
async function directMessageEncryption(message, symKey) {
    const encoder = new TextEncoder();
    const data = encoder.encode(message);
    const iv = crypto.getRandomValues(new Uint8Array(12));

    const encryptedData = await crypto.subtle.encrypt(
        {
            name: "AES-GCM",
            iv: iv,
        },
        symKey,
        data
    );
    // Convert ArrayBuffer to Base64
    const arrayBufferToBase64 = (buffer) => {
        const binary = String.fromCharCode.apply(null, new Uint8Array(buffer));
        return btoa(binary);
    };

    const base64Iv = arrayBufferToBase64(iv.buffer);
    const base64EncryptedData = arrayBufferToBase64(encryptedData);

    return { iv: base64Iv, encryptedData: base64EncryptedData };
}

async function decryptDirectMessage(base64Iv, base64EncryptedData, symKey) {
    // Convert Base64 to ArrayBuffer
    const base64ToArrayBuffer = (base64) => {
        const binary = atob(base64);
        const buffer = new ArrayBuffer(binary.length);
        const view = new Uint8Array(buffer);
        for (let i = 0; i < binary.length; i++) {
            view[i] = binary.charCodeAt(i);
        }
        return buffer;
    };

    const ivArrayBuffer = base64ToArrayBuffer(base64Iv);
    const encryptedDataArrayBuffer = base64ToArrayBuffer(base64EncryptedData);

    const decryptedData = await crypto.subtle.decrypt(
        {
            name: "AES-GCM",
            iv: new Uint8Array(ivArrayBuffer),
        },
        symKey,
        encryptedDataArrayBuffer
    );

    const decoder = new TextDecoder();
    return decoder.decode(decryptedData);
}

async function generateSymmetricKey() {
    return await crypto.subtle.generateKey(
        {
            name: "AES-GCM",
            length: 256,
        },
        true,
        ["encrypt", "decrypt"]
    );
}

async function encryptMessage(key, message) {
    const encoder = new TextEncoder();
    const data = encoder.encode(message);
    const iv = crypto.getRandomValues(new Uint8Array(12));

    const encryptedData = await crypto.subtle.encrypt(
        {
            name: "AES-GCM",
            iv: iv,
        },
        key,
        data
    );

    return { iv, encryptedData };
}

async function decryptMessage(key, iv, encryptedData) {
    const decryptedData = await crypto.subtle.decrypt(
        {
            name: "AES-GCM",
            iv: iv,
        },
        key,
        encryptedData
    );

    const decoder = new TextDecoder();
    const message = decoder.decode(decryptedData);
    return message;
}

function loadTweetNaCl(callback) {
    const script = document.createElement('script');
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/tweetnacl/1.0.3/nacl.min.js';
    script.onload = function() {
        if (typeof callback === 'function') {
            callback();
        }
    };
    document.head.appendChild(script);
}

// A function that runs when the page is loaded
$(async function () {
    loadTweetNaCl();
    const keyA = await loadKeyFromLocalStorage("keyA", 'AES');
    const keyB = await loadKeyFromLocalStorage("keyB", 'AES');
    const privateKey = loadCurve25519KeyFromLocalStorage("privateKey");
    const publicKey = loadCurve25519KeyFromLocalStorage("publicKey");

    // Prevent the form from submitting when the "Enter" key is pressed
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    // Call the getUname function to get the current user's username, and then replace the "Username:" text in the HTML with the current username
    getUname().then(uname => $("#username_Update").replaceWith("Username: " + uname));
    // Call the name function to set up the "Send" button
    name();
    fetchChannels().then(() => {
    });
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

    // Add Create dm button click event
    $("#CreateDM").on("click", async function () {
        const username = prompt("Enter username:");
        if (username) {
            const response = await createPrivateChannel(username);
            console.log(response);
            await fetchChannels(); // Fetch and update the channel list after creating a new channel
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
document.addEventListener('DOMContentLoaded', async () => {
    await fetchChannels();
});
