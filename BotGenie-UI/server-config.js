exports.config={
	"logo":"logo_ADP_red.png",
	"header":"Welcome to ADP Live Support",
	"footer":"&copy; ADP Pvt Ltd",
	"presence-monitoring":true,
	"chat-history":{
		"retain":false,
		"period":""
	}
};

exports.initialize = function() {
	console.log("Global Server Configuration is initialized successfully");
};


