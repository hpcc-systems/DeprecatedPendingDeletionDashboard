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
			
			var showLegend = false;
			if(Object.keys(response.yNames).length > 1) {
				showLegend = true;
			} 
			
			var yColumnMargin = 20;
			if(showLegend){yColumnMargin = 60}
			
			var container = jq("#" + divElement.attr("id"));
			
			var filter_desc = "";
			if(response.isFiltered){
				filter_desc = "<span class='btn-link btn-sm' style='float: right;' id='"+ response.portletId +"_title'> Filters </span>" +
						"<div id='"+ response.portletId +"_filter_content' style='line-height: initial;position: absolute;padding: 2px;border: 1px solid rgb(124, 124, 124);margin: 5px;background-color: rgb(177, 177, 177);font-size: small;color: white;z-index: 2; display: none'>"+ response.filterDescription +"</div>" +
						"<script type='text/javascript'>" +
							"jq('#"+ response.portletId +"_title').mouseenter(function() {" +
									"jq('#"+ response.portletId +"_filter_content').show();" +
											"})" +
							".mouseleave(function() { " +
									"jq('#"+ response.portletId +"_filter_content').hide();});" +
						"</script>";
			}
			
			container.append(jq("<div style='margin-top: 3px; margin-left: 5px; height: 15px;'>"+ response.title + filter_desc + " </div>" ));
			
			container.append(
						jq( "<table> <tr>" +
								"<td> " +
									"<div style='margin-top:" + yColumnMargin +"px; margin-left: 3px; font-size: 10px; width: 15px; height: 15px; writing-mode:tb-rl; -webkit-transform:rotate(-90deg); -moz-transform:rotate(-90deg); -o-transform: rotate(-90deg); white-space:nowrap; display:block;'>" +
										response.yName +
									"</div> </td>" +
								"<td> <div id='"+ response.portletId + "holderDiv" +"'/> </td>" +
							"</tr> </table>"),
						jq("<div style='margin-left: 50px; height: 15px; font-size:10px; text-align: center;'>" + response.xName +" </div>" )
					);
			
			var fullHeight = divElement.height();
			var fullWidth = divElement.width();
			
			if(fullWidth < 50 ){ fullWidth = 400; }
			if(fullHeight < 50 ){ fullHeight = 385; }
			
			var legendWidth = fullWidth/Object.keys(response.yNames).length;
			
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
					width:fullWidth - 15 - 5,
					height:fullHeight - 15 - 20
				},
				axis: {
					y: {
						tick: {
			                format: d3.format(".2s")
			            }
					},
					x: {
						type: 'categorized',
						categories: response.xValues
					}
				},
				legend: {
			        show: showLegend,
			        item: {
			        	width: legendWidth
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