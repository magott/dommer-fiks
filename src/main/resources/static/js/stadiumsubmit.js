$('form').submit(function() {
  $(this).find("button[type='submit']").prop('disabled',true).text("Sender...");
});