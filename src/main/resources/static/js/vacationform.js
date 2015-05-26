var app = angular.module('vacationform', []);
app.controller("ctrl", function($scope, $http, $window, $locale, $filter) {
    $scope.vacation = {}
    $scope.wholeDay = false;
    $scope.errors = []
    $scope.submitVacation = function(vacation) {
        console.debug("submitting vacation "+vacation.fromDate);
        var copy = $scope.sanitize(vacation);
        console.debug("Sending: "+JSON.stringify(copy));
        $http.post('/vacation/new', copy)
        .success(function(data, status) {
            $window.location.href= "/vacation";
        })
        .error(function(data, status, headers) {
            if(status === 401){
               $window.location.href= "/login?message=sessionTimeout";
            }else if(status === 400){
               $scope.errors = data;
               console.debug(data);
               console.debug($scope.errors);
               console.debug("hasErrors"+$scope.hasErrors());
            }
        });
    };
    $scope.toggleWholeDay = function(){
        if($scope.wholeDay){
            $scope.vacation.fromTime=moment(0).startOf('day').toDate();
            $scope.vacation.toTime=moment(0).endOf('day').second(0).millisecond(0).toDate();
        }
    }
    $scope.hasErrors = function(){
        return !_.isEmpty($scope.errors);
    }

    $scope.toDate = function (dt) {
       if (!dt) {
         return undefined;
       }
       return moment(dt).format("YYYY-MM-DD");
    };

    $scope.toTime = function (dt) {
       if (!dt) {
         return undefined;
       }
       return moment(dt).format("HH:mm");
    };

    $scope.sanitize = function(vacation) {
        var v = _.clone(vacation, true)
        v.fromDate = $scope.toDate(vacation.fromDate);
        v.toDate = $scope.toDate(vacation.toDate);
        v.fromTime = $scope.toTime(vacation.fromTime);
        v.toTime = $scope.toTime(vacation.toTime);
        return v;
    }
});
