var props;
function Array(array){
   this.props = array;
}

Array.prototype.contains = function(obj) {
    var i = this.props.length;
    while (i--) {
        if (this.props[i] === obj) {
            return true;
        }
    }
    return false;
}

// check if an element exists in array using a comparer function
// comparer : function(currentElement)
Array.prototype.inArray = function(comparer) { 
    for(var i=0; i < this.props.length; i++) { 
        if(comparer(this.props[i])) return true; 
    }
    return false; 
}; 

// adds an element to the array if it does not already exist using a comparer 
// function
Array.prototype.pushIfNotExist = function(element, comparer) { 
    if (!this.inArray(comparer)) {
        this.props.push(element);
    }
}; 

module.exports = Array;