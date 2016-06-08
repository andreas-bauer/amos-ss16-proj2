'use strict';

describe('myApp.dpad module', function() {
    var $compile, $rootScope, $controller;

    beforeEach(angular.mock.module('templates'));
    beforeEach(angular.mock.module('myApp.roverService'));
    beforeEach(module('myApp.dpad'));

    beforeEach(inject(function(_$compile_, _$rootScope_, $httpBackend, _$controller_) {
        // parameters with _$..._ get injected
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $controller = _$controller_;

        $httpBackend.whenGET('assets/icons/ic_label_48px.svg').respond('');
    }));

    describe('dpad directive general layout', function() {
        var element;
        /**
         * General setup
         */
        beforeEach(inject(function() {
            element = $compile('<dpad></dpad>')($rootScope);
            $rootScope.$digest();
        }));

        it('contains four dpad buttons with correct graphics', inject(function() {
            var element = $compile('<dpad></dpad>')($rootScope);

            $rootScope.$digest();

            var icons = element.find('md-icon');
            expect(icons.length).toBe(4);

            for (var i = 0; i < icons.length; i++) {
                var iconElement = icons[i];
                expect(iconElement.getAttribute('md-svg-icon')).toContain('ic_label_48px.svg');
            }
        }));
    });

    describe('dpad directive as movement controller', function() {
        var element;
        /**
         * General setup
         */
        beforeEach(inject(function() {
            element = $compile('<dpad></dpad>')($rootScope);
            $rootScope.$digest();
        }));

        xit('contains the correct header', inject(function() {
            expect(element.html()).toContain("Driving");
        }));

        it('contains a button labeled "Stop"', inject(function() {
            expect(element.find('button').length).toBe(2);
            expect(element.find('button').text()).toContain("Stop");
        }));
    });

    describe('dpad directive as camera controller', function() {
         var element;
        /**
         * General setup
         */
        beforeEach(inject(function() {
            element = $compile('<dpad mode="camera"></dpad>')($rootScope);
            $rootScope.$digest();
        }));

        xit('contains the correct header', inject(function() {
            expect(element.html()).toContain('Camera');
        }));
        it('contains a button labeled "Center"', inject(function() {
            expect(element.find('button').length).toBe(2);
            expect(element.find('button').text()).toContain("Center");
        }));
    });

    describe('dpad component controller ', function() {
        var element, component, scope, roverService;
        /**
         * General setup
         */
        beforeEach(inject(function() {
            element = $compile('<dpad></dpad>');
            scope = $rootScope.$new();
            component = $controller('DPadController', {
                $scope: scope,
                $attrs: {},
                roverService: roverService
            });

        }));

        it('exposes a set of methods according to its buttons', function() {
            expect(scope).toBeDefined();
            expect(scope.buttonClick).toBeDefined();
            expect(scope.up).toBeDefined();
            expect(scope.down).toBeDefined();
            expect(scope.left).toBeDefined();
            expect(scope.right).toBeDefined();
        });
    });
});