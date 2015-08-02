$( document ).ready(function() {
    $.get("/messages/unreadcount?json", function() {
        }).done(function(data) {
            if(data.count > 0){
                $('.unread').html(data.count);
            }
         });
});