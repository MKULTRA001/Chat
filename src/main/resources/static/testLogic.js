let stompClient = null;
let open = false;


function connect() {
    getUname().then(function (response) {
        if (stompClient == null || !open) {
                //connect to the server endpoint
            const socket = new SockJS('/chat-websocket');
            open = true;
                //bind STOMP message to the socket
                stompClient = Stomp.over(socket);
                //connect and set up subscription to hello endpoint
                stompClient.connect({}, function (frame) {
                    console.log('Connected: ' + frame);
                    //"function" is the callback: this is executed every time a message is found on this channel
                    //this is for broadcast
                    stompClient.subscribe('/chat/hello', function (message) {
                        //this just expands the text section with a new one for the line
                        $("#text").append("<tr><td>" + JSON.parse(message.body).message + "</td></tr>");
                    });
                    //this is for dm
                    stompClient.subscribe('/chat/private/'+response, function (message) {
                        //this just expands the text section with a new one for the line
                        $("#text").append("<tr><td><i>" + JSON.parse(message.body).message + "</i></td></tr>");
                    });
                });
                //enable name button
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
        //disable text button and name button
        $("#send").prop("disabled", true);
        $("#sendName").prop("disabled", true);
    } else {
        console.log("Could not disconnect: no connection was established");
    }
}

function send() {
    getUname().then(function (response) {
        $("#username_Update").replaceWith("Username: " + response);
        let value = $("#input").val();
        if (value.search('/msg') == 0){
            //if dm command
            let [, target, ...payload] = value.split(' ');
            payload = payload.join(' ');
            console.log("Direct Message to: " + target);
            stompClient.send("/app/chat/private/" + target, {}, JSON.stringify({'message': '(' + response + 'messaged you: ' + payload + ')'}));
            stompClient.send("/app/chat/private/" + response, {}, JSON.stringify({'message': '(you messaged ' + target + ': ' + payload + ')'}));
        } else {
            //otherwise:
            stompClient.send("/app/chat", {}, JSON.stringify({'message': response + ': ' + value}));
        }
    });
}


function name() {
    getUname().then(function (response) {
        const uname = response;
        if (stompClient !== null && open) {
            console.log("Username: " + uname);
            stompClient.send("/app/name", {}, JSON.stringify({'message': uname}));
            //enable text button and disable name button
            $("#send").prop("disabled", false);
            $("#sendName").prop("disabled", true);
            $("#username_Update").replaceWith("Username: " + uname);
        }
    });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    getUname().then(r => $("#username_Update").replaceWith("Username: " + r));
    name();
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { send(); });

});

async function getUname () {
    let response = await fetch('/myUsername').then(response => response.text());
    console.log(response)
    return response;
}
