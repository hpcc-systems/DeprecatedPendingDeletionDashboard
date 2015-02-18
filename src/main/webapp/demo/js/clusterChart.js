function createClusterChart(divId, reqData) {
	var chartData = jq.parseJSON(reqData);
	console.log(chartData);		
	
	var divElement = jq('$'+divId).empty();
	divElement.append(jq("<div id='chartHolder' style='position: absolute;'/>" ));		
	var container = d3.select(divElement.get(0)).select("div");
	
	// size of the diagram
	var height = divElement.height();
	var width = divElement.width();
	
	//checking the minimum height for browser and component and set the same as window height. 
	if(divElement.parent().parent().height()<($(window).height()-150)){height = divElement.parent().parent().height();}
	else{height = ($(window).height()-150);}
	
	if(divElement.width()>$(window).width()){width = $(window).width();}

	var force = d3.layout.force()
	    .gravity(.05)
	    .charge(-200)
	    .size([width, height]);

	var svg = container.append("svg")
	    .attr("width", width)
	    .attr("height", height);
	    
	var tooltip = container.append("div")
		.attr("class", "cluster-tooltip hidden");
			
	tooltip.append("p");
	
	 var color = d3.scale
	  	.ordinal()
	  	.range(["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", 
	  	        "#e377c2", "#00FFFF", "#7FFFD4", "#FFE4C4", "#0000FF", "#8A2BE2",
	  	        "#A52A2A", "#5F9EA0", "#7FFF00", "#FF7F50", "#6495ED", "#00008B", 
	  	        "#008B8B", "#B8860B", "#A9A9A9", "#006400", "#9932CC", "#8FBC8F", 
	  	        "#483D8B", "#FF1493", "#DAA520", "#FF69B4", "#9370DB", "#C71585", 
	  	        "#191970", "#00FA9A", "#6B8E23", "#FFA500", "#F4A460", "#9ACD32"])
	  	.domain(chartData.types);
	 
	 var lineColor = d3.scale
		.ordinal()
		.range(["#00a300", "#ff0097", "#7e3878", "#2d89ef", "#da532c", "#1d1d1d", 
		        "#603cba", "#808000", "#FF4500", "#a6cee3", "#1f78b4","#b2df8a",
		        "#33a02c","#fb9a99","#e31a1c","#fdbf6f","#ff7f00","#cab2d6",
		        "#6a3d9a","#ffff99"])
		.domain(chartData.linkTypes);
	 
	 var legend = svg.append("g")
	  	.attr("class","legend")	  	
		.attr('transform', 'translate(-20,50)')
		.style("font-size","10px");
	

	 legend.selectAll('rect').data(chartData.linkTypes).enter().append("rect")
		.attr("x", width - 100).attr("y", function(d, i) {return i * 20;})
		.attr("width", 8).attr("height", 8).style("fill", lineColor); 	
	 

	legend.selectAll('text').data(chartData.linkTypes).enter().append("text")
			.attr("x", width - 87).attr("y", function(d, i) {return i * 20 + 7;})
			.text(function(d) {return d;});	  
	  
	 var circleRadius = function(d) {
		  radius = d.weight * 2;
		  
		  if(radius > 8) {
			  return 10;
		  }
		  return radius;
	  }
	  
	  force
	      .nodes(chartData.nodes)
	      .links(chartData.links)
	      .linkDistance(100)
	      .start();
	
	  var link = svg.selectAll(".cluster-link")
	      .data(chartData.links)
	    .enter().append("line")
	      .attr("class", "cluster-link")
	      .style("stroke", function(d) { return lineColor(d.type); })
	      .style("stroke-width", 2);
	
	  
	  
	  var node = svg.selectAll(".cluster-node")
	      .data(chartData.nodes)
	    .enter().append("g")
	      .attr("class", "cluste-node")
	      .call(force.drag);
	  
	  node.append("circle")
	  	.attr("r", 8)
	  	.style("fill", function(d) {  return  color(d.type); })
	  	.on("mouseover", mouseover)
	    .on("mouseout", mouseout);
	  
	  function mouseover(d) {
		  tooltip
		  	.style("left", (d.x + circleRadius(d)/2) + "px")
		  	.style("top", (d.y + circleRadius(d)/2) + "px")
		  	.classed("hidden", false);
		  
		  tooltip.select("p")
		  	.text(function(){
		  		return d.detail;
		  	});
		  
		  d3.select(this).
		  	style("fill", function(d) { 
		  		return d3.rgb(color(d.type)).brighter(1); 
		  	});
	  }
	  
	  function mouseout(d) {
		  tooltip.classed("hidden", true);
		  
		  d3.select(this).style("fill", function(d) { return color(d.type); });
	  }
	    
	  force.on("tick", function() {
	    link.attr("x1", function(d) { return d.source.x; })
	        .attr("y1", function(d) { return d.source.y; })
	        .attr("x2", function(d) { return d.target.x; })
	        .attr("y2", function(d) { return d.target.y; });
	
	    node
	    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	  });
}