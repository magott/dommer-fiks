var app = angular.module('messages', ['ngSanitize']);
app.controller("ctrl", function($scope, $http, $window) {
     $scope.messages = [];
     $scope.loadMessages = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/messages?json',
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status) {
            $scope.messages = data;
        }).error(function(data, status, headers) {
            if(status === 401){
               $window.location.href= "/login?message=sessionTimeout";
            }
      });
    };
});