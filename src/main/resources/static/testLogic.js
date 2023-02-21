let stompClient = null;
let open = false;


function connect() {
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
                stompClient.subscribe('/chat/hello', function (message) {
                    //this just expands the text section with a new one for the line
                    $("#text").append("<tr><td>" + JSON.parse(message.body).message + "</td></tr>");
                });
            });
            //enable name button
            $("#send").prop("disabled", false);
    } else {
        console.log("Could not connect: already connected");
    }
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
        stompClient.send("/app/chat", {}, JSON.stringify({'message': response + ': ' + value}));
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
