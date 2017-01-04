
var nodemailer = require('nodemailer');
var transporter = null;
	
exports.initialize = function() {
	transporter = nodemailer.createTransport({
		host: "mailrelay.nj.adp.com",
		port : "25",
	});	
	
	console.log("Mail Manager is initialized successfully");
};

exports.sendMail = function(from, to, subject, body) {
	if(!transporter)
	{
		console.error("e-mail transporter is not initialized properly !!");
		return;
	}

	transporter.sendMail({  
		to : to,
		from : from,
		subject : subject,
		html : body,
		function(err, result){
			if(err){ 
				console.error(err); 
				return;
			}
		}
	});
	
	console.log("Mail sent successfully to..."+to);
};
