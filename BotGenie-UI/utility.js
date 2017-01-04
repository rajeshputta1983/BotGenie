var ServerConfig = require('./server-config');
var MailManager = require('./mail-manager');

exports.initialize = function() {
	MailManager.initialize();
	
	console.log("Utility module is initialized successfully");
};

exports.getServer = function(server, serverLabel) {
	return server.select(serverLabel);
};

exports.broadcast = function(socket, msg) {
	socket.broadcast.emit(msg);	
};

exports.getServerConfig=function(key)
{
	if(!key)
		return ServerConfig.config;
		
	return ServerConfig.config[key];
};

exports.sendMail=function(from, to, subject, body)
{
	MailManager.sendMail(from, to, subject, body);	
}





