var app = angular.module('reportinterest', []);
app.controller("ctrl", function($scope, $http, $window) {
     $scope.loaded = false;
     $scope.matchInfo = null;
     $scope.loadMatchInfo = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/fiks/availabilityinfo?matchid='+ _.trim($(".kampnummer").text()) +"&tournament="+ _.trim($(".turnering").text()),
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status,headers) {
            $scope.loaded = true;
            $scope.matchInfo = data;
        }).error(function(data, status, headers) {
            if(status === 401){
                $window.location.href= "/login?message=sessionTimeout";
            }else if(status === 504){
            }
        });
    };
});
