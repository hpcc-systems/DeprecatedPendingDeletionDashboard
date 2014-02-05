function clearChart(divId)
{
	jq('$'+divId).empty();	
}

function createChart(divId, chartData) {
	jq.getScript('js/lib/d3.v3.min.js', function() {
		jq.getScript('js/lib/c3.js', function() {
			
			var response = jq.parseJSON(chartData);
			console.log(response);
			
			var datum = response.chartData;
			var divElement = jq('$'+divId).empty();
			
			jq("#" + divElement.attr("id"))
				.append(
						jq("<div style='margin-left: 3px; height: 15px;'>" + response.title +" </div>" ), 
						jq( "<div id='"+ response.portletId + "holderDiv" +"'/>" 
						)
				);
			
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			var showLegend = false;
			if(Object.keys(response.yNames).length > 1) 
				showLegend = true;
			
			console.log("Height = " + fullHeight);
			console.log("Width = " + fullWidth);
			console.log("Legend = " + showLegend);
			 
			var isLargeGraph = false;
			if(response.xValues.length > 25){
				isLargeGraph = true;
			}
			
			var chart = c3.generate({
				data: {
					rows: response.yValues,
					types: response.yNames
				},
				bindto: "#" + response.portletId + "holderDiv",
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
				legend: {
			        show: showLegend
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