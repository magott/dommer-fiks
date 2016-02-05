$("#km").keyup(updateMillage);

$("#kmAllowanceMunicipal").change(updateMillage);

function updateMillage(event) {
    var m = calculateMillageAllowance();
    if(m > 0){
        $("#millageAllowance").val(Number(m).toFixed(2));
    }else{
        $("#millageAllowance").val("");
    }
}

function calculateMillageAllowance(){
    var kmMultiplier = findKmMultiplier();
    var km = parseFloat($("#km").val());
    var millageAllowance = 0.00;
    if(_.isFinite(km)){
        millageAllowance = Number(km * kmMultiplier);
    }
    return millageAllowance;
}

function findKmMultiplier(){
    if($('#kmAllowanceMunicipal').prop('checked')) {
        return 3.90;
    }else{
        return 3.80;
    }
}

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
        if(element.parent('.form-group').length) {
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
    var millageAllowance = calculateMillageAllowance();
    var toll = orZero($("#toll"));
    var otherExpenses = orZero($("#otherExpenses"));
    var perDiem = orZero($("#perDiem"));
    var passenger = Number(orZero($("#passengers")) * orZero($("#passengerKm")));
    $("#total").val(Number(matchFee + millageAllowance + toll + perDiem + otherExpenses +passenger).toFixed(2));
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
        millageAllowance:{
            number:true
        },
        perDiem:{
            digits:true
        },
        passengers:{
            digits:true
        }
    },
    messages:{
        matchFee: {
            required: "Kamphonorar må fylles ut",
            digits: "Kamphonorar må være et heltall"
        },
        km: "Kilometer må være et tall",
        perDiem: "Diett må være heltall",
        toll: "Bompenger må være et tall",
        passengers: "Antall passasjerer må være et heltall",
        passengerKm: "Antall kilometer for passasjerer må være et tall",
        otherExpenses: "Feltet må være tomt eller et tall"
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
