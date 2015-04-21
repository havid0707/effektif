var _ = require("lodash");
var child_process = require("child_process");
var util = require("util");
var EventEmitter = require("events").EventEmitter;

var conf = require("../configuration");
var logger = require("./logger");

var MemoryWatcher = require("./MemoryWatcher");
var UpdateManager = require("./UpdateManager");

var Executer = function(script, variables, typeDescriptors) {
    this.script = script;
    this.variables = _.extend({}, variables);
    this.typeDescriptors = typeDescriptors;

    EventEmitter.call(this);
};

util.inherits(Executer, EventEmitter);

Executer.prototype.run = function() {
    if(!this.script) {
        this.emit("complete", {});

        return;
    }

    this._process = this.spawn();
    var pipe = this._process.stdio[3];

    this.listenForUpdates(this._process, pipe);
    this.observeMemory(this._process);
};

Executer.prototype.observeMemory = function(child) {
    var memoryWatcher = new MemoryWatcher(child);

    memoryWatcher.on("error", this.handleError.bind(this));

    memoryWatcher.startWatching();
};

Executer.prototype.handleError = function(error) {
    this.hasCrashed = true;

    this.emit("error", error);

    this._process.kill();
};

Executer.prototype.listenForUpdates = function(child, pipe) {
    var updateManager = new UpdateManager(child, pipe);

    updateManager.on("complete", function(variables, logs, errors) {
        if(this.hasCrashed) {
            return;
        }

        var updates = {};
        _.each(variables, function(value, name) {
            if(JSON.stringify(value) === this.variables[name]) {
                return;
            }

            updates[name] = value;
        }.bind(this));

        if(errors.length > 0) {
            this.handleError(logs + errors);

            return;
        }

        this.emit("complete", {
            logs: logs,
            variableUpdates: updates
        });
    }.bind(this));

    updateManager.on("timeout", this.handleError.bind(this));
};

Executer.prototype.spawn = function() {
    return child_process.spawn("node", [__dirname + "/run.js"], {
        env: _.extend({}, process.env, {
            script: this.script,
            variables: JSON.stringify(this.variables),
            typeDescriptors: JSON.stringify(this.typeDescriptors)
        }),
        cwd: __dirname,
        detached: true,
        // -stdin-, stdout, stderr, custom: Create a message hub between child and parent
        stdio: ["ignore", "pipe", "pipe", "pipe"]
    });
}

module.exports = Executer;

/**
 * Opens a new process to execute
 * the given script.
 * @params data The script and the given variables
 * @params callback A function which will be called when the execution is finished
 * @returns boolean Returns true if script is going to be executed.
 */
// exports.execute = function(data, callback) {

//     try {
//         return true;
//     } catch (error) {

//         // I would love to get the line number for errors here,
//         // but regrettably https://github.com/joyent/node/issues/3452
//         logger.error(error);
//         result.logs += ""+error.name+": "+error.message;
//         result.error = true;
//         callback(result);
//         return false;
//     }
// };

