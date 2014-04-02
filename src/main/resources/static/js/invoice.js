$( "#km, #allowance").keyup(function(e) {
    var allowance = parseFloat($("#allowance").val());
    var km = parseFloat($("#km").val());
    if(_.isFinite(allowance) && _.isFinite(km)){
        $("#millageAllowance").val(Number(km * allowance).toFixed(2));
    }
});

$("#invoice").change(function(){
    if($("#invoice").valid()){
        calculateTotal();
    }
});

$("#reminder").click(function(){
    if($(this).html() === "Purret"){
        invoiceAction("DELETE", $(this), "reminder")
    }else{
        invoiceAction("POST", $(this), "reminder")
    }
});
$("#settled").click(function(){
    if($(this).html() === "Betalt"){
        invoiceAction("DELETE", $(this), "settled")
    }else{
        invoiceAction("POST", $(this), "settled")
    }
});

function invoiceAction(method, button, action){
    button.attr("disabled", "disabled");
    $.ajax({
        type: method,
        url: window.location + "?action="+action,
        success: function(data){button.attr("class", data.buttonClass).html(data.buttonText);},
        complete: function(){button.removeAttr("disabled");}
    });
}

function calculateTotal(){
    var matchFee = orZero($("#matchFee"));
    var millageAllowance = orZero($("#millageAllowance"));
    var toll = orZero($("#toll"));
    var perDiem = orZero($("#perDiem"));
    $("#total").val(Number(matchFee + millageAllowance + toll + perDiem).toFixed(2));
}
$('#invoice').validate({
    errorPlacement:function (error, element) {
        error.appendTo(element.nextAll("span"));
    },
    rules:{
        matchFee:{
            required:true,
            digits:true
        },
        toll:{
            number:true
        },
        perDiem:{
            number:true
        },
        total:{
            number:true
        }
    },
    messages:{
        matchFee: "Kamphonorar må være et heltall",
        perDiem: "Diett må være heltall",
        toll: "Bompenger må være et tall",
        total: "Total må være et tall"
    }
});

function orZero(element){
    var value = parseFloat(element.val());
    if (_.isNaN(value)){
        return 0;
    }else{
        return value;
    }
}