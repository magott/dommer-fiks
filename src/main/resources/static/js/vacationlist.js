var app = angular.module('vacationlist', []);
app.controller("ctrl", function($scope, $http, $window) {
     $scope.vacationList = [];
     $scope.loadVacations = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/vacation?json',
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status) {
            $scope.vacationList = data;
        }).error(function(data, status, headers) {
            if(status === 401){
               $window.location.href= "/login?message=sessionTimeout";
          }
      });
    };
    $scope.deleteVacation = function(vacationId) {
        $http.delete('/vacation?id='+vacationId)
        .success(function(data, status) {
            $scope.loadVacations();
        })
        .error(function(data, status, headers) {
            if(status === 401){
               $window.location.href= "/login?message=sessionTimeout";
            }
        });
    };

    $scope.isHistoric = function(vacation){
        if(!vacation){
            return false;
        }
        return moment(vacation.end).isBefore(moment());
    }

});
