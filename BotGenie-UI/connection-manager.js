var ChatManager = require('./chat-manager');
var NotificationManager = require('./notification-manager');
var Utility = require('./utility');

exports.config = {
	connections:[
		{ 
			port: 9001, 
			labels: ['notification'] 
		},
		
		{
			port: 9000, 
			labels: ['chat'] 
		}
	]
};

exports.initialize = function(server)
{
	var connections=this.config.connections;
	
	for(var connection in connections)
	{
		server.connection(connections[connection]);	
	}
	
	var io = require('socket.io')(Utility.getServer(server, 'chat').listener);

	io.on('connection', function (socket) {
		
		console.log("chat connection established...");
		socket.emit("greeting",{"msg":", I am Alice. To get started please type your question."});
		ChatManager.handleRequest(io, socket);
		
	});
	
	console.log("Connection Manager is initialized successfully...");
};