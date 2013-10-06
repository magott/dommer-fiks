function fetchForecast(){
    $("#weather").empty();
    var opts = { lines: 8, length: 4, width: 3, radius: 5, left: 10}
    var target = document.getElementById('spinner');
    var spinner = new Spinner(opts).spin(target);
    $.ajax({
        type: "GET",
        url: window.location.href.replace(/\/$/, "")+"/forecast",
        success: function(data){
            $("#weather").html(data)
        },
        error: function(error){
            if(error.status == 404){
                $("#weather").html(error.responseText);
            }else{
                $("#weather").html("<div>Problemer med v&aelig;rmeldingstjenesten</div>");
            }
        },
        complete: function(){
            spinner.stop();
        }
    });
}

function fetchStadiumLink(){
    var venueCell = $("#venue")
    var locationDiv = $("#location")
    var stadiumName = venueCell.text();
    var matchFiksId = window.location.href.replace(/\/$/, "").split('/').pop();
    $.get("/stadium/?stadiumName="+encodeURIComponent(stadiumName)+"&matchid="+matchFiksId, function() {
    }).done(function(data) { locationDiv.append( '&nbsp;<a href='+data.link +' target=_blank>' + data.name + '</a>' );})
        .fail(function(xhr) {
            if(xhr.status == "404"){
                try{
                    var data = $.parseJSON(xhr.responseText);
                    locationDiv.append( '&nbsp;<a href='+data.link +'>' + data.name + '</a>' );
                }catch(e){}
            }
        });
}

function bindSmsButton(){
    $('.smscheck').click(function(){
        $("#sms").prop("href", createSmsLink());
    });
    createSmsLink();
}

function createSmsLink(){
    var checkedVals = $('.smscheck:checked').map(function() {
        return this.value;
    }).get();
    return "sms:" + checkedVals.join(",");
}
