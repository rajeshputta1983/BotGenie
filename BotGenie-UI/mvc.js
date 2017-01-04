var FileUtility=require("./file-utility");
var TilesManager=require("./tiles-manager");
var TemplateEngine=require("./template-engine");
var ServerConfig=require("./server-config");


exports.config={
	 "staticDirectory":"./public/html/",

	 "/chat":{
		 "model":null,
		 "file":null,
		 "tile":"chat",
		 "processTemplate":true,
		 "ContentType":"text/html",
		 "ContentDisposition":null
	 },
	 
	 "/support":{
		 "model":null,
		 "file":"support.html",
		 "tile":null,
		 "processTemplate":true,
		 "ContentType":"text/html",
		 "ContentDisposition":null
	 },
	 
	 "/supportChat":{
		 "model":null,
		 "file":"support-popup.html",
		 "tile":null,
		 "processTemplate":true,
		 "ContentType":"text/html",
		 "ContentDisposition":null
	 }
	 
	 
};

exports.initialize = function() {
	console.log("MVC is initialized successfully");
};

exports.render=function(response, config, content){
	
    return response(content).type(config?config.ContentType:"text/html");	
};

exports.applyTemplates=function(response, templateContent, modelContext) {
	var processedContent=new TemplateEngine().processTemplate(templateContent, modelContext);
	//console.log("processed template content :::"+processedContent);
	response(processedContent).type("text/html");
};

exports.handleRequest=function(request, response){
	 console.log("handling request for path modified :"+request.path);
	 
	 if(!this.config[request.path])
	 {
		 console.log("path is not registered with MVC "+request.path);
		 FileUtility.serveContent((this.config.staticDirectory + "404.html"), null, this.render, response, this.config[request.path]);
		 return;
	 }
	 
	 var modelContext=JSON.parse(JSON.stringify(ServerConfig.config));
	 
	 if(this.config.model)
	 {
		if(typeof this.config.model !== 'function')
		{	
			throw new Error("Illegal configuration...not a function :"+this.config.model);
		}
		
		this.config.model(modelContext);
	 }
	 
	 if(this.config[request.path].file)
	 {
		console.log("processing as static resource...");
		FileUtility.serveContent((this.config.staticDirectory + this.config[request.path].file), null, this.render, response, this.config[request.path]);
	 }
	 else if(this.config[request.path].tile)
	 {
		console.log("processing as tile definition..."+this.config[request.path].tile);
		
		TilesManager.serveTileContent(this.config[request.path].tile, response, this.applyTemplates, this.config[request.path].processTemplate?modelContext:null);
	 }
	 
};