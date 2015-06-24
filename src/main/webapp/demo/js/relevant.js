/**
 * Code for displaying data for the Relavant graph 
 */
var graphData = [];
var graph;
var frame;
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
        

	        require([ "src/layout/Surface", "src/layout/Grid", "src/other/Comms",
					"src/graph/Graph", "src/graph/Edge", "src/graph/Vertex",
					"src/other/Table", "src/chart/Column","src/other/Persist" ],
			function(Surface, Grid, Comms, Graph, Edge, Vertex, Table, Column, Persist) {
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
				 	"</nav>" +
				 "</header>"));
        	
        	divElement.append(jq("<div id='chartHolder' class='about' />" ));
        	chartHolderDiv = d3.select(divElement.get(0)).select("#chartHolder").attr('id');
        	console.log("container to attach chart: "+chartHolderDiv);
        	
        	// size of the diagram
            width = divElement.width();
            height = divElement.height();
            
            var vertices = [];
            var vertexMap = [];
            var edges = [];
            var edgeMap = {};
            var claimMap = {};
            
           graph = new Graph()
	            .layout("ForceDirected")
	            .hierarchyRankDirection("TB")
	            .hierarchyNodeSeparation(20)
	            .hierarchyRankSeparation(10)
	            .applyScaleOnLayout(false)
	            .highlightOnMouseOverVertex(true) ;
            
            graph.vertex_dblclick = function (d) {
            	 d3.event.stopPropagation();
            	 //backup the present vertices and edges of graph object, will be used for back button
            	/* var vertexedgeMap = [];
            	 var vertex;
            	 var edge;
            	 var vertexStr = [];
            	 var edgeStr = [];
                 for (var index = 0; index < graph.data().vertices.length; index++) {
                	 vertex = graph.data().vertices[index];                	
                	 vertexStr.push(Persist.serialize(vertex));
                 }
            	
                 for (var index = 0; index < graph.data().edges.length; index++) {
                	 edge = graph.data().edges[index];
                	 edgeStr.push(Persist.serialize(edge));
                 }
                 vertexedgeMap["vertices"] = vertexStr;
                 vertexedgeMap["edges"] = edgeStr;
                 graphData.push(vertexedgeMap);
                 console.log('vertexedgeMap -->',vertexedgeMap);*/
                callService(d._id, d.element()); 
            };
            
            graph.graph_selection = function (selection) {
                populateTableV(selectionTable, selection);
                vertexTable.selection(selection.map(function (vertex) {
                    return vertex.__tableRowIdx;
                })).render();
            };
            graph.vertex_click = function (d) {
                this.graph_selection(graph.selection());
            };
            
           var selectionTable = new Table();

           var vertexTable = new Table();
            
            vertexTable.click = function (row, col) {
                var selection = this.selection().map(function (item) {
                    return item[item.length - 1];
                });
                graph
                    .selection(selection)
                    .render()
                ;
                populateTableV(selectionTable, selection);
            };
            
       var claimsChart = new Column()
		            .columns(["Date", "Amount"])
		            .selectionMode(true)
		            .xAxisType("timeseries")
		            .timeseriesPattern("%Y-%m-%d %H:%M:%S") ;
            
        claimsChart.selection = function (selected) {
            var selection = selected.map(function (row) { return row[2]; });
            graph
                .selection(selection)
                .render();
            graph.graph_selection(selection);
        }

        
        var main = new Grid()
	        .setContent(0, 0, claimsChart, "", 1, 4)
	        .setContent(1, 0, graph, "", 6, 4)
	        .setContent(0, 4, selectionTable, "Selection", 7, 1)
	        .setContent(7, 0, vertexTable, "", 2, 5) ;
        
        frame = new Surface();        
        frame.widget(main)
	        .target(chartHolderDiv)
	        .render();
        
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
            
            var service = Comms.createESPConnection(url);
            
            var search = window.location.search.split("?");
            var entity = search[search.length - 1];
            if (!entity) {
                entity = chartData.claimId;
            }
            if (entity.indexOf("CLM") === 0) {
                callService("c_" + entity);
            } else if (entity.indexOf("POL") === 0) {
                callService("pol_" + entity);
            } else if (entity.indexOf("VEH") === 0) {
                callService("v_" + entity);
            } else {
                callService("p_" + entity);
            }
            
            function callService(id, element) {
                if (element) {
                    element.classed("expanding", true);
                }
                var request = null;
                var catId = id.split("_");
                switch (catId[0]) {
                    case "c":
                        request = { claim_ids: catId[1] };
                        break;
                    case "p":
                        request = { person_ids: catId[1]};
                        break;
                    case "pol":
                        break;
                    case "v":
                        request = { vehicle_ids: catId[1] };
                        break;
                }
                
                if (!request) {
                    if (element) {
                        element.classed("expanding", false);
                        element.classed("expanded", true);
                    }
                } else {
                    service.send(request, function(response) {
                        if (element) {
                            element.classed("expanding", false);
                            element.classed("expanded", true);
                        }
                        response.claim_list.forEach(function (item, i) {   
                        	console.log(item.accident_time);
                            var claim = getVertex("c_" + item.report_no, "\uf0d6", item.report_no, item);
                            claimMap[item.report_no] = {
                                    date: item.accident_time,
                                    amount: item.claim_amount,
                                    claim: claim
                                };
                            var annotations = [];
                            if (item.road_accident && item.road_accident !== "0") {
                                annotations.push({
                                    "faChar": "\uf018",
                                    "tooltip": "Road Accident",
                                    "shape_color_fill": "darkgreen",
                                    "image_color_fill": "white"
                                });
                            }
                            if (item.third_vehicle && item.third_vehicle !== "0") {
                                annotations.push({
                                    "faChar": "\uf1b9",
                                    "tooltip": "Third Vehicle",
                                    "shape_color_fill": "navy",
                                    "image_color_fill": "white"
                                });
                            }
                            if (item.injury_accident && item.injury_accident !== "0") {
                                annotations.push({
                                    "faChar": "\uf067",
                                    "tooltip": "Injury Accident",
                                    "shape_color_fill": "white",
                                    "shape_color_stroke": "red",
                                    "image_color_fill": "red"
                                });
                            }
                            claim.annotationIcons(annotations);
                        });
                       
                        response.claim_list.forEach(function (item, i) {
                        	getVertex("c_" + item.report_no, chartData.claimImage, item.report_no, item);
                        });
                        response.policy_list.forEach(function (item, i) {
                            getVertex("pol_" + item.car_mark, chartData.policyImage, item.car_mark, item);
                        });
                        response.person_list.forEach(function (item, i) {
                            getVertex("p_" + item.person_id, chartData.personImage, item.person_id, item);
                        });
                        response.vehicle_list.forEach(function (item, i) {
                            getVertex("v_" + item.rack_no, chartData.vehicleImage, item.rack_no, item);
                        });
                        response.claim_policy.forEach(function (item, i) {
                            getEdge(vertexMap["c_" + item.report_no], vertexMap["pol_" + item.car_mark], "", item);
                        });
                        response.claim_person.forEach(function (item, i) {
                            getEdge(vertexMap["c_" + item.report_no], vertexMap["p_" + item.person_id], "", item);
                        });
                        response.claim_vehicle.forEach(function (item, i) {
                            getEdge(vertexMap["c_" + item.report_no], vertexMap["v_" + item.rack_no], "", item);
                        });
                        response.person_policy.forEach(function (item, i) {
                            getEdge(vertexMap["pol_" + item.car_mark], vertexMap["p_" + item.person_id], "", item);
                        });
                        response.person_person.forEach(function (item, i) {
                            getEdge(vertexMap["p_" + item.lhs_person], vertexMap["p_" + item.rhs_person], "", item);
                        });
                        response.person_vehicle.forEach(function (item, i) {
                            getEdge(vertexMap["p_" + item.person_id], vertexMap["v_" + item.rack_no], "", item);
                        });

                        graph
                           .data({ vertices: vertices, edges: edges, merge: true })
                            .render()
                            .layout(graph.layout(), transitionDuration);
                      
                        var claimsData = [];
                        for (var key in claimMap) {
                        	claimsData.push([claimMap[key].date, claimMap[key].amount, claimMap[key].claim]);
                        }
                        console.log("claimsData-->",claimsData);
                        claimsChart
                            .data(claimsData)
                            .render();
                        
                        populateTableH(vertexTable, vertices);
                    });
                }
            }
            
            function populateTableV(table, selection) {
                var columns = ["Property"];
                var propIdx = {};
                var data = [];
                selection.forEach(function (item, idx) {
                    columns.push(item.text());
                    var props = item.data();
                    for (var key in props) {
                        var row = null;
                        if (propIdx[key] === undefined) {
                            propIdx[key] = data.length;
                            row = [key];
                            row.length = selection.length + 1;
                            data.push(row);
                        } else {
                            row = data[propIdx[key]];
                        }
                        row[idx + 1] = props[key];
                    }
                });
                table
                    .columns(columns)
                    .data(data)
                    .render()
                ;
            }

            function populateTableH(table, selection) {
                var columns = ["Entity"];
                var entityIdx = {};
                var propIdx = {};
                var data = [];
                selection.forEach(function (item, idx) {
                    var row = [item.text()];
                    var props = item.data();
                    for (var key in props) {
                        if (propIdx[key] === undefined) {
                            propIdx[key] = columns.length;
                            columns.push(key);
                        }
                        row[propIdx[key]] = props[key];
                    }
                    data.push(row);
                });
                data.forEach(function (row, idx) {
                    row.length = columns.length + 1;
                    row[columns.length] = selection[idx];
                    selection[idx].__tableRowIdx = row;
                });
                table
                    .columns(columns)
                    .data(data)
                    .render()
                ;
            }

            function getVertex(id, faChar, label, data) {
                var retVal = vertexMap[id];
                if (!retVal) {
                    retVal = new Vertex()
                        .id(id)
                        .text(label)
                        .faChar(faChar)
                        .data(data)
                    ;
                    vertexMap[id] = retVal;
                    vertices.push(retVal);
                }
                return retVal;
            }

            function getEdge(source, target, label) {
                var id = source._id + "_" + target._id;
                var retVal = edgeMap[id];
                if (!retVal) {
                    retVal = new Edge()
                        .id(id)
                        .sourceVertex(source)
                        .targetVertex(target)
                        .sourceMarker("circleFoot")
                        .targetMarker("arrowHead")
                        .text(label || "")
                    ;
                    edgeMap[id] = retVal;
                    edges.push(retVal);
                }
                return retVal;
            }

			
			divElement.on("change", ".chartOptions",function() {
				switch($("#selectbox").val()){
					case "Circle":
						graph.layout('Circle', transitionDuration);
						break;
						
					case "Randomize":
						console.log("Calling Do Random...");
		                var maxV = Math.floor(Math.random() * 100);
		                var maxE = Math.floor(Math.random() * 100);
		                for (var i = 0; i < maxV; ++i) {
		                    var fromV =  getVertex("v" + i, "", i);
		                }
		                for (var i = 0; i < maxE; ++i) {
		                    var fromIdx = Math.floor(Math.random() * vertices.length);
		                    var toIdx = Math.floor(Math.random() * vertices.length);
		                    getEdge(vertices[fromIdx], vertices[toIdx]);
		                }
		                graph
		                    .data({ vertices: vertices, edges: edges, merge: true })
		                    .render()
		                    .layout(graph.layout(), transitionDuration)
		                ;
						break;
						
					case "ForceDirected":
						graph.layout('ForceDirected', transitionDuration);
						break;
						
					case "Animated":
						graph.layout('ForceDirected2', transitionDuration);
						break;
						
					case "Hierarchy":
						graph.layout('Hierarchy', transitionDuration);
						break;
						
					case "Show/Hide":
						graph.showEdges(!graph.showEdges()).render();
						break;
						
					case "Fit":
						graph.zoomTo('all');
						break;
						
					case "Expand":
						graph.zoomTo('width');
						break;
						
					case "ZoomSelected":
						graph.zoomTo('selection');
						break;
						
					case "Zoom":
						graph.zoomTo('100%');
						break;
						
				}
			});
			divElement.on("click", ".back",function() {
				
				/*var backupGraph = graphData.pop();
				var verticesAry = backupGraph["vertices"];
				var edgesAry = backupGraph["edges"];
				var newData = {
					    vertices: [],
					    edges: [],
					    merge : false
					};	
				for(var index = 0; index < verticesAry.length; index++){	
					Persist.create(verticesAry[index], function(vertex) {
						newData.vertices.push(vertex)
						});
				}
				
				for(var index = 0; index < edgesAry.length; index++){					
					Persist.create(edgesAry[index], function(edge) {
						newData.edges.push(edge)
						});
				}
				
				graph
                 .data({ vertices: newData.vertices, edges: newData.edges, merge: false })
                  .render();*/
				
				/* main.setContent(1, 0, graph, "", 6, 4);
				 main.render();*/
			});
			
        });        
}

function resizeGraph() {
	if(frame){
		frame.resize().render();
	}
}
	
