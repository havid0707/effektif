// JavaScript execution server - creates an HTTP server that responds to JavaScript code sent over HTTP by
// executing the code and returning a response that contains the execution results.

var http = require('http');

var conf = require('./configuration.js');
var Executer = require('./lib/Executer.js');
var logger = require('./lib/logger');
var prettyjson = require("prettyjson");

// Start server
http.createServer(function (request, response) {

    var data = "";

    // Retrive data
    request.on("data", function(chunk) {
        data += chunk;
    });

    request.on("end", function() {
        try {
            console.log("data = " + data);
            data = JSON.parse(data);

            logger.debug("\n" + prettyjson.render(data));

            var executer = new Executer(data.script, data.variables, data.typeDescriptors);

            response.setHeader("Content-Type", "application/json; charset=utf-8");

            executer.on("complete", function(result) {
                response.end(JSON.stringify(result));

                logger.debug("Response sent for request\n" + prettyjson.render(result));
            });

            executer.on("error", function(error) {
                console.log(error);
                response.end(JSON.stringify({
                    error: true,
                    logs: error,
                    variableUpdates: {}
                }));
            });

            executer.run();
        } catch(e){
            logger.error(e);

            response.end(JSON.stringify({}));
        }
    });

}).listen(conf.port, conf.host);

logger.info('NodeJS script server started on ' + (conf.host?conf.host+':':'')+ (conf.port || '8081'));
