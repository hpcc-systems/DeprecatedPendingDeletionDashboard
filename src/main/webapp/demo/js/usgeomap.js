
function createGeoChart(divId, reqData) {
	var chartData = jq.parseJSON(reqData);
	console.log(chartData);

	var uniqueId = "holder" + chartData.portletId;
	
	var divElement = jq('$' + divId).empty();
	divElement.append(jq("<div id='" + uniqueId + "'/>"));

	// size of the diagram
	
	var fullHeight = divElement.height();
	var fullWidth = divElement.width();
	
	//checking the minimum height for browser and component and set the same as window height. 
	if(divElement.parent().parent().height()<($(window).height()-150)){fullHeight = divElement.parent().parent().height();}
	else{fullHeight = ($(window).height()-150);}
	
	if(divElement.width()>$(window).width()){fullWidth = $(window).width();}
	
	if (fullWidth > 1.7 * fullHeight) {
		fullWidth = fullHeight * 1.7;
	}
	
	var colorData = jq.extend(true, {}, chartData.states);
	var arr = Object.keys(colorData).map(function(key) {
		return colorData[key];
	});
	var min = Math.min.apply(null, arr);
	var max = Math.max.apply(null, arr);
	var median = function(values) {

		values.sort(function(a, b) {
			return a - b;
		});

		var half = Math.floor(values.length / 2);

		if (values.length % 2)
			return values[half];
		else
			return (values[half - 1] + values[half]) / 2.0;
	}

	
	var initialScaleData = arr;
	var newScaledData = [];
	var groupCount = Math.ceil((max-min)/10);
	var loopVal = groupCount;
	console.log("groupCount --->"+groupCount);
	
	for(var i=0 ; i<10 ; i++){		
		newScaledData[i] = loopVal;
		loopVal = loopVal+groupCount;
	}
	console.log("newScaledData --->"+newScaledData);

	 var color_domain = newScaledData;
		 
	 var colors = d3.scale.threshold()
	  .domain(color_domain)
	  .range(["#a5b5cb","#7890b1","#627ea4","#4b6b97","#35598a","#1f477e", "#1f477e","#183864","#122a4b","#0c1c32"]);
	 

	var labelData = {
			defaultFill : '#e8ecf2'
		};
	 for(var i=0 ; i<newScaledData.length-1 ; i++){	
		 if(i == 0){
			 labelData["<" + newScaledData[i]]=colors(0);
			 labelData[newScaledData[i] + "+"]=colors(newScaledData[i]);
		 }else{
			 labelData[newScaledData[i] + "+"]=colors(newScaledData[i]);
		 }	
		
	 }
	 labelData[">"+newScaledData[newScaledData.length-1]]=colors(newScaledData[newScaledData.length-1]);
	 console.log("labelData --->",labelData);	 
	
	var arr = Object.keys(colorData).map(function(key) {
		return colorData[key];
	});
	jq.each( colorData, function(key,value){
		console.log(colors(value));
		colorData[key] = colors(value);
		});
	console.log("colorData --->",colorData);
	
	var legendData = {
			defaultFillName: "No data",
	}
	
	var election = new Datamap({
		height : fullHeight,
		width : fullWidth,
		scope : 'usa',
		element : document.getElementById(uniqueId),

		geographyConfig : {
			highlightBorderColor : '#bada55',
			popupTemplate : function(geography, data) {
				return '<div class="hoverinfo">' + geography.properties.name
						+ '<br>'+ chartData.primaryYAxisLabel +' : ' + data + '</div>';
			},
			highlightBorderWidth : 3
		},
		fills : labelData,

		data : chartData.states
	});

	election.labels();
	election.legend({
		    defaultFillName: "No data"
		  });
	election.legend(legendData);

	election.updateChoropleth(colorData);
}
