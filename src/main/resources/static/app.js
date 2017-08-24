var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#stream").show();
    }
    else {
        $("#stream").hide();
    }
    $("#output").html("");
}

function connect() {
    var socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        startSparkJob();
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/stream/output', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
        stopSparkJob();
    }
    setConnected(false);
    console.log("Disconnected");
}

function startSparkJob() {
   $.ajax({
       url: '/stream/start',
       type: 'GET'
   });
}
function stopSparkJob() {
   $.ajax({
       url: '/stream/stop',
       type: 'GET'
   });
}

function showGreeting(message) {
    $("#output").prepend("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
});