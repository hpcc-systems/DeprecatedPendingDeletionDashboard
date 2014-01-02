function clearChart(divId)
{
	jq('$'+divId).empty();	
}

function createChart(divId, chartData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
			
			var response = jq.parseJSON(chartData);
			console.log(response);
			console.log(divId);
			
			
			divElement = jq('$'+divId).empty();
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			divElement.append(jq("<div id='chartHolder'/>" ));
			
			var divToDraw = d3.select(divElement.get(0)).select("div");
		    
			// define dimensions of svg
			var h = fullHeight,
			    w = fullWidth;
			
			//preparing text/header for chart
			divToDraw.append('text').text(
				response.yName + ' by ' + response.xName);
			if(response.filterColumn != null )
				{				
				if(response.from != null)
					{
					divToDraw.append('text').text( ' where '+ response.filterColumn
							+' between ' + response.from + ' and ' + response.to);
					}
				//need to check this code
				/*if(response.stringFilter != null)
					{
					divToDraw.append('text').text( ' where '+ response.filterColumn + stringFilter); 
					}*/
				}
			
			// create svg element
			var chart = divToDraw
			              .append('svg') // parent svg element will contain
											// the chart
			              .attr('width', w)
			              .attr('height', h);
			
			var dataset= response.chartData;

			var chartPadding = 10;
			var chartBottom = h - (response.xWidth + 25);
			var chartRight = w - chartPadding;
			var maxValue = d3.max(dataset,function(d){ return d.yData; });
			var barLabels = dataset.map(function(datum){
			            return datum.xData;
			        });
			// adjust scale values to use padding
			var yScale = d3.scale
			               .linear()
			               .domain( [0,maxValue] )
			               .range( [chartBottom,chartPadding] )
			               .nice();
			               // adjusts scale to nice round values

			var xScale = d3.scale.ordinal()
			                     .domain( barLabels )
			                     .rangeRoundBands([(response.yWidth + 20), chartRight], 0.1); // 20 Extra for Axis label 
			                     // instead of .rangeRoundBands([0,w], 0.1)
			var yAxis = d3.svg.axis()
			                  .scale(yScale)
			                  .orient('left');

			// declare & configure the x axis
			var xAxis = d3.svg.axis()
			                  .scale(xScale)
			                  .orient('bottom')
			                  .tickSize(0);

			var showValue = function(d) {
			    chart.append('text')
			         .text(d.yData)
			         .attr({
			             'x': xScale(d.xData) + xScale.rangeBand() / 2,
			             'y': yScale(d.yData) + 15,
			             'class': 'value',
		            	 'font-family': 'sans-serif',
		                 'font-size': '13px',
		                 'font-weight': 'bold',
		                 'fill': 'white',
		                 'text-anchor': 'middle'
			         });
			};

			var hideValue = function() {
			    chart.select('text.value').remove();
			};
							  
			// create bars
			 chart.selectAll('rect')
			         .data(dataset)
			         .enter()
			         .append('rect')
			         .attr({
			             'x': function(d) {
			                 return xScale(d.xData); 
			                 // instead of return i * barwidth
			             },
			             'y': function(d) {
			                 return yScale(d.yData);
			                 // instead of return h - d.value
			             },
			             'width': xScale.rangeBand(), // gives bar width with
														// space calculation
														// built in
			                // instead of barwidth - spacing
			             'height': function(d) {
			                  return chartBottom - yScale(d.yData);
			                // instead of return d.value
			             },
			             'fill': 'steelblue'
			         })
					 .on('mouseover',function(d){
			    d3.select(this).attr('fill','brown');
			    showValue(d);
			})
			.on('mouseout',function(d){
			    d3.select(this)
			      .transition()  // adds a "smoothing" animation to the
									// transition
			      .duration(200) // set the duration of the transition in ms
									// (default: 250)
			      .attr('fill','steelblue');
			      hideValue();
			});

			   
			chart.append('g')
			     .attr('class', 'axis xAxis')
			     .attr('transform','translate(0,' + chartBottom + ')')  // push to bottom
			     .call(xAxis) // passes itself (g element) into xAxis function
			     .append("text")
					.attr("transform", "translate(" + fullWidth/2 + ",0)")
					.style("text-anchor", "center")
					.attr("dy", response.xWidth + 10)
					.text(response.xName);
			
			// use transformation to adjust position of axis 
			y_axis = chart.append('g')
			            .attr('class','axis')
			            .attr('transform','translate(' + (response.yWidth + 20) + ',0)') // 20 px extra for labels
			            .call(yAxis)
			            .append("text")
								.attr("transform", "translate(0, " + fullHeight + ")")
								.attr("transform", "rotate(-90)")
								.style("text-anchor", "center")
								.attr("dy", "-" + (response.yWidth) )
								.attr("dx", "-" + (fullHeight/2))
								.text(response.yName);

			// generate y Axis within group using yAxis function
			//yAxis(y_axis);
			
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

			chart.selectAll('g.xAxis g text').each(insertLinebreaks);

});
	
}

