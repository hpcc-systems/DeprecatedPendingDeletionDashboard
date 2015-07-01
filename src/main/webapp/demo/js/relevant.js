/**
 * Code for displaying data for the Relavant graph 
 */
var app;
var backupAppData = [];
function createRelevantChart(divId, reqData) {
	var chartData = jq.parseJSON(reqData);
	console.log(chartData);	
	
	console.log("file -->"+chartData.files[0]);	
	console.log("Calling createRelevantChart...");
	
        var table = null;
        var doRandom = null;
        var chartHolderDiv = null;
        var divElement = null;
        var width = null;
        var height = null;
        var transitionDuration = 250;
        var tables = [];
       
        requirejs.config({
			baseUrl: "js/relevant/visualization/a"
		});
        

	        require([ "src/app/Main"],
			function(Main) {
        	console.log("Loading relevant widgets...");
        	
        	divElement = jq('$'+divId).empty();
        	
        	divElement.append(jq("<header>" +
					"<nav>" +
						"<a style=\"float:left;\" class=\"back\"> <i class=\"fa fa-arrow-left\"></i></a>"+	
						"<div style=\"height:37px;border-left:1px solid #000;display:inline;float:left;\"> &nbsp;</div>"+
						"<select style=\"float:left;\" id=\"selectbox\" class=\"chartOptions\">"+
						"<option value=\"\">-layout-</option>"+
						"<option value=\"Randomize\">Randomize</option>"+
						"<option value=\"Circle\">Circle</option>"+
						"<option value=\"ForceDirected\">Force Directed</option>"+
						"<option value=\"Animated\">Force Directed(Animated)</option>"+
						"<option value=\"Hierarchy\">Hierarchy</option>"+
						"<option value=\"Show/Hide\">Show/Hide</option>"+
						"<option value=\"Fit\">Zoom:Fit</option>"+
						"<option value=\"Expand\">Zoom:Width</option>"+
						"<option value=\"ZoomSelected\">Zoom:Selection</option>"+
						"<option value=\"Zoom\">Zoom:100%</option>"+
						"</select>"+	
						
					 	"<li><label class=\"showHideSelectTableLabel\"><input type=\"checkbox\" checked class=\"showHideSelectTable\"/>Show Selection</label></li>"+					 	
						"<select style=\"float:left;\" id=\"filterTable\" class=\"filterTableOptions\">"+
						"<option value=\"all\">All</option>"+
						"<option value=\"claims\">Claims</option>"+
						"<option value=\"people\">People</option>"+
						"<option value=\"vehicles\">Vehicle</option>"+
						"<option value=\"policies\">Policies</option>"+							
						"</select>"+						
					 	
				 	"</nav>" +
				 "</header>"));
        	
        	divElement.append(jq("<div id='chartHolder' class='about' />" ));
        	chartHolderDiv = d3.select(divElement.get(0)).select("#chartHolder").attr('id');
        	console.log("container to attach chart: "+chartHolderDiv);
        	
        	// size of the diagram
            width = divElement.width();
            height = divElement.height();
            
            var url= "";
            if(chartData.hpccConnection.isHttps == true){
            	url = url.concat("https://");
            }else{
            	url = url.concat("http://");
            }
            url = url.concat(chartData.hpccConnection.serverHost);
            url = url.concat(":");
            url = url.concat(chartData.hpccConnection.wsEclPort);
            url = url.concat("/?QuerySetId=roxie&Id=");
            url = url.concat(chartData.files[0]);
            url = url.concat("&Widget=QuerySetDetailsWidget");
            
            console.log("url --->"+url);
            
            app = new Main()
	            .url(url)
	            .target(chartHolderDiv)	            
	            .render();
            
          var search = window.location.search.split("?");
            var entity = search[search.length - 1];
            if (!entity) {
                entity = chartData.claimId;
            }
            if (entity.indexOf("CLM") === 0) {
            	app.queryClaim(entity);
            } else if (entity.indexOf("POL") === 0) {
            	app.queryPolicy(entity);
            } else if (entity.indexOf("VEH") === 0) {
            	app.queryVehicle(entity);
            } else {
            	app.queryPerson(entity);
            }
            divElement.on("click", ".showHideSelectTable",function(event) {
            	app.showSelection(event.target.checked);
            });
            
            divElement.on("change", ".filterTableOptions",function(event) {
            	app.filterEntities(event.target.value);
            });
            
			divElement.on("change", ".chartOptions",function() {
				switch($("#selectbox").val()){
					case "Circle":
						app.graph.layout('Circle', transitionDuration);
						break;
						
					case "Randomize":
						console.log("Calling Do Random...");
		                var maxV = Math.floor(Math.random() * 100);
		                var maxE = Math.floor(Math.random() * 100);
		                for (var i = 0; i < maxV; ++i) {
		                    var fromV =  app.getVertex("v" + i, "", i);
		                }
		                for (var i = 0; i < maxE; ++i) {
		                    var fromIdx = Math.floor(Math.random() *app.vertices.length);
		                    var toIdx = Math.floor(Math.random() * app.vertices.length);
		                    app.getEdge(app.vertices[fromIdx], app.vertices[toIdx]);
		                }
		                
		                app.graph
			                .data({ vertices: app.vertices, edges: app.edges, merge: true })
			                .render()
			                .layout(app.graph.layout(), transitionDuration);
						break;
						
					case "ForceDirected":
						app.graph.layout('ForceDirected', transitionDuration);
						break;
						
					case "Animated":
						app.graph.layout('ForceDirected2');
						break;
						
					case "Hierarchy":
						app.graph.layout('Hierarchy', transitionDuration);
						break;
						
					case "Show/Hide":
						app.graph.showEdges(!app.graph.showEdges()).render();
						break;
						
					case "Fit":
						app.graph.zoomTo('all');
						break;
						
					case "Expand":
						app.graph.zoomTo('width');
						break;
						
					case "ZoomSelected":
						app.graph.zoomTo('selection');
						break;
						
					case "Zoom":
						app.graph.zoomTo('100%');
						break;
						
				}
			});
			divElement.on("click", ".back",function() {				
				console.log("window.backupAppData -->",window.backupAppData);
				app.deserializefromObject(window.backupAppData.pop());
				 
			});
			
        });        
}

function resizeGraph() {
	if(app){
		 app.resize().render();
	}
}
	
