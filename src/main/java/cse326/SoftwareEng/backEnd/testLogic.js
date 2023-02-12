/*
This file assumes a few things, taken from the Spring setup framework's HTML file and associated js socket logic
1: there is a connect button with id "connect"
2: there is a disconnect button with id "disconnect"
3: there is an input field with id "input"
4: there is a send message button with id "send"
5: there is a table with tbody id "text"
If you look at the html code at https://spring.io/guides/gs/messaging-stomp-websocket/ you'll see these w/ different names:
    text is called greetings
    input is called name
they also have an extra function to toggle buttons and stuff, but overall the structure between the two is the same.

The example also has 2 message container classes, which I consider unnecessary, so I didn't do that.
 */

var stompClient = null;

function connect() {
    //connect to the server endpoint
    var socket = new SockJS('/test-websocket');
    //bind STOMP messager to the socket
    stompClient = Stomp.over(socket);
    //connect and set up subscription to hello endpoint
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        //"function" is the callback: this is executed every time a message is found on this channel
        stompClient.subscribe('/test/hello', function (message) {
            //this just expands the text section with a new one for the line
            $("#text").append("<tr><td>" + JSON.parse(message.body).message + "</td></tr>");
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log("Disconnected");
    } else {
        console.log("Disconnect attempted when already disconnected");
    }
}

function send() {
    stompClient.send("/app/test", {}, JSON.stringify({'message': $("#input").val()}));
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { send(); });
});

