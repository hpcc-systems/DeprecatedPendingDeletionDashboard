/**
 * Code for displaying data for the Relavant graph 
 */

var backupAppData = [];
var obj;
function renderRelevantLayout(divId, releventlayout){
	console.log(releventlayout);
	obj = releventlayout;
	var reqData;
	jq('$'+divId).attr("reqData", function(i, origValue){
		reqData = origValue;
    });
	console.log(releventlayout.layout);
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
						"<li><label class=\"relevantBackButton\">"+releventlayout.back+"</label></li>"+
						"<div style=\"height:37px;border-left:1px solid #000;display:inline;float:left;\"> &nbsp;</div>"+
						"<select style=\"float:left;\" id=\"selectbox\" class=\"chartOptions\">"+
						"<option value=\"\">-"+releventlayout.layout+"-</option>"+
						"<option value=\"Randomize\">"+releventlayout.randomize+"</option>"+
						"<option value=\"Circle\">"+releventlayout.circle+"</option>"+
						"<option value=\"ForceDirected\">"+releventlayout.forceDirected+"</option>"+
						"<option value=\"Animated\">"+releventlayout.forceDirectedAnimated+"</option>"+
						"<option value=\"Hierarchy\">"+releventlayout.hierarchy+"</option>"+
						"</select>"+
						
						"<select style=\"float:left;\" id=\"resizeselectbox\" class=\"resizeOptions\">"+
						"<option value=\"\">-"+releventlayout.zoom+"-</option>"+
						"<option value=\"Fit\">"+releventlayout.zoomFit+"</option>"+
						"<option value=\"Expand\">"+releventlayout.zoomWidth+"</option>"+
						"<option value=\"ZoomSelected\">"+releventlayout.zoomSelection+"</option>"+
						"<option value=\"Zoom\">"+releventlayout.zoom100+"</option>"+
						"<option value=\"Show/Hide\">"+releventlayout.showHide+"</option>"+
						"</select>"+
						
						"<select style=\"float:left;\" id=\"filterTable\" class=\"filterTableOptions\">"+
						"<option value=\"all\">"+releventlayout.all+"</option>"+
						"<option value=\"claims\">"+releventlayout.claims+"</option>"+
						"<option value=\"people\">"+releventlayout.people+"</option>"+
						"<option value=\"vehicles\">"+releventlayout.vehicle+"</option>"+
						"<option value=\"policies\">"+releventlayout.policies+"</option>"+							
						"</select>"+						
						"<li><label class=\"showHideSelectTableLabel\"><input type=\"checkbox\" checked class=\"showHideSelectTable\"/>"+releventlayout.showSelection+"</label></li>"+					 	
					 	
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
            if(!chartData.claimId && chartData.groupType && chartData.groupId ){            	
            	app.queryGroup(chartData.groupId,chartData.groupType.id);
            }
            if(entity){
	            if (entity.indexOf("CLM") === 0) {
	            	app.queryClaim(entity);
	            } else if (entity.indexOf("POL") === 0) {
	            	app.queryPolicy(entity);
	            } else if (entity.indexOf("VEH") === 0) {
	            	app.queryVehicle(entity);
	            } else {
	            	app.queryPerson(entity);
	            }
	        }
            divElement.on("click", ".showHideSelectTable",function(event) {
            	app.showSelection(event.target.checked);
            });
            
            divElement.on("change", ".filterTableOptions",function(event) {
            	app.filterEntities(event.target.value);
            });
            
            divElement.on("change", ".resizeOptions",function() {
				switch($("#resizeselectbox").val()){
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
						
				}
			});
			divElement.on("click", ".back",function() {				
				console.log("window.backupAppData -->",window.backupAppData);
				app.deserializefromObject(window.backupAppData.pop());
				 
			});
			
        });        

}
function createRelevantChart(divId, reqData) {
	console.log("div-->"+divId);
	console.log("div-->"+reqData);
	jq('$'+divId).attr("reqData", reqData);
	var releventlayout1 = "layout,randomize,circle,forceDirected,forceDirectedAnimated,hierarchy,showHide,zoom," +
			"zoomFit,zoomWidth,zoomSelection,zoom100,all,claims,people,vehicle,policies,showSelection,selection,property,back," +
			"report_no,company_id,comm_ind,mand_ind,reporter,accident_reason,claim_amount,accident_time,claim_status,accident_place," +
			"accident_description,liability,road_accident,injury_accident,brigandage_accident,third_vehicle,estimate_amount," +
			"main_injury_amount,main_car_amount,third_injury_amount,third_car_amount,third_property_amount,clm_rid,flagged_ind," +
			"rejected_ind,qrypos,partition_id,estimate_factory_code,estimate_factory,balance_factory_code,balance_factory," +
			"vehicle_list,person_list,policy_no,car_mark,driver_name,rack_no,certi_code,encrypt_flag,dob,fpos,person_id,cnt," +
			"name,address,zip,phone,by_claim,by_policy,lhs_person,rhs_person,policy_cnt,entity";
	zAu.send(new zk.Event(zk.Widget.$('$'+divId), "onTraslateLabels",   releventlayout1, {toServer:true}));
}

function resizeGraph() {
	if(app){
		 app.resize().render();
	}
}
	
