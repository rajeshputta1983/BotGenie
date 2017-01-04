
var TemplateEngine=function(){

	this.templatePrefix  = "<<";
	this.templatePostfix = ">>";
	this.keywordRegex = "(if|else|else\s+if|for|break|continue|{|})\s*(.*)?";

	this.processTemplate = function(template, context){
		
		var templateRegex = this.templatePrefix;
		templateRegex+="[^";
		templateRegex+=this.templatePostfix;
		templateRegex+="]+";
		templateRegex+=this.templatePostfix;
		
		var regex=new RegExp(templateRegex, "ig");
		var processedTemplate='var content=[];\r\n';
		var offset=0;
		while(match=regex.exec(template)) {
			//console.log(match.index);
			//console.log(match[0]);
			
			processedTemplate+="content.push('"+template.slice(offset, match.index).replace(/'/g, "\\'")+"');\r\n";
			
			var placeholder=match[0].substring(this.templatePrefix.length, match[0].length-this.templatePostfix.length);
			
			if(placeholder.match(this.keywordRegex))
				processedTemplate+=placeholder+"\r\n";
			else
				processedTemplate+="content.push("+placeholder+");\r\n";
			
			offset=match.index + match[0].length;
		}
		
		processedTemplate+="content.push('"+template.substring(offset).replace(/'/g, "\\'")+"');\r\n";
		processedTemplate+="return content.join('');";
		
		processedTemplate=processedTemplate.replace(/\r\n/g, "");
		
		try
		{
			var func= new Function(processedTemplate);
			return func(context);
		}
		catch(err){
			throw err;
		}
	}
};

/*var te=new TemplateEngine();

var result=te.processTemplate("hello <<this.ph1>> how are <<for(var i=0;i<=10;i++){ >> you <<this.ph2>> <<}>> <<this.ph3>>", {"ph1":"man", "ph2":"good", "ph3":"great"});
console.log("------------------");
console.log(result);
*/

module.exports=TemplateEngine;
