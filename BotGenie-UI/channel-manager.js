
var uuid = require('node-uuid');
var utility = require('./utility');

var channels={
};

exports.registerSupportPerson = function(eventContext) {
	
	var socket=eventContext.socket;
	var data=eventContext.data;

	if(!socket || !data)
		return;
	
	if(channels[data.emailId])
	{
		socket.emit("register failure",{"msg":"somebody already registered with this email id..."});
		
		return;
	}
	
	channels[data.email]={"socket":socket, "data":data};
	
	socket.emit("register success");
};

exports.establishChannel = function(eventContext) {
	
	var socket=eventContext.socket;

	if(!socket)
		return;

	var uniqueRoom=uuid.v4();
	
	socket.join(uniqueRoom);
	
	if(!channels)
	{
		socket.emit("not available",{"msg":"support team is not available at this moment..."});
		return;
	}
	
	var keys = Object.keys(channels);
	var random_person_key = keys[Math.floor(Math.random() * keys.length)];	
	
	var random_person=channels[random_person_key];
	
	if(!random_person)
	{
		socket.emit("not available",{"msg":"no support person is not available at this moment..."});
		return;
	}
	
	console.log("support person ::"+random_person +"\joining room ::"+uniqueRoom);
	
	random_person.socket.join(uniqueRoom);
	
	socket.broadcast.to(uniqueRoom).emit("support person joined",{"msg":random_person.data.name+" has joined to support you..."});
	
	random_person.socket.emit("user joined",{"msg": "user has joined for support..."});
	
	return;
};

var spellcheck = require('./spell-checker');
var chatbotclient = require('./chatbotclient');

exports.checkIfChannelCanBeEstablished = function(eventContext) {
	
	var socket=eventContext.socket;
	var data=eventContext.data;
	var io=eventContext.io;	

	if(!socket || !data)
		return;
		
	if(data.room)
	{
		var question = data.msg;
		var conversationGuid = data.conversationGuid;
		console.log(data.room+' ------------------ question :'+question);
		console.log(conversationGuid);
		
		chatbotclient.getAnswer(question, conversationGuid, function(err, uquestion, convGuid, result){
			console.log(' ');
			console.log('channel-manager '+uquestion +' ,response - '+result);
			if ( result ){
				var temp={"msg":result, "conversationGuid": convGuid,"topic":"question", "room":data.room};
				console.log(JSON.stringify(temp));
				socket.emit("chat reply", {"msg":result, "conversationGuid": convGuid,"topic":"question", "room":data.room} );
			}
		});
		
		return;
	}
		
	var uniqueRoom=uuid.v4();
	
	socket.join(uniqueRoom);
	
	return;
};

exports.supportChannelEstablished = function(eventContext) {
	var socket=eventContext.socket;
	var data=eventContext.data;
	var io=eventContext.io;

	if(!socket || !data)
		return;
	
	socket.join(data.room);
	
	var person=channels[data.email];
	
	socket.broadcast.to(data.room).emit("support person joined",{"msg":person.data.name+" has joined to support you...", "room":data.room});
	
	if(!person.rooms)
	{
		person.rooms=[];
	}
	
	person.rooms.push(data.room);
	
	return;
};

exports.processChatRequest = function(eventContext) {
	var socket=eventContext.socket;
	var data=eventContext.data;
	var io=eventContext.io;

	if(!socket || !data)
		return;
	
	socket.broadcast.to(data.room).emit("chat reply",data);
	
	return;
};

exports.supportPersonExit = function(eventContext) {
	var socket=eventContext.socket;
	var data=eventContext.data;

	var person=channels[data.email];
	
	if(!person)
	{
		return;
	}
	
	for(var roomIndex in person.rooms){
		var room=person.rooms[roomIndex];
		socket.broadcast.to(room).emit("support person exit",{"msg":person.data.name+" has left the room..."});
	}
	
	delete channels[data.email];
};

exports.notify = function(eventContext) {
	var socket=eventContext.socket;
	var data=eventContext.data;
	var io=eventContext.io;

	console.log("notify room "+JSON.stringify(data));
	
	socket.broadcast.to(data.room).emit("notify",data);
};

exports.sendTranscript = function(eventContext) {
	var socket=eventContext.socket;
	var data=eventContext.data;
	var io=eventContext.io;

	utility.sendMail("Rajesh.Putta@adp.com", data.email, "chat transcript", data.msg);
};