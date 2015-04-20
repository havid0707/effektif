var _ = require("lodash");
var util = require("util");
var EventEmitter = require("events").EventEmitter;

var config = require("../configuration");
var logger = require("./logger");

var UpdateMangaer = function(child, pipe) {
    this.variableData = "";
    this.logs = "";
    this.errors = "";

    pipe.setEncoding("utf8");
    pipe.on("data", this.handleData.bind(this));

    child.stdout.setEncoding("utf8");
    child.stdout.on("data", this.handleConsole.bind(this));

    child.stderr.setEncoding("utf8");
    child.stderr.on("data", this.handleError.bind(this));

    child.on("exit", this.handleExit.bind(this));

    this._timeout = setTimeout(function() {
        this.timedOut = true;

        this.emit("timeout", "Timeout exceeded after "+ config.execution.timeout +" seconds\n");
    }.bind(this), config.execution.timeout * 1000);

    EventEmitter.call(this);
};

util.inherits(UpdateMangaer, EventEmitter);

UpdateMangaer.prototype.handleData = function(data) {
    logger.debug("data received from pipe:\n" + data);

    this.variableData += data.toString("utf8");
};

UpdateMangaer.prototype.handleConsole = function(data) {
    if(this.logs.length > config.execution.logSize) {
        return;
    }

    this.logs += data.toString("utf8");

    if(this.logs.length > config.execution.logSize) {
        this.errors += "Console limit of "+ config.execution.logSize/1000 +" KB exceeded\n";
    }
};

UpdateMangaer.prototype.handleError = function(data) {
    data = data.toString("utf8");

    // Check if error is given
    if (data.split("\n").length <= 3){
        return;
    }

    var lines = data.split("\n");

    // Get error
    var err = lines[0];
    var line = (lines[1].match(/:[0-9:]+/)||[""])[0];

    // Check for maximum lines
    if (line && parseInt(line.slice(1)) > lines.length){
        line = ":"+(lines.length)+":0"
    }

    if(line) {
        var lineAndColumn = line.slice(1).split(":");

        var lineNumber = parseInt(lineAndColumn[0], 10) - 1;
        var column = parseInt(lineAndColumn[1], 10);
    }

    // Append error code
    this.errors += err + " " + (line ? "- In line " + lineNumber + ":" + column : "")+"\n";
};

UpdateMangaer.prototype.handleExit = function() {
    if(this.timedOut) {
        return;
    }

    var data = _.compact(_.map(this.variableData.split("}{"), function(chunk) {
        if(chunk.indexOf("{") !== 0) {
            chunk = "{" + chunk;
        }

        if(chunk.lastIndexOf("}") !== chunk.length - 1) {
            chunk = chunk + "}";
        }

        var json;

        try {
            json = JSON.parse(chunk);
        } catch(e) {
            // ignore this chunk
        }

        return json
    }));

    var currentValues = {};

    _.each(data, function(entry) {
        currentValues[entry.name] = entry.value;
    });

    clearTimeout(this._timeout);

    this.emit("complete", currentValues, this.logs, this.errors);
};

module.exports = UpdateMangaer;
