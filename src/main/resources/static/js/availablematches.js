var app = angular.module('availablematchesapp', []);
app.controller("ctrl", function($scope, $http, $window) {
     $scope.isLoading = true;
     $scope.matches = [];
     $scope.isTimeout = false;
     $scope.role=""
     $scope.category=""
     $scope.loadMatches = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/fiks/availablematches?format=json',
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status,headers) {
            $scope.isLoading = false;
            $scope.matches = data;
        }).error(function(data, status, headers) {
            if(status === 401){
                $window.location.href= "/login?message=sessionTimeout";
            }else if(status === 504){
                $scope.isLoading = false;
                $scope.isTimeout = true;
            }else{
                $window.location.href = "/error";
            }
        });
    };

    $scope.uniqueCategories = function(){
        return _.map(_.uniq($scope.matches, 'category'), 'category');
    }

    $scope.filterByType = function(match){
        return _.startsWith(match.role, $scope.role);
    }

    $scope.filterByCategory = function(match){
        return _.startsWith(match.category, $scope.category);
    }
});
