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

	var colors = d3.scale.linear()
		.domain([ min, median(arr), max ])
		.range([ "red", "yellow", "green" ]);

	jq.each(colorData, function(key, val) {
		colorData[key] = colors(val);
	});

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

		fills : {
			defaultFill : '#b2e5e5'
		},
		data : chartData.states
	});

	election.labels();

	election.updateChoropleth(colorData);
}
