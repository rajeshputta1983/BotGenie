var Utility = require('./utility');
var ChannelManager = require('./channel-manager');


var events = require('events');
var eventEmitter = new events.EventEmitter();

exports.config={
	"eventHandlers":{
		"support chat request":[ChannelManager.processChatRequest],
		"chat request":[ChannelManager.checkIfChannelCanBeEstablished],
		"register support person":[ChannelManager.registerSupportPerson],
		"support channel established":[ChannelManager.supportChannelEstablished],
		"bye":[ChannelManager.supportPersonExit],
		"notification request":[ChannelManager.notify],
		"email transcript":[ChannelManager.sendTranscript]
	}
};

exports.initialize=function() {
	
	for(var eventName in this.config.eventHandlers)
	{
		var handlers=this.config.eventHandlers[eventName];
		
		if(handlers)
		{
			for(var handlerIndex in handlers)
			{
				var handler=handlers[handlerIndex];
				
				//console.log(handler);
				
				eventEmitter.on(eventName, handler);

				//console.log("registered event handlers for event '"+eventName+"'");	
			}	
		}
	}
	
	console.log("Event Bus is initialized successfully...");
};

exports.registerHandler=function(eventName, eventHandler) {
	if(!eventName || !eventHandler)
	{
		return;
	}
	
	var handlers=this.config.eventHandlers.eventName;
	
	eventEmitter.addListener(eventName, eventHandler);
	
	if(handlers)
	{
		handlers.push(eventHandler);
		
		return;
	}
	
	this.config.eventHandlers.eventName=[];
	
	this.config.eventHandlers.eventName.push(eventHandler);
};

exports.removeEventHandler=function(eventName, eventHandler) {
	if(!eventName || !eventHandler)
	{
		return;
	}
	
	delete this.config.eventHandlers.eventName[eventHandler];
	
	eventEmitter.removeListener(eventName, eventHandler);
};

exports.removeEventHandlers=function(eventName) {
	if(!eventName)
	{
		return;
	}
	
	delete this.config.eventHandlers.eventName;
	
	eventEmitter.removeAllListeners(eventName);
};

exports.processEvent=function(eventName, context) {
	if(!eventName)
	{
		return;
	}
	
	eventEmitter.emit(eventName, context);	
};

exports.getEventHandlers=function(eventName) {
	return eventEmitter.listeners(eventName);	
};

exports.getEventEmitter=function() {
	return eventEmitter;
}
