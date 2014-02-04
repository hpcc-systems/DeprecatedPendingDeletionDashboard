function createLineChart(divId, chartData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
		jq.getScript('js/lib/c3.js', function() {
			
			var response = jq.parseJSON(chartData);
			console.log(response);
			
			var datum = response.chartData;
			var divElement = jq('$'+divId).empty();
			divElement.append("text", response.title);
			
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			var divToDraw = '#' + divElement.attr("id");
			
			console.log("Height = " + fullHeight);
			console.log("Width = " + fullWidth);
			
			var isLargeGraph = false;
			if(response.xValues.length > 25){
				isLargeGraph = true;
			}
			
			var chart = c3.generate({
				data: {
					rows: response.yValues
				},
				bindto: divToDraw,
				size: {
					width:fullWidth,
					height:fullHeight - 15
				},
				axis: {
					y: {
						label: response.yName,
						tick: {
			                format: d3.format(".2s")
			            }
					},
					x: {
						type: 'categorized',
						categories: response.xValues,
						label: response.xName
					}
				},
				subchart: {
			        show: isLargeGraph
			    },
				zoom: {
			        enabled: false
			    }
			});
			
		});
	});
	
}