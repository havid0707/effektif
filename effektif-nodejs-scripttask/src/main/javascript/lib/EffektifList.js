var _ = require("lodash");

function EffektifList(organization, typeDescriptors, type, value, parent) {
    this.__type = type;

    // lazily require because of circular dependency
    var EffektifObject = require("./EffektifObject");
    
    return _.map(value, function(item) {
        return new EffektifObject(organization, typeDescriptors, type.elementType, item, parent);
    });
}

module.exports = EffektifList;