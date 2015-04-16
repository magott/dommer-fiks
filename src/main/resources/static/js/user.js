$("#zip").focusout(fetchAndSetPoststed);
function fetchAndSetPoststed() {
     var inputField = $('#zip');
     var outputElement = $('#city');
     if (inputField.val().length == 4) {
         $.getJSON('https://fraktguide.bring.no/fraktguide/api/postalCode.json?pnr='+ inputField.val() +'&callback=?',
             function(data){
                 if (data.valid) {
                     outputElement.val(data.result);
                 }
                 else {
                     outputElement.val('');
                 }
             });
     }
     else {
         outputElement.text('');
     }
 }