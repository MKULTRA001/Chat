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

