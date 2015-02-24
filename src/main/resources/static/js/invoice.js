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
     $scope.loadInvoices = function(year) {
        var httpRequest = $http({
            method: 'GET',
            url: '/invoice?year='+year,
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status) {
            $scope.invoices = data;
        });
    };
    $scope.sumSettled = function(fitleredInvoices) {
        return _.reduce(fitleredInvoices, function(acc, i){
            if(i.settled == true){
                return acc + i.total;
            }else {
                return acc;
            }
        }, 0);
    };
    $scope.sumTotal = function(fitleredInvoices) {
        return _.reduce(fitleredInvoices, function(acc, i){
           return acc + i.total;
        }, 0);
    };
    $scope.sumUnsettled = function(fitleredInvoices) {
        return _.reduce(fitleredInvoices, function(acc, i){
            if(i.settled === false){
                return acc + i.total;
            }else {
                return acc;
            }
        }, 0);
    };

});
