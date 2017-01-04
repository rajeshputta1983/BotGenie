var fs = require('fs');
var Utility=require("./utility");

var bufferBlockSize=2048;

exports.serveContent=function(filePath, encoding, callback, response, config) {
	var stream = fs.createReadStream(filePath);
	
	var fileContent='';
	
	stream.on('data', function (chunk) {
		fileContent+=chunk;
	})
	.on('end', function () {
		console.log('static resource loaded modified ::'+filePath);
		callback(response, config, fileContent);
	});

};


exports.readFileContent=function(filePath, encoding, callback) {

	 console.log("reading file content :"+filePath);

	 fs.open(filePath, "r", function(error, fd) {
		
		if(error)
		{
			console.log(error);
			throw error;
		}
		
		var fileContent=''; 

		 var buffer = new Buffer(bufferBlockSize);
		 fs.read(fd, buffer, 0, buffer.length, null, function(error, bytesRead, buffer) {

			if(error)
			{
				console.log(error);
				throw error;
			}

			var data = buffer.toString(encoding, 0, buffer.length);
			
			 fileContent+=data; 
			 
			 fs.close(fd, function(err){
				 if (err){
					 console.log(err);
					 throw err;
				 } 
				 console.log("File closed successfully.");
			});
		});
		
		 console.log("file content ::"+fileContent);
		 
		 return fileContent;
		
	 });
	 
};
