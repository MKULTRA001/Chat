var stompClient = null;
var open = false;
var uname = null;


function connect() {
    if (stompClient == null || !open) {
            //connect to the server endpoint
            var socket = new SockJS('/test-websocket');
            open = true;
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
            //enable name button
            $("#sendName").prop("disabled", false);
            $("#name").prop("readOnly", false);
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
        $("#name").prop("readOnly", true);
        uname = null;
    } else {
        console.log("Could not disconnect: no connection was established");
    }
}

function send() {
    stompClient.send("/app/test", {}, JSON.stringify({'message': uname + ': ' + $("#input").val()}));
    $("#input").val("");
}


function name() {
    if(stompClient !== null && open) {
        uname = $("#name").val();
        console.log("Username: " + uname);
        stompClient.send("/app/name", {}, JSON.stringify({'message': uname}));
        //enable text button and disable name button
        $("#send").prop("disabled", false);
        $("#sendName").prop("disabled", true);
        $("#name").prop("readOnly", true);
    }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { send(); });
    $( "#sendName" ).click(function () { name(); });
});

