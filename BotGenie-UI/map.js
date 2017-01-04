function Map(map){
}

Map.prototype.size = function(){ return Object.keys(this).length;}
Map.prototype.asort = function(){
    var retVal = {};
    var self = this;
    var keys = Object.keys(this);
    keys = keys.sort(function(a,b){return self[a] - self[b]});
    for (var i = keys.length -1 ; i >= 0 ; i--) {
        retVal[keys[i]] = this[keys[i]];
    }
    return retVal;
}
Map.prototype.containsKey = function(key){
    var keys = Object.keys(this);
    for (var i = 0; i < keys.length; i++) {
        if ( keys[i].toString().toLowerCase() === key.toLowerCase() )
			return true;
    }
    return false;
}	

module.exports = Map;