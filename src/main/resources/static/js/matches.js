var app = angular.module('matchesapp', []);
app.controller("ctrl", function($scope, $http, $location) {
     $scope.fromDate = moment().startOf('day');
     $scope.isLoading = true;
     $scope.matches = [];
     $scope.loadMatches = function() {
        var httpRequest = $http({
            method: 'GET',
            url: '/fiks/mymatches?format=json',
            headers: {
               'Content-Type': 'application/json'
            }
        }).success(function(data, status,headers) {
            $scope.isLoading = false;
            $scope.matches = data;
        }).error(function(data, status, headers) {
            if(status === 302){
                $location.path(headers().location).replace();
            }
        });
    };
    $scope.isSet = function(value) {
        return _.isNull(value) != true;
    }
    $scope.count = function(col) {
        return col.length;
    }
    $scope.dateFilter = function(match) {
        return moment(match.date).isAfter($scope.fromDate);
    }

    $scope.setFromDate = function(date) {
        $scope.fromDate = date;
    }

    $scope.isShowingAllMatches = function() {
        return !($scope.fromDate.isSame($scope.today()));
    }

    $scope.setFromDate = function(from) {
        $scope.fromDate = from;
    }
    $scope.today = function() {
        return moment().startOf('day');
    }

    $scope.yearAgo = function() {
        return $scope.today().subtract(1, 'year');
    }
});
