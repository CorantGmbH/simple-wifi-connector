var exec = require('cordova/exec');

exports.connect = function (arg0, arg1) {
    console.log("called connect with params:",arg0+","+arg1)
    return new Promise(function (resolve, reject) {
        exec(resolve, reject, 'simplewificonnector', 'connect', [arg0,arg1]);
    });
};
