var app = angular.module('matchesapp', []);
app.controller("ctrl", function($scope, $http, $window, $location) {
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
            if(status === 401){
                $window.location.href= "/login?message=sessionTimeout";
            }else{
                $window.location.href = "/error";
            }
        });
    };
    $scope.isSet = function(value) {
        return _.isNull(value) != true;
    }
    $scope.dateFilter = function(match) {
        return moment(match.date).isAfter($scope.fromDate);
    }
    $scope.reloadMatches = function() {
        $scope.isLoading = true;
        $scope.loadMatches();
    }
    $scope.isShowingAllMatches = function() {
        return $scope.fromDate.isBefore($scope.today());
    }

    $scope.setFromDate = function(from) {
        $location.search("from", from.toISOString())
        $scope.fromDate = from;
    }
    $scope.today = function() {
        return moment().startOf('day');
    }

    $scope.getFromInitValue = function() {
         from = $location.search().from
         fromUrl = moment(from)
         if(fromUrl.isValid){
            return fromUrl;
        }else{
            return today();
        }
    }
    $scope.yearAgo = function() {
        return $scope.today().subtract(1, 'year');
    }
    $scope.fromDate = $scope.getFromInitValue();
});
