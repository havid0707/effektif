module.exports = {
    port: 8081,
    host: "127.0.0.1",

    effektifHost: "http://localhost:8900",
    apiVersion: "v1",

    execution: {
        timeout: 10,

        // size of logging inside script
        logSize: 20 * 1000,
        processSize: 40 * 1000
    },

    log: {
        // 'info', 'warn', 'error'
        level: "debug",
        toFile: false,

        // if toFile === true then this is the dir of the output fiel
        file: "",
        errorFile: "",
        maxFileSize: 100 * 1024 * 1024,
        maxFiles: 10
    }
}
