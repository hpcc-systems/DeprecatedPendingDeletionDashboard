function clearChart(divId)
{
	jq('$'+divId).empty();	
}

function createChart(divId, chartData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
			
			var response = jq.parseJSON(chartData);
			console.log(response);
						
			divElement = jq('$'+divId).empty();
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			divElement.append(jq("<div id='chartHolder'/>" ));
			
			var divToDraw = d3.select(divElement.get(0)).select("div");
		    
			var margin = {top: 25, right: 20, bottom: response.xWidth + 20, left: 50},
		    width = fullWidth - margin.left - margin.right,
		    height = fullHeight - margin.top-margin.bottom;

			var x0 = d3.scale.ordinal()
			    .rangeRoundBands([0, width], .03);
	
			var x1 = d3.scale.ordinal();
	
			var y = d3.scale.linear()
			    .range([height, 0]);
	
			var color = d3.scale.ordinal()
			    .range( ["#fc8d59","#fee08b","#ffffbf","#d9ef8b","#91cf60","#1a9850"]);
	
			var xAxis = d3.svg.axis()
			    .scale(x0)
			    .orient("bottom");
	
			var yAxis = d3.svg.axis()
			    .scale(y)
			    .orient("left")
			    .tickFormat(d3.format(".2s"));
			
			var parentsvg = divToDraw.append("svg")
			    .attr("width", width + margin.left + margin.right)
			    .attr("height", height + margin.top +margin.bottom);
				
			var svg = parentsvg.append("g")
			    .attr("transform", "translate(" + margin.left + "," + (margin.top) + ")");
				
			//preparing text/header for chart
			var chartheader =parentsvg.append("g")
			.attr("class", "chart header")
			.attr("transform", "translate(0,15)");
			chartheader.append('text').text(function(){
				if(response.secondLine) {
					var text= response.yName + ' & ' + response.yName2 + ' by ' + response.xName;
					if(response.filterColumn != null )
					{				
						if(response.from != null)
						{
							text+=' where '+ response.filterColumn
								+' between ' + response.from + ' and ' + response.to;
						}
					}
					return text;
				} else {
					var text= response.yName + ' by ' + response.xName;
					if(response.filterColumn != null )
					{				
						if(response.from != null)
						{
							text+=' where '+ response.filterColumn
								+' between ' + response.from + ' and ' + response.to;
						}
					}
					return text;
				}
					
			});
			
			var data = response.chartData;
			var ageNames = d3.keys(data[0]).filter(function(key) { return key !== "xData"; });
		    data.forEach(function(d) {
				d.ages = ageNames.map(function(name) { return {name: name, value: +d[name]}; });
			  });
			console.log(data);
			
			 x0.domain(data.map(function(d) { return d.xData; }));
			 x1.domain(ageNames).rangeRoundBands([0, x0.rangeBand()]);
			 y.domain([0, d3.max(data, function(d) { return d3.max(d.ages, function(d) { return d.value; }); })]);
			  
			if(data.length > 11)
			  xAxis.tickFormat(function (d) { return ''; });
			 
			svg.append("g")
			    .attr("class", "x axis xAxis")
			    .attr("transform", "translate(0," + (height) + ")")
			    .call(xAxis)
			    .append("text")
			    .attr("transform", "translate(" + fullWidth/2 + ",0)")
			    .attr("dy",response.xWidth + 10)
			    .style("text-anchor", "middle")
			    .text(response.xName);
			  
			var yLabel = svg.append("g") 
				.attr("transform", "translate(-43,"+(height/2)+")")
			    .append("text")
    		    .attr("transform", "rotate(-90)")
			    .attr("dy", ".71em")
			    .style("text-anchor", "middle");
			  
			if(response.secondLine){
			   yLabel.text(response.yName + " & " + response.yName2);
			} else {
				yLabel.text(response.yName);
			}
			
			svg.append("g")
			      .attr("class", "y axis yAxis")
			      .call(yAxis);

		    var state = svg.selectAll(".state")
			  .data(data)
			  .enter().append("g")
			  .attr("class", "g")
			  .attr("transform", function(d) { return "translate(" + x0(d.xData) + ",0)"; });
	
			state.selectAll("rect")
			  .data(function(d) { return d.ages; })
			  .enter().append("rect")
			  .attr("width", x1.rangeBand())
			  .attr("x", function(d) { return x1(d.name); })
			  .attr("y", function(d) { return y(d.value); })
			  .attr("height", function(d) { return height - y(d.value); })
			  .style("fill", function(d) { return color(d.name); });
	
			var legend = parentsvg.selectAll(".legend")
			  .data(ageNames.slice().reverse())
			  .enter().append("g")
			  .attr("class", "legend")
			  .attr("transform", function(d, i) { var i=i+1;return "translate(0," + i * 5 + ")"; });
	
			legend.append("rect")
			  .attr("x", width - 18)
			  .attr("width", 18)
			  .attr("height", 18)
			  .style("fill", color);
	
		    legend.append("text")
			  .attr("x", width - 24)
			  .attr("y", 9)
			  .attr("dy", ".35em")
			  .style("text-anchor", "end")
			  .text(function(d) { return d; });
			
			//inserting line breaks
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
			if(data.length < 11)
				svg.selectAll('g.xAxis g text').each(insertLinebreaks);
});
}

