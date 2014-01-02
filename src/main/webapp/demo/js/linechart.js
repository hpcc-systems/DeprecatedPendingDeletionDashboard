function createLineChart(divId, chartData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
			
			var response = jq.parseJSON(chartData);
			console.log(response);
			var data= response.chartData;
			var dataCount=0;
			dataCount=data.length;
			divElement = jq('$'+divId).empty();
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			divElement.append(jq("<div id='chartHolder'/>" ));
			
			var divToDraw = d3.select(divElement.get(0)).select("div");
			
			var div = d3.select("body").append("div")   
		    .attr("class", "tooltip")               
		    .style("opacity", 0);
			
			//preparing text/header for chart
			divToDraw.append('text').text(
				response.yName + ' by ' + response.xName);
			if(response.filterColumn != null && response.from != null)
				{
					divToDraw.append('text').text( ' where '
							+ response.filterColumn + ' between ' + response.from
							+ ' and ' + response.to);
				}
		    
			// define dimensions of svg
			var margin = {top: 30, right: 20, bottom: response.xWidth + 25, left: response.yWidth + 15}, //adding 15 for Axis label
			 	width = fullWidth - margin.left - margin.right,
			 	height = fullHeight - margin.top - margin.bottom;
			
			var xscale = d3.scale.ordinal().rangeRoundBands([0, width],1);
			var yscale = d3.scale.linear().rangeRound([height, 0]);

			var xAxis = d3.svg.axis().scale(xscale)
			    .orient("bottom");

			var yAxis = d3.svg.axis().scale(yscale)
			    .orient("left").ticks(5);

			var valueline = d3.svg.line()
				//.interpolate("monotone")
			    .x(function(d) { return xscale(d.xData); })
			    .y(function(d) { return yscale(d.yData); });
			
			// create svg element
			var chart = divToDraw
			              .append('svg') // parent svg element will contain
											// the chart
			              .attr('width', width + margin.left + margin.right)
			              .attr('height', height + margin.top + margin.bottom)
			              .append("g")
			               .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
			
			xscale.domain(data.map(function(d) { return d.xData; }));
			yscale.domain([0, d3.max(data, function(d) { return d.yData; })]);
			
			if(dataCount>10)
				xAxis.tickFormat(function (d) { return ''; });
			
			chart.append("path")
				.attr("class", "lineChart")
				.attr("d",function(d) { return valueline(data); });
			
			chart.append("g")         // Add the X Axis
		        .attr("class", "x axis lineChart")
		        .attr("transform", "translate(0," + height + ")")
		        .call(xAxis)
		        .append("text")
					.attr("transform", "translate(" + width/2 + ",0)")
					.style("text-anchor", "center")
					.attr("dy", (response.xWidth+10)+"px")
					.text(response.xName);
			
			chart.append("g") 
	        .attr("class", "line-point")
	        .selectAll('circle')
	        .data(data)
	        .enter().append('circle')
	        .attr("cx", function(d, i) {
	            console.log("xscale(d.xData)--"+xscale(d.xData));
	        	return xscale(d.xData) + xscale.rangeBand() / 2;
	          })
	         .attr("cy", function(d, i) { return yscale(d.yData); })
	         .on("mouseover", function(d) {      
	            div.transition()        
	                .duration(200)      
	                .style("opacity", .9);      
	            div .html(d.xData + "<br/>"  + d.yData)  
	                .style("left", (d3.event.pageX) + "px")     
	                .style("top", (d3.event.pageY - 28) + "px");    
	            })
	         .on("mouseout", function(d) {       
	            div.transition()        
	                .duration(500)      
	                .style("opacity", 0);   
	         	})   
	         .attr("r", 3)
	         .style("fill","#0000FF");
			
			chart.append("g")         // Add the Y Axis
		        .attr("class", "y axis lineChart")
		        .call(yAxis)
		        .append("text")
					.attr("transform", "translate(0, " + height + ")")
					.attr("transform", "rotate(-90)")
					.style("text-anchor", "center")
					.attr("dy", "-" + (response.yWidth) )
					.attr("dx", "-" + (height/2))
					.text(response.yName);
		 
		  var insertLinebreaks = function (d) {
			    var el = d3.select(this);
			    var words = d.split(' ');
			    el.text('');

			    for (var i = 0; i < words.length; i++) {
			        var tspan = el.append('tspan').text(words[i]);
			        if (i > 0)
			            tspan.attr('x', 0).attr('dy', '15');
			    }
			};
		
			if(dataCount<10)	
				chart.selectAll('g.x.lineChart g text').each(insertLinebreaks);
});
	
}