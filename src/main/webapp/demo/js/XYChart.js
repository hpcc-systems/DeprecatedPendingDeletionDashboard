function createXYChart (divId, chartData) {
	var response = jq.parseJSON(chartData);
	console.log("response -->"+response.hideY2Axis);
	console.log("response -->"+response.enabledY2Axis);
	console.log("chartData -->"+chartData)
	var divElement = jq('$'+divId).empty();
	
	var showLegend = false;
	if(Object.keys(response.chartTypes).length > 1) {
		showLegend = true;
	} 
	
	var rotateAxis = false;
	if(response.rotateAxis){
		rotateAxis = true;
	}
	var yColumnMargin = 20;
	if(showLegend){yColumnMargin = 60}
	
	var container = jq("#" + divElement.attr("id"));
	
	var filter_desc = "";
	if(response.isFiltered){
		filter_desc = "<span class='btn-link btn-sm' style='float: right; padding: 0px 10px;' id='"+ response.portletId +"_title'> Filters </span>" +
				"<div id='"+ response.portletId +"_filter_content' style='line-height: initial;position: absolute;padding: 2px;border: 1px solid rgb(124, 124, 124);margin: 5px;background-color: rgb(177, 177, 177);font-size: small;color: white;z-index: 2; display: none'>"+ response.filterDescription +"</div>" +
				"<script type='text/javascript'>" +
					"jq('#"+ response.portletId +"_title').mouseenter(function() {" +
							"jq('#"+ response.portletId +"_filter_content').show();" +
									"})" +
					".mouseleave(function() { " +
							"jq('#"+ response.portletId +"_filter_content').hide();});" +
				"</script>";
	}
	
	container.append(jq("<div style='margin-top: 3px; margin-left: 5px; height: 15px;'>"+ filter_desc + " </div>" ));
	
	container.append(
				jq( "<div id='"+ response.portletId + "holderDiv" +"'> Rendering chart... </div>" )
			);
	
	var fullHeight = divElement.height();
	var fullWidth = divElement.width();
	
	//checking the minimum height for browser and component and set the same as window height. 
	if(divElement.parent().parent().height()<($(window).height()-150)){fullHeight = divElement.parent().parent().height();}
	else{fullHeight = ($(window).height()-150);}
	
	if(divElement.width()>$(window).width()){fullWidth = $(window).width();}
	
	var isLargeGraph = false;
	if(response.xCategoryLabels.length > 25){
		isLargeGraph = true;
	}
	
	var xAxisType = 'categorized';
	var timeFormat = null, timeColumnName = null, xAxisDisplayFormat = null,yMin=null,yMax=null;y2Max=null;y2Min=null;
	var yThresholdMin = null;
	var yThresholdMax = null;
	var y2ThresholdMin = null;
	var y2ThresholdMax = null;
	var showY2Axis = false;
	
	//Hides secondary axis, if secondary axis is enabled but didn't drop any secondary column
	//Shows the secondary axis if secondary axis is enabled && didn't hide the axis
	if(response.enabledY2Axis && !response.hideY2Axis && response.secondaryYAxisLabel.length){
		showY2Axis = true;
	}
	
	
	if(response.timeseries.isEnabled) {
		xAxisType = 'timeseries';
		timeFormat = response.timeseries.format;
		timeColumnName = response.xAxisLabel;
		xAxisDisplayFormat = response.timeseries.displayFormat;
	}
	
	if(response.yMin) {
		yMin=response.yMin;
	}
	if(response.yMax) {
		yMax=response.yMax;
	}
	if(response.y2Min){
		y2Min = response.y2Min;
	}
	if(response.y2Max) {
		y2Max=response.y2Max;
	}
	if(response.yThresholdMin){
		yThresholdMin = response.yThresholdMin;
	}
	if(response.yThresholdMax){
		yThresholdMax = response.yThresholdMax;
	}
	if(response.y2ThresholdMin){
		y2ThresholdMin = response.y2ThresholdMin;
	}
	if(response.y2ThresholdMax){
		y2ThresholdMax = response.y2ThresholdMax;
	}

	c3JSON = {
			data: {
				x: timeColumnName,
			    xFormat : timeFormat,
				rows: response.dataRows,	
				types: response.chartTypes,
				axes:response.axes				
			},
			bindto: "#" + response.portletId + "holderDiv",
			size: { 
				width:fullWidth - 5,
				height:fullHeight
			},
			bar:{
				width:{
	            ratio: 0.5 // this makes bar width 50% of length between ticks
			    },
			    zerobased:false
			},
			axis: {
				y: {
					min: yMin,
					max: yMax,
		            padding: {top:0, bottom:0},
					tick: {
		                format: d3.format(",.2r")
		            },
		            label: {
		                text: response.primaryYAxisLabel,
		                position: 'outer-middle'
		            }
				},
				x: {
					type: xAxisType,
					categories: response.xCategoryLabels,
					tick: {
						format: xAxisDisplayFormat
			        },
					label: {
		                text: response.xAxisLabel,
		                position: 'outer-center'
		            }
				},
				y2: {
					min: y2Min,
					max: y2Max,
		            padding: {top:0, bottom:0},
					tick: {
		                format: d3.format(",.2r")
		            },
		            label: {
		                text: response.secondaryYAxisLabel		                
		            },
		            show: showY2Axis		            
		        },		       
		       rotated: rotateAxis
			},
			
			tooltip: {
		        format: {
		           // value: d3.format('f')
		        }
		    },
			legend: {
		        show: showLegend,
		        equally: true
		    },
			subchart: {
		        show: isLargeGraph
		    },
			zoom: {
		        enabled: false
		    }
		};
	
	console.log(JSON.stringify(c3JSON));
	
	var chart = c3.generate(c3JSON);
	
	if(yThresholdMin && !yThresholdMax && !y2ThresholdMin && !y2ThresholdMax){
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'}]);
   
	}else if(yThresholdMin && yThresholdMax && !y2ThresholdMin && !y2ThresholdMax){
    	
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'},
		                  {value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'}]);
    
	}else if(yThresholdMin && !yThresholdMax && y2ThresholdMin && !y2ThresholdMax){
	
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'},
	                  {value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'}]);

	}else if(yThresholdMin && !yThresholdMax && !y2ThresholdMin && y2ThresholdMax){
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'},
		                  {value: y2ThresholdMax, text: y2ThresholdMax+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmax'}]);
		
	}else if(yThresholdMin && yThresholdMax && y2ThresholdMin && !y2ThresholdMax){
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'},
		                  {value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'},
		                  {value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'}]);

	}else if(yThresholdMin && !yThresholdMax && y2ThresholdMin && y2ThresholdMax){		
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'},
		                  {value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'},
		                  {value: y2ThresholdMax, text: y2ThresholdMax+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmax'}]); 
		
	}else if(yThresholdMin && yThresholdMax && y2ThresholdMin && y2ThresholdMax){		
		chart.ygrids.add([{value: yThresholdMin, text: yThresholdMin+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmin'},
		                  {value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'},
		                  {value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'},
		                  {value: y2ThresholdMax, text: y2ThresholdMax+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmax'}]); 

	}else if(!yThresholdMin && yThresholdMax && !y2ThresholdMin && !y2ThresholdMax){		
		chart.ygrids.add([{value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'}]); 	
		
	}else if(!yThresholdMin && yThresholdMax && y2ThresholdMin && !y2ThresholdMax){		
		chart.ygrids.add([{value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'},
		                  {value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'}]); 
		
	}else if(!yThresholdMin && yThresholdMax && !y2ThresholdMin && y2ThresholdMax){		
		chart.ygrids.add([{value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'},
		                  {value: y2ThresholdMax, text: y2ThresholdMax+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmax'}]); 
		
	}else if(!yThresholdMin && yThresholdMax && y2ThresholdMin && y2ThresholdMax){		
		chart.ygrids.add([{value: yThresholdMax, text: yThresholdMax+' - '+response.primaryYAxisLabel,position: 'start',class: 'gridmax'},
		                  {value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'},
		                  {value: y2ThresholdMax, text: y2ThresholdMax+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmax'}]); 
		
	}else if(!yThresholdMin && !yThresholdMax && y2ThresholdMin && !y2ThresholdMax){		
		chart.ygrids.add([{value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'}]);
		
	}else if(!yThresholdMin && !yThresholdMax && y2ThresholdMin && y2ThresholdMax){		
		chart.ygrids.add([{value: y2ThresholdMin, text: y2ThresholdMin+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmin'},
		                  {value: y2ThresholdMax, text: y2ThresholdMax+' - '+response.secondaryYAxisLabel,axis: 'y2',position: 'end',class: 'gridmax'}]); 
	}

}
