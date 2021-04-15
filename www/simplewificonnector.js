var exec = require('cordova/exec');

exports.connect = function (arg0, arg1, success, error) {
    console.log("called connect with params:",arg0+","+arg1)
    exec(success, error, 'simplewificonnector', 'connect', [arg0,arg1]);
};