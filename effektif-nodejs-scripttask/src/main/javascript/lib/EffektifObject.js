var _ = require("lodash");
var events = require("events");
var request = require("sync-request");

var EffektifList = require("./EffektifList");

var config = require("../configuration");

var URLS = {
    user: function(organization, id) {
        return [config.effektifHost, "api", config.apiVersion, organization, "users", id].join("/");
    },
    file: function(organization, id) {
        return [config.effektifHost, "api", config.apiVersion, organization, "files", id].join("/");
    },
    mail: function(organization, id) {
        return [config.effektifHost, "api", config.apiVersion, organization, "mails", id].join("/");
    }
}

var REFERENCE_OBJECT_CACHE = {};

function EffektifObject(organization, typeDescriptors, type, value, parent) {
    this.__type = type;
    this.__typeDescriptor = _.findWhere(typeDescriptors, { id: type.name });
    this.__parent = parent;
    this.__data = null;
    this.__ref = value;
    this.__organization = organization;

    var typeDescriptor = this.__typeDescriptor;

    if(typeDescriptor.isMutable) {
        // mutable type values are stored inline
        this.__data = value;
    } else {
        // reference type objects will be fetched when accessed
        // if the referenced object has already been fetched before, return
        // the unique instance from the cache
        this.__uri = URLS[this.__type.name](this.__organization, this.__ref);
        if(REFERENCE_OBJECT_CACHE[this.__uri]) {
            // returning a non-primitive value from a constructor function invoked with the `new`keyword  
            // will make sure that this value is the result of the class instantiation
            return REFERENCE_OBJECT_CACHE[this.__uri];
        } else {
            REFERENCE_OBJECT_CACHE[this.__uri] = this;
        }
    }

    _.each(typeDescriptor.fields, function(field) {
        var key = field.key;
        var field = _.findWhere(typeDescriptor.fields, { key: key });
        var fieldTypeDescriptor;
        if(field.type.name === "list") {
            var elementTypeDescriptor = _.findWhere(typeDescriptors, { id: field.type.elementType.name });
            fieldTypeDescriptor = {
                isList: true,
                isPrimitive: elementTypeDescriptor.isPrimitive
            };
        } else {
            fieldTypeDescriptor = _.findWhere(typeDescriptors, { id: field.type.name });
        }

        this.__defineGetter__(key, function() {
            if(!typeDescriptor.isMutable) {
                // reference types are immutable,
                // make sure to fetch the data of the referenced object
                this.__ensureLoaded();
            }

            if(fieldTypeDescriptor.isPrimitive) {
                return this.__data[key];
            } else if(fieldTypeDescriptor.isList) {
                return this.__data[key] ?
                    new EffektifList(organization, typeDescriptors, field.type, this.__data[key], this) :
                    null;
            } else {
                return this.__data[key] ?
                    new EffektifObject(organization, typeDescriptors, field.type, this.__data[key], this) :
                    null;
            }
        });

        this.__defineSetter__(key, function(newValue) {
            if(this.__isMutable()) {
                this.__data[key] = newValue;
                // emit `change` event to notify its parent/environment
                this.__emit("change");
            } else {
                throw new Error("Cannot set property value on an immutable object");
            }
        });

    }, this);

    // propagate change notifications of mutable objects up to the top level parent
    // so the environment can handle them
    if(parent) {
        this.__on("change", function() {
            parent.__emit("change");
        }.bind(this));
    }
}
EffektifObject.__flushReferenceObjectCache = function() {
    var REFERENCE_OBJECT_CACHE = {};
}
EffektifObject.prototype.__emit = (new events.EventEmitter).emit;
EffektifObject.prototype.__on = (new events.EventEmitter).on;
EffektifObject.prototype.__ensureLoaded = function() {
    if(!this.__data) {
        // synchronous fetch of the resolved variable value
        var response = request("GET",  this.__uri);
        this.__data = JSON.parse(response.getBody());
    }
};
EffektifObject.prototype.__isMutable = function() {
    // returns true if this and all its (grand)parents are mutable
    return this.__typeDescriptor.isMutable && (!this.__parent || this.__parent.__isMutable());
};
EffektifObject.prototype.toString = function() {
    var result = {};
    _.each(this.__typeDescriptor.fields, function(field) {
        var value = this[field.key];
        result[field.key] = value instanceof EffektifObject ?
            (value.__isMutable() ? value.toString() : value.__ref) :
            value;
    }, this);
    return JSON.stringify(result);
};

module.exports = EffektifObject;