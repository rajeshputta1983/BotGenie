var EventBus = require('./event-bus');

exports.handleRequest = function(io, socket) {
	
	var _this=this;
	
	socket.on('notification request', function(data){
			_this.delegateRequest('notification request', io, socket, data); 
	});
	
};

exports.delegateRequest= function(eventType, io, socket, data){
		console.log('data :'+JSON.stringify(data));
		EventBus.processEvent(eventType, {"io":io, "socket":socket, "data":data});
};

