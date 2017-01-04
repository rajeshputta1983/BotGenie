var request = require('request');

exports.getresults = function (question, conversationGuid, callback) {

	var headersObj={'chatmsg' : question};
	
	if(conversationGuid!=null) {
		headersObj['conversationGuid']=conversationGuid;
	}

	request.post({
	  headers: headersObj,
	  url:     'http://localhost:10000/chatbot/request'
	}, function(err, response, body){
		if (err) {
		  callback(err, null);
		}
		
		if(response!=null && body!=null) {
			console.log('getResults response - '+JSON.stringify(response));
			
			var responseObj = JSON.parse(body);
			console.log(responseObj.response.status);
			
			if ( responseObj.response.status == "Success" ){
				console.log(responseObj.response.result);
				callback(null, responseObj.response);
			}else{
				console.log(responseObj.response.statusMessage);
				callback(err, responseObj.response.statusMessage);
			}
		}
		
		callback(null, '');
		
	});
}