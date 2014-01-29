function createLineChart(divId, chartData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
			
			var response = jq.parseJSON(chartData);
			console.log(response);
			var datum = response.chartData;
			divElement = jq('$'+divId).empty();
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			divElement.append(jq("<div id='chartHolder' class='chartHolder'/>" ));
			
			var divToDraw = d3.select(divElement.get(0)).select("div");
			
			var largeGraph = (datum.length > 10);
			var secondLine = response.secondLine;
			var data = datum;

			var main_margin, mini_margin,  main_width, main_height, mini_height;
				
			if(largeGraph) {
				main_margin = {top: 20, right: 20, bottom: 100, left: 50},
			    mini_margin = {top: fullHeight-70, right: 80, bottom: 20, left: 50},
			    main_width = fullWidth - main_margin.left - main_margin.right,
			    main_height = fullHeight - main_margin.top - main_margin.bottom,
			    mini_height = fullHeight - mini_margin.top - mini_margin.bottom;
			} else {
				main_margin = {top: 20, right: 20, bottom: response.xWidth + 25, left: 50},
			    mini_margin = {top: fullHeight-30 , right: 0, bottom: 30, left: 0},
			    main_width = fullWidth - main_margin.left - main_margin.right,
			    main_height = fullHeight - main_margin.top - main_margin.bottom,
			    mini_height = fullHeight - mini_margin.top - mini_margin.bottom;
			}

			var barLabels = data.map(function(datum){
	            return datum.xData;
	        });
			
			var main_x = d3.scale.ordinal().rangeRoundBands([0, main_width], 1),
				mini_x = d3.scale.ordinal().rangeRoundBands([0, main_width], 1);
			
			var main_y0 = d3.scale.linear().range([main_height, 0]),
				mini_y0 = d3.scale.linear().range([mini_height, 0]);
			
			var main_y1 = d3.scale.linear().range([main_height, 0]),
				mini_y1 = d3.scale.linear().range([mini_height, 0]);
			
			var main_xAxis = d3.svg.axis()
				.scale(main_x)
				.orient("bottom");
			
			var main_yAxisLeft = d3.svg.axis()
				.scale(main_y0)
				.orient("left")
				.tickFormat(d3.format(".2s"));
			
			var brush = d3.svg.brush()
				.x(mini_x)
				.on("brush", brush);
			
			var main_line0 = d3.svg.line()
				.x(function(d) { return main_x(d.xData); })
				.y(function(d) { return main_y0(d.yData); });
			
			var main_line1 = d3.svg.line()
				.x(function(d) { return main_x(d.xData); })
				.y(function(d) { return main_y0(d.yData2); });
			
			var mini_line0 = d3.svg.line()
				.x(function(d) { return mini_x(d.xData); })
				.y(function(d) { return mini_y0(d.yData); });
			
			var mini_line1 = d3.svg.line()
				.x(function(d) { return mini_x(d.xData); })
				.y(function(d) { return mini_y0(d.yData2); });
			
			var maxValue = d3.max(data,function(d){ return d.yData; });
			main_x.domain(barLabels);
			mini_x.domain(main_x.domain());
			
			if(secondLine) {
				var maxValue_y1 = d3.max(data,function(d){ return d.yData2; });
				if(maxValue > maxValue_y1)
					main_y0.domain([0, maxValue]);
				else
					main_y0.domain([0, maxValue_y1]);
				main_y1.domain([0, maxValue_y1]);
				mini_y1.domain(main_y1.domain());
			} else	{
				main_y0.domain([0, maxValue]);
			}
			mini_y0.domain(main_y0.domain());
			
			if(data.length > 11)
				main_xAxis.tickFormat(function (d) { return ''; });
			
			//preparing text/header for chart
			var title_text = function(){
				if(secondLine) {
					return response.yName + ' & ' + response.yName2 + ' by ' + response.xName;
				} else {
					return response.yName + ' by ' + response.xName;
				}
			};
			if(response.filterColumn != null && response.from != null) {
				title_text += ' where '
							+ response.filterColumn + ' between ' + response.from
							+ ' and ' + response.to;
			}
			
			var svg = divToDraw.append("svg")
						.attr("width", main_width + main_margin.left + main_margin.right)
						.attr("height", main_height + main_margin.top + main_margin.bottom);
			
			svg.append("defs").append("clipPath")
				.attr("id", "clip")
				.append("rect")
				.attr("width", main_width)
				.attr("height", main_height);
			
			var main = svg.append("g")
						.attr("transform", "translate(" + main_margin.left + "," + main_margin.top + ")");
			
			var mini = svg.append("g")
				.attr("transform", "translate(" + mini_margin.left + "," + mini_margin.top + ")");
			
			main.append("path")
				.datum(data)
				.attr("clip-path", "url(#clip)")
				.attr("class", "line line0")
				.attr("d", main_line0);
			
			main.append("g")
				.attr("class", "x linechart_axis")
				.attr("transform", "translate(0," + main_height + ")")
				.call(main_xAxis)
				.append("text")
				.attr("transform", "translate(" + main_width/2 + ",0)")
				.attr("dy",response.xWidth + 10)
				.style("text-anchor", "middle")
				.text(response.xName);
			
			var yLabel = main.append("g") 
				.attr("transform", "translate(-43,"+(main_height/2)+")")
			    .append("text")
    		    .attr("transform", "rotate(-90)")
			    .attr("dy", ".71em")
			    .style("text-anchor", "middle");
			
			yLabel.text(response.yName);
			
			if(secondLine) {
				yLabel.text(response.yName + " & " + response.yName2);
				main.append("path")
				.datum(data)
				.attr("clip-path", "url(#clip)")
				.attr("class", "line line1")
				.attr("d", main_line1);
			}
			
			main.append("g")
			    .attr("class", "y linechart_axis axisLeft")
			    .call(main_yAxisLeft);
			
			if(largeGraph) {
				mini.append("g")
				.attr("class", "x linechart_axis mini_axis")
				.attr("transform", "translate(0," + mini_height + ")")
				.call(main_xAxis);
				
				mini.append("path")
				.datum(datum)
				.attr("class", "line")
				.attr("d", mini_line0);
				
				if(secondLine) {	  
				mini.append("path")
				.datum(datum)
				.attr("class", "line")
				.attr("d", mini_line1);
				}
				
				mini.append("g")
				.attr("class", "x brush")
				.call(brush)
				.selectAll("rect")
				.attr("y", -6)
				.attr("height", mini_height + 7);
			}
			 
			var focus = main.append("g")
				.attr("class", "focus")
				.style("display", "none");
				
			// Display on the x Axis
			focus.append("line")
				.attr("class", "x")
				.attr("y1", main_y0(0) - 6)
				.attr("y2", main_y0(0) + 6)
			
			// Display for first line
			focus.append("line")
				.attr("class", "y0")
				.attr("x1", main_width - 6) // nach links
				.attr("x2", main_width + 6); // nach rechts
				
			focus.append("circle")
				.attr("class", "y0")
				.attr("r", 4);
			 
			focus.append("text")
				.attr("class", "y0")  
				.attr("dy", "-1em"); 
			
			if(secondLine) {
				//Display for second line
				focus.append("line")
					.attr("class", "y1")
					.attr("x1", main_width - 6)
					.attr("x2", main_width + 6);
					
				focus.append("circle")
					.attr("class", "y1")
					.attr("r", 4);
				
				focus.append("text")
					.attr("class", "y1")  
					.attr("dy", "-1em");
			}
			
			
			main.append("rect")
				.attr("class", "overlay")
				.attr("width", main_width)
				.attr("height", main_height)
				.on("mouseover", function() { focus.style("display", null); })
				.on("mouseout", function() { focus.style("display", "none"); })
				.on("mousemove", mousemove);
			
			function mousemove() {
				
				console.log(this);
				
				var xPos = d3.mouse(this)[0];
				var leftEdges = main_x.range();
				var width = main_x.rangeBand();
				var j;
				for(j=0; xPos > (leftEdges[j] + width); j++) {}
				d = data[j];
				if(d === undefined) {
					return;
				}
				
				focus.select("circle.y0").attr("transform", "translate(" + main_x(d.xData) + "," + main_y0(d.yData) + ")");
				focus.select("text.y0").attr("transform", "translate(" + main_x(d.xData) + "," + main_y0(d.yData) + ")").text(d.yData);
				focus.select(".x").attr("transform", "translate(" + main_x(d.xData) + ",0)");
				focus.select(".y0").attr("transform", "translate(" + main_width * -1 + ", " + main_y0(d.yData) + ")").attr("x2", main_width + main_x(d.xData));
				if(secondLine) {
					focus.select("circle.y1").attr("transform", "translate(" + main_x(d.xData) + "," + main_y0(d.yData2) + ")");
					focus.select("text.y1").attr("transform", "translate(" + main_x(d.xData) + "," + main_y0(d.yData2) + ")").text(d.yData2);
					focus.select(".y1").attr("transform", "translate(" + main_width * -1 + ", " + main_y0(d.yData2) + ")").attr("x2", main_width + main_x(d.xData));
				}
			}
			
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
			
			function brush() {
			//Creating new subset of data to draw the graph
			var s = d3.event.target.extent();
			var newArray = [];
			var newData = [];
			for (var i = 0; i < barLabels.length; i++) {
			if(s[0] <= (d = mini_x(barLabels[i])) && d <= s[1]) {
				newArray.push(barLabels[i]);
				newData.push(datum[i]);
			}
			}
			main_x.domain(brush.empty() ? mini_x.domain() : newArray);
			
			// Checking when no nodes are selected
			if(newData.length > 0 ) {
				main.datum(newData);
				data = newData;
			} else {
				main.datum(datum);
				data = datum;
			}	
			main.select(".line0").attr("d", main_line0);
			 
			if(secondLine) {
				main.select(".line1").attr("d", main_line1);
			}
			
			main.select(".x.linechart_axis").call(main_xAxis);
			if(data.length < 11)
				main.selectAll('g.x.linechart_axis g text').each(insertLinebreaks);
			}
			
			if(data.length < 11)
				main.selectAll('g.x.linechart_axis g text').each(insertLinebreaks);
			
			//Legend
			var legendData = new Array();
			legendData.push(response.yName)
			
			if(secondLine) {
				legendData.push(response.yName2)
			}

			var legend = svg.selectAll(".legend")
			  .data(legendData)
			  .enter().append("g")
			  .attr("class", "legend")
			  .attr("transform", function(d, i) {return "translate(0," + ((i * 20) + 3)  + ")"; }); //Additional 3 for top-margin

			legend.append("line")
			  .attr("x2", fullWidth - 18 - 10) // 10 for padding
			  .attr("x1", fullWidth - 34 - 10)
			  .attr("y1", 10)
			  .attr("y2", 10)
			  .style("stroke", function(d,i){ if(i==0) return "steelblue"; else return "indianred"; });

			legend.append("text")
			  .attr("x", fullWidth - 40 - 10) // 10 for padding
			  .attr("y", 9)
			  .attr("dy", ".35em")
			  .style("text-anchor", "end")
			  .text(function(d) { return d; });
			
});
	
}