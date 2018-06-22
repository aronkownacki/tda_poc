function onTestClick(id, url){
        $(id).html("performing test..").css('color', 'blue');
        $.ajax({
            url: url,
            success: function(data, status){
                $(id).html("Status: " + status + "<br/>Result: <br/><pre>"+ JSON.stringify(data, null, 2) + "</pre>").css('color', 'black');
            },
            error: function(xhr){
                $(id).html("Status: " + xhr.statusText).css('color', 'red');
            }
        });
}

$(document).ready(function(){
    $("#tst0").click(function(){onTestClick("#res0", "/test/sparkContext")});
    $("#tst1").click(function(){onTestClick("#res1", "/test/sparkContext")});
    $("#tst2").click(function(){onTestClick("#res2", "/test/h2")});
    $("#tst3").click(function(){onTestClick("#res3", "/test/webSocket")});
    $("#tst4").click(function(){onTestClick("#res4", "/test/mongo")});
    $("#tst5").click(function(){onTestClick("#res5", "/test/lookup")});
    $("#tst6").click(function(){
        statusId = $("#in6").val();

        onTestClick("#res6", "/test/lookup/" + statusId)});
});