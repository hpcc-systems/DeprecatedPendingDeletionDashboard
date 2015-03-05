/**
 * Code for displaying data for the Relavant graph 
 */

function createRelevantChart(divId, reqData) {
	var chartData = jq.parseJSON(reqData);
	var clonedGraph ;
	console.log(chartData);	
	
	console.log("file -->"+chartData.files[0]);	
	console.log("Calling createRelevantChart...");
	
        var table = null;
        var doRandom = null;
        var cholderDiv = null;
        var divElement = null;
        var width = null;
        var height = null;
        var transitionDuration = 250;
        
        requirejs.config({
			baseUrl: "js/relevant/widgets"
		});
        
        require(["src/other/Comms", "src/graph/Graph", "src/graph/Edge", "src/graph/Vertex", "src/other/Table"], function (Comms, Graph, Edge, Vertex, Table) {
        	console.log("Loading relevant widgets...");
        	
        	divElement = jq('$'+divId).empty();
        	divElement.append(jq("<div id='chartHolder' class='about' />" ));
        	divElement.append(jq("<div id='table' class='about tableDiv'/>" ));
        	cholderDiv = d3.select(divElement.get(0)).select("#chartHolder").attr('id');
        	console.log("container to attach chart: "+cholderDiv);
        	
        	// size of the diagram
            width = divElement.width();
            height = divElement.height();
            
            var vertices = [];
            var vertexMap = [];
            var edges = [];
            var edgeMap = {};

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


            var url = "https://10.173.147.1:8010/?QuerySetId=roxie&Id=";
            url = url.concat(chartData.files[0]);
            url = url.concat("&Widget=QuerySetDetailsWidget");
            console.log("url -->"+url);
            
            var service = Comms.createESPConnection(url);
            
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
                            .layout(graph.layout(), transitionDuration)
                        ;
                    });
                }
            }

            graph = new Graph()
                .target(cholderDiv)
                .layout("ForceDirected")
                .hierarchyOptions({
                    rankdir: "TB",
                    nodesep: 20,
                    ranksep: 10
                })
                .shrinkToFitOnLayout(false)
                .highlightOnMouseOverVertex(true)
            ;
            graph.vertex_dblclick = function (d) {
            	clonedGraph = jQuery.extend(true, {}, graph);
                callService(d._id, d.element());
            };
            
            graph.vertex_click = function (d) {
            	console.log("Calling graph.vertex_click");
                /* table
                    .data([])
                    .render()
                ; */
                var props = d.data();
                
                var data = [];
                for (var key in props) {
                    data.push([key, props[key]]);
                }
                console.log(data);
                /* table
                    .data(data)
                    .render()
                ; */
                hot.loadData(data);
                hot.render();
            };

            //callService("c_" + "CLM00042945-C034"); 
            //callService("c_" + chartData.claimId);
            
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
            

            //  Table  ---
            /* table = new Table()
                .target("table")
                .columns(["Property", "Value"])
                .render()
            ; */
            
            var dummyData = [["", ""]];
			var container = document.getElementById('table');
			var config = {
					data : dummyData,
					manualColumnResize: true,
					columnSorting: true,
					currentRowClassName: 'currentRow',
					currentColClassName: 'currentCol',
					colHeaders: ["Property", "Value"]
			};
			
			var hot = new Handsontable(container, config);
			
			divElement.on("click", ".randomize", function() {
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
            
			});
			
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
						
				}
			});
			
			divElement.append(jq("<header>" +
					"<nav>" +
						"<a style=\"float:left;\" class=\"back\"> <i class=\"fa fa-arrow-left\"></i></a>"+	
						"<div style=\"height:37px;border-left:1px solid #000;display:inline;float:left;\"> &nbsp;</div>"+
						"<select style=\"float:left;\" id=\"selectbox\" class=\"chartOptions\">"+
						"<option value=\"\">-layout-</option>"+
						"<option value=\"Randomize\">R</option>"+
						"<option value=\"Circle\">C</option>"+
						"<option value=\"ForceDirected\">F</option>"+
						"<option value=\"Animated\">F2</option>"+
						"<option value=\"Hierarchy\">H</option>"+
						"<option value=\"Show/Hide\">E</option>"+
						"</select>"+						
				 	"</nav>" +
				 "</header>"));
			
        });
        
}

function resizeGraph() {
	if(graph){
		graph.resize();
	}
}
	
