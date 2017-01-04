var fs = require('fs');
var MVC=require("./mvc");
var FileUtility=require("./file-utility");

exports.config={
	"cache":true,
	"order":["header","body","footer"],

	"default":{
		"header":"header.html",
		"body":"body.html",
		"footer":"footer.html"
	},

	"chat":{
		"body":"chat.html",
	},
	
	"supportchat":{
		"footer":"support-footer.html",
		"extends":"chat"
	}

};


exports.initialize=function(){

	var keys=Object.keys(this.config);
	
	for(var keyIndex in keys)
	{
		var tileKey=keys[keyIndex];
		
		var tileDefinition=this.config[tileKey];
		
		if(tileKey!="default")
		{
			this.initializeDefinition(tileDefinition, false);
		}
	}
	
	console.log("Tiles Manager is initialized successfully...");
};


exports.initializeDefinition=function(tileDefinition, isDefaultTile)
{
		if(isDefaultTile)
		{
			return;
		}

		var parentTile=tileDefinition.extends?tileDefinition.extends:"default";
		
		//console.log("tile being processed..."+parentTile);

		if(parentTile && !this.config[parentTile])
		{
			throw new Error("Illegal configuration parent tile is not configured..."+parentTile);
		}
		
		if(!tileDefinition.tileProcessed)
		{
			this.initializeDefinition(this.config[parentTile], parentTile=="default"?true:false);
		}
		
		var parentTileDef=this.config[parentTile];
		
		var tileKeys=Object.keys(parentTileDef);
		
		for(var tileKeyIndex in tileKeys)
		{
			var tileKeyName=tileKeys[tileKeyIndex];
			if(tileDefinition[tileKeyName])
				continue;
			
			tileDefinition[tileKeyName]=parentTileDef[tileKeyName];
		}
		
		tileDefinition.tileProcessed=true;

		if(parentTile=="default")
		{
			return;
		}
}


exports.getTileDefinition=function(tileName){

	if(!tileName)
	{
		throw new Error("tile name cannot be null...");
	}

	return this.config[tileName];

};

exports.handleAsyncResponse=function(fileContent, response, context, tile, templatesCallback, modelContext){
	
	delete context.cloneDef[tile];
	
	context[tile]=fileContent;

	if(Object.keys(context.cloneDef).length==0)
	{
		var orderArray=context.orderArray;
		
		var finalContent='';
		
		for(var index in orderArray)
		{
			var orderKey=orderArray[index];
			var tileContent=context[orderKey];
			
			if(!tileContent)
			{
				continue;
			}
		
			finalContent+=tileContent;
			
			delete context[orderKey];
		}

		//console.log('final response...'+finalContent);
		
		if(!modelContext)
			response(finalContent).type("text/html");
		else
			templatesCallback(response, finalContent, modelContext);

	}
};


exports.serveTileContent=function(tileName, response, templatesCallback, modelContext){
		
	var tileDefinition=this.getTileDefinition(tileName);

	if(!this.config.order)
	{
		throw new Error("order of tiles to be configured under...config['order']");
	}
	
	delete tileDefinition.extends;
	delete tileDefinition.tileProcessed;
	
	var cloneDef=JSON.parse(JSON.stringify(tileDefinition));

	var context={"cloneDef":cloneDef, "orderArray":this.config.order};
	
	for(var key in tileDefinition)
	{
		var tile=tileDefinition[key];
		
		if(!tile)
		{
			continue;
		}
		
		this.readContent(MVC.config.staticDirectory+tile, null, this.handleAsyncResponse, response, context, key, templatesCallback, modelContext);
	}

};

exports.readContent=function(filePath, encoding, callback, response, context, tile, templatesCallback, modelContext) {
	var stream = fs.createReadStream(filePath);
	
	var fileContent='';
	
	stream.on('data', function (chunk) {
		fileContent+=chunk;
	})
	.on('end', function () {
		callback(fileContent, response, context, tile, templatesCallback, modelContext);
	});

};
