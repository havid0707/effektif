var vm = require('vm');
var net = require('net');
var _ = require("lodash");
var request = require("request");

var EffektifObject = require("./EffektifObject");

// Create a pipe for variable results
var pipe = new net.Socket({ fd: 3 });
pipe.unref && pipe.unref();

var organization = process.env.organization;
var host = process.env.host;
var variables = JSON.parse(process.env.variables);
var typeDescriptors = JSON.parse(process.env.typeDescriptors);




var variableObjects = _.map(variables, function(variable) {
    var typeDescriptor = typeDescriptors[variable.type.name];
    if(typeDescriptor.primitive) {
        return variable.value;
    } else {
        return new EffektifObject(typeDescriptor, variable.value);
    }
});



/**
 * Context for the script execusion
 *
 */
var Context = function(){

    // Define context variables
    this.console = console;
    this.request = request;
    this._ = _;

    if (!variables){
        return;
    }



    // Define GETTER/SETTER for all variables
    for (var key in variables){

        (function(key, value, ctx){

            // GETTER
            ctx.__defineGetter__(key, function(){
                return value;
            });

            // SETTER
            ctx.__defineSetter__(key, function(arg){
                // Set local variable
                value = arg;

                pipe.write(JSON.stringify({
                    name: key,
                    value: arg
                }));
            });

        }(key, variables[key], this));
    }
};

var ctx = new Context();

// Running script in IIFE in order to allow return
// statements in the scripts.
vm.runInNewContext("(function() {\n" + process.env.script + "}())", ctx);
