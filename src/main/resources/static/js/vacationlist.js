var app = angular.module('vacationlist', []);
app.controller("ctrl", function($scope, $http) {
     $scope.vacationList = [];
     $scope.loadVacations = function(year) {
        var httpRequest = $http({
            method: 'GET',
            url: '/vacation?json',
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status) {
            $scope.vacationList = data;
        });
    };
});
