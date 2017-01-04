var EventBus = require('./event-bus');

exports.config={
	"events":['chat request', 'support chat request', 'register support person', 'support channel established', 'notification request', 'email transcript']
};

exports.initialize = function() {
	console.log("Chat Request Manager is initialized successfully");
};

exports.handleRequest = function(io, socket) {
	
	var _this=this;
	socket.on('chat request', function(data){
			_this.delegateRequest('chat request', io, socket, data); 
	});
	
	socket.on('support chat request', function(data){
			_this.delegateRequest('support chat request', io, socket, data); 
	});

	socket.on('register support person', function(data){
			_this.delegateRequest('register support person', io, socket, data); 
	});

	socket.on('support channel established', function(data){
			_this.delegateRequest('support channel established', io, socket, data); 
	});

	socket.on('bye', function(data){
			_this.delegateRequest('bye', io, socket, data); 
	});
	
	socket.on('notification request', function(data){
			_this.delegateRequest('notification request', io, socket, data); 
	});

	socket.on('email transcript', function(data){
			_this.delegateRequest('email transcript', io, socket, data); 
	});
		
};

exports.delegateRequest= function(eventType, io, socket, data){
		//console.log('data :'+JSON.stringify(data));
		EventBus.processEvent(eventType, {"io":io, "socket":socket, "data":data});
};

