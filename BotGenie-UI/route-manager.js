var MVC=require("./mvc");
var TileManager=require("./tiles-manager");

exports.config = {
	routes:[
		{
			method: 'GET',
			path: '/html/{file*1}',
			handler:  {
				directory: { 
				  path: 'public/html'
				}
			}	
		},
		
		{
			method: 'GET',
			path: '/js/{file*1}',
			handler:  {
				directory: { 
				  path: 'public/js'
				}
			}	
		},
		
		{
			method: 'GET',
			path: '/img/{file*1}',
			handler:  {
				directory: { 
				  path: 'public/img'
				}
			}	
		},

		{
			method: 'GET',
			path: '/css/{file*1}',
			handler:  {
				directory: { 
				  path: 'public/css'
				}
			}	
		},
		
		{
			method: '*',
			path: '/{param*}',
			handler: function (request, reply) {
				MVC.handleRequest(request, reply);		
			}
		}		
	]
};

exports.initialize = function(server)
{
	var routes=this.config.routes;
	
	for(var route in routes)
	{
		server.route(routes[route]);	
	}
	
	console.log("Route Manager is initialized successfully...");
	
	TileManager.initialize();
};