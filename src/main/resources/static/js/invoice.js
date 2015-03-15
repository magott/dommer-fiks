$( "#km, #kmMultiplier").keyup(function(e) {
    var kmMultiplier = parseFloat($("#kmMultiplier").val());
    var km = parseFloat($("#km").val());
    if(_.isFinite(kmMultiplier) && _.isFinite(km)){
        $("#millageAllowance").val(Number(km * kmMultiplier).toFixed(2));
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

$('#delete').click(function() {
    $.ajax({
        type: 'DELETE',
        success: function(result){
            window.location.href="/invoice/";
        }
    });
});
$.validator.setDefaults({
    highlight: function(element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function(element) {
        $(element).closest('.form-group').removeClass('has-error');
    },
    errorElement: 'p',
    errorClass: 'help-block',
    errorPlacement: function(error, element) {
        if(element.parent('.input-group').length) {
            error.insertAfter(element.parent());
        } else {
            error.insertAfter(element);
        }
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
    rules:{
        matchFee:{
            required:true,
            digits:true
        },
        toll:{
            number:true
        },
        km:{
            number:true
        },
        kmMultiplier:{
            number:true
        },
        millageAllowance:{
            number:true
        },
        perDiem:{
            digits:true
        },
        total:{
            number:true
        }
    },
    messages:{
        matchFee: {
            required: "Kamphonorar må fylles ut",
            digits: "Kamphonorar må være et heltall"
        },
        millageAllowance: "Kilometergodtgjørelse (sum) må være et tall",
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

var app = angular.module('invoiceapp', []);
app.controller("ctrl", function($scope, $http) {
     $scope.invoices = [];
     $scope.activeYear = 0;
     $scope.loadInvoices = function(year) {
        var httpRequest = $http({
            method: 'GET',
            url: '/invoice?year='+year,
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status) {
            $scope.activeYear = year;
            $scope.invoices = data;
        });
    };
    $scope.isShowingFor = function(year){
        return year === $scope.activeYear;
    }
    $scope.sumSettled = function(fitleredInvoices) {
        return _.reduce(fitleredInvoices, function(acc, i){
            if(i.settled == true){
                return acc + i.total;
            }else {
                return acc;
            }
        }, 0).toFixed(2);
    };
    $scope.sumTotal = function(fitleredInvoices) {
        return _.reduce(fitleredInvoices, function(acc, i){
           return acc + i.total;
        }, 0).toFixed(2);
    };
    $scope.sumUnsettled = function(fitleredInvoices) {
        return _.reduce(fitleredInvoices, function(acc, i){
            if(i.settled === false){
                return acc + i.total;
            }else {
                return acc;
            }
        }, 0).toFixed(2);
    };

});
