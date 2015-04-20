var util = require("util");
var child_process = require("child_process");
var EventEmitter = require("events").EventEmitter;

var config = require("../configuration");

var MemoryWatcher = function(proc) {
    this.timeouts = {};
    this.process = proc;

    proc.on("exit", this.stopWatching.bind(this));

    EventEmitter.call(this);
};

util.inherits(MemoryWatcher, EventEmitter);

MemoryWatcher.prototype.stopWatching = function() {
    var pid = this.process.pid || this.process;

    if (!this.timeouts[pid]) {
        return;
    }

    clearTimeout(this.timeouts[pid]);
};

MemoryWatcher.prototype.startWatching = function() {
    var pid = this.process.pid || this.process;

    // Use ps to get the real memory size
    child_process.exec('ps -p' + pid + ' -o rss=',  function (err, stdout, stderr) {
        err = err || stderr;

        if (err) {
            this.stopWatching();
        } else {
            if (parseInt(stdout, 10) > config.execution.processSize){
                this.kill();
            }
        }
    }.bind(this));

    this.timeouts[pid] = setTimeout(function(){
        this.startWatching();
    }.bind(this), 10);
};

MemoryWatcher.prototype.kill = function(){
    this.stopWatching();

    this.emit("error", "Memory size exceeds " + config.execution.processSize + " KB");
};

module.exports = MemoryWatcher;
