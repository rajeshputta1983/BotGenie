var wfnrest = require('./request-wfn');
	
exports.getAnswer = function (question, conversationGuid, callback) {

	console.log('before wfnrest call...'+question);

	wfnrest.getresults(question, conversationGuid, function(err, response){
		if ( err ){
			console.log('wfnrest error - '+err);
			callback(err, question, conversationGuid, null);
		}else{
			console.log('wfnrest response - '+JSON.stringify(response));
			callback(null, question, (response && response.conversationGuid)?response.conversationGuid:null, (response && response.result)?response.result:null);
		}
	});
}

