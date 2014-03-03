function createPieChart(divId, jsonArrayData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
		var response = jq.parseJSON(jsonArrayData);
		console.log(response);
		var data = response.chartData;
		divElement = jq('$'+divId).empty();
		var fullHeight = divElement.height();
		var fullWidth = divElement.width();
		
		if(fullWidth < 50 ){ fullWidth = 400; }
		if(fullHeight < 50 ){ fullHeight = 385; }
		
		divElement.append(jq("<div id='chartHolder'/>" ));
		
		var divToDraw = d3.select(divElement.get(0)).select("div");		
		
		//preparing text/header for chart
		divToDraw.append('text').text(
			response.yName + ' by ' + response.xName);
		if(response.filterColumn != null && response.from != null)
			{
				divToDraw.append('text').text( ' where '
						+ response.filterColumn + ' between ' + response.from
						+ ' and ' + response.to);
			}
		//Color scale
		var labels = data.map(function(datum){
            return datum.xData;
        });
		var colorScale = d3.scale.ordinal()
			    .domain(labels)
			    .range(["#66c2a5","#fc8d62","#8da0cb","#e78ac3","#a6d854","#ffd92f","#e5c494"]);
		
		
		// define dimensions of svg
		var height = fullHeight-10,
		    width = fullWidth,
		    radius = Math.min(width, height) / 2;
		
		var arc = d3.svg.arc()
		    .outerRadius(radius - 10)
		    .innerRadius(0);
		
		var arcOver = d3.svg.arc()
	    	.outerRadius(radius - 5)
	    	.innerRadius(0);

		var pie = d3.layout.pie()
		    .sort(null)
		    .value(function(d) { return d.yData; });

		var svg = divToDraw.append("svg")
		    .attr("width", width)
		    .attr("height", height);
		
		var globalg = svg.append("g")
		    .attr("transform", "translate(" + width/2 + "," + height/2 + ")");

		  var g = globalg.selectAll(".arc")
		      .data(pie(data))
		      .enter().append("g")
		      .attr("class", "arc");

		  g.append("path")
		  	  .attr("id", function(d) {
		  		  return stripString(d.data.xData);
		  	  })
		      .attr("d", arc)
		      .style("fill", function(d) { return colorScale(d.data.xData); })
		      .on("mouseover", function(d) {
		    	  highlightPie(d.data);
		      })
		      .on("mouseout", function(d) {
		    	  removeHighlight(d.data);
		      });

		  function pointIsInArc(pt, ptData, d3Arc) {
			    // Center of the arc is assumed to be 0,0
			    // (pt.x, pt.y) are assumed to be relative to the center
			    var r1 = arc.innerRadius()(ptData),
			        r2 = arc.outerRadius()(ptData),
			        theta1 = arc.startAngle()(ptData),
			        theta2 = arc.endAngle()(ptData);
			    
			    var dist = pt.x * pt.x + pt.y * pt.y,
			        angle = Math.atan2(pt.x, -pt.y);
			    
			    angle = (angle < 0) ? (angle + Math.PI * 2) : angle;
			        
			    return (r1 * r1 <= dist) && (dist <= r2 * r2) && 
			           (theta1 <= angle) && (angle <= theta2);
			  }
		  
		  var legend = svg.append("g")
			  .attr("class", "legend")
			  .attr("height", 100)
			  .attr("width", 100);
		  
		  legend.selectAll('rect')
		      .data(data)
		      .enter()
			      .append("rect")
			      .attr("y", function(d,i){ return 12*i; })
				  .attr("width", 10)
				  .attr("height", 10)
				  .style("fill", function(d,i) { return colorScale(d.xData); });
		  
		  legend.selectAll('text')
		      .data(data)
		      .enter()
			      .append("text")
			      .style("font-size","10px")
				  .attr("x", 20)
			      .attr("y", function(d,i){ return (12*i) + 9; })
				  .text(function(d,i) { return d.xData; })
				  	.on("mouseover", function(datum){ highlightPie(datum); })
				  	.on("mouseout", function(datum) {
				  		removeHighlight(datum);
				  	});
		  
		  function highlightPie(data) {
			  
		  		d3.select('#' + stripString(data.xData)).transition()
				.duration(100)
				.attr("d", arcOver)
				.style("fill", function(d) { return d3.rgb(colorScale(d.data.xData)).brighter(.3); });
		  	}
		  
		  function removeHighlight(data) {
			  d3.select('#' + stripString(data.xData)).transition()
				.duration(100)
				.attr("d", arc)
				.style("fill", function(d) { return d3.rgb(colorScale(d.data.xData)); });
		  }
		  
		  function stripString(str) {
			  str = response.portletId + str;
			  return str.replace(/[^a-z0-9\s]/gi, '').replace(/[_\s]/g, '-').replace(/ /g, "_");
		  }
		  
		  var legendWidth=0;
		  legend.each(function(){
			  legendHeight = this.getBBox().height + 10; //Additional 10px as padding
			  legendWidth = this.getBBox().width + 10;
			}); 
		  
		  legend.attr("transform", "translate(" + (width - legendWidth) + ", 10)");
	});
}