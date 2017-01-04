var Hapi = require('hapi');
var ConnectionManager = require('./connection-manager');
var RouteManager = require('./route-manager');
var EventBus = require('./event-bus');
var MVC = require('./mvc');
var ChatManager = require('./chat-manager');
var Utility = require('./utility');
var spellchecker = require('./spell-checker');

var server = new Hapi.Server();

ConnectionManager.initialize(server);

server.register(require('inert'), (err) => {

    if (err) {
        throw err;
    }

	RouteManager.initialize(server);
	
	EventBus.initialize();
	
	ChatManager.initialize();
	
	MVC.initialize();
	
	Utility.initialize();
	
	server.start((err) => {

		if (err) {
			throw err;
		}

		console.log('\r\nChat Server is up for business !!');
	});
});