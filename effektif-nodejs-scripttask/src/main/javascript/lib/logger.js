
var winston = require('winston');
var conf = require('../configuration.js');

// some default values
var DEFAULT_MAX_FILE_SIZE = conf.log.maxFileSize;
var DEFAULT_MAX_FILES = conf.log.maxFiles;


var options = {
    // this means if any uncaught exception occurs the system will keep on running and not stop
    exitOnError : false
}

options.transports = [
    new (winston.transports.Console)({
        level           : conf.log.level,
        timestamp       : true,
        handleExceptions: true
    })
];

if (conf.log.toFile) {
    // just log to a file if a log file is defined
    options.transports.push(new (winston.transports.File)({
        level           : conf.log.level,
        filename        : conf.log.file,
        json            : false,
        timestamp       : true,
        maxsize         : DEFAULT_MAX_FILE_SIZE,
        maxFiles        : DEFAULT_MAX_FILES,
        handleExceptions: true
    }));
}

if (conf.log.toFile && conf.log.errorFile) {
    // just log to the error file if it is defined
    options.exceptionHandlers = [
        (new (winston.transports.File)({
            filename        : conf.log.errorFile,
            json            : false,
            timestamp       : true,
            maxsize         : DEFAULT_MAX_FILE_SIZE,
            maxFiles        : DEFAULT_MAX_FILES,
            handleExceptions: true
        }))
    ];
}

var logger = new (winston.Logger)(options);

var fnError = logger.error;

var errorToJson = function(e) {
    return {
        name    : e.name,
        message : e.message,
        stack   : e.stack
    }
}

/**
 * This overwritten error function takes an Error as first or second parameter
 * and creates a JSON object with respective values.
 */
logger.error = function() {
    if (arguments.length == 1) {
        if (arguments[0] instanceof Error) {
            fnError.call(this, errorToJson(arguments[0]));
        } else {
            fnError.apply(this, arguments);
        }
    } else if (arguments.length == 2) {
        if (arguments[1] instanceof Error) {
            fnError.call(this, arguments[0], errorToJson(arguments[1]));
        } else {
            fnError.apply(this, arguments);
        }
    } else {
        fnError.apply(this, arguments);
    }
}

module.exports = logger;
