function createGaugeChart(divId, reqData) {
	var chartData = jq.parseJSON(reqData);
	console.log(chartData);		
	
	var divElement = jq('$'+divId).empty();
	divElement.append(jq("<div id='chartHolder'/>" ));		
	var container = d3.select(divElement.get(0)).select("div");
	
	// size of the diagram
	var height = divElement.height();
	var width = divElement.width();
	
	//checking the minimum height for browser and component and set the same as window height. 
	if(divElement.parent().parent().height()<($(window).height()-150)){height = divElement.parent().parent().height();}
	else{height = ($(window).height()-150);}
	
	if(divElement.width()>$(window).width()){width = $(window).width();}
	
	var gauge = function(container, configuration) {
		var that = {};
		var config = {
			size						: 200,
			clipWidth					: 200,
			clipHeight					: 110,
			ringInset					: 20,
			ringWidth					: 20,
			
			pointerWidth				: 10,
			pointerTailLength			: 5,
			pointerHeadLengthPercent	: 0.9,
			
			minValue					: 0,
			maxValue					: 10,
			
			minAngle					: -90,
			maxAngle					: 90,
			
			transitionMs				: 750,
			
			majorTicks					: 150,
			labelFormat					: d3.format(',g'),
			labelInset					: 10,
			
			valueLabel					: 'Value',
			
			arcColorFn					: d3.interpolateHsl(d3.rgb('#ff4242'), d3.rgb('#42ff42'))
		};
		var range = undefined;
		var r = undefined;
		var pointerHeadLength = undefined;
		var value = 0;
		
		var svg = undefined;
		var arc = undefined;
		var scale = undefined;
		var ticks = undefined;
		var tickData = undefined;
		var pointer = undefined;
		var percentLabel = undefined;

		var donut = d3.layout.pie();
		
		function deg2rad(deg) {
			return deg * Math.PI / 180;
		}
		
		function newAngle(d) {
			var ratio = scale(d);
			var newAngle = config.minAngle + (ratio * range);
			return newAngle;
		}
		
		function configure(configuration) {
			var prop = undefined;
			for ( prop in configuration ) {
				config[prop] = configuration[prop];
			}
			
			range = config.maxAngle - config.minAngle;
			r = config.size / 2;
			pointerHeadLength = Math.round(r * config.pointerHeadLengthPercent);

			// a linear scale that maps domain values to a percent from 0..1
			scale = d3.scale.linear()
				.range([0,1])
				.domain([config.minValue, config.maxValue]);
				
			ticks = scale.ticks(config.majorTicks);
			tickData = d3.range(config.majorTicks).map(function() {return 1/config.majorTicks;});
			
			arc = d3.svg.arc()
				.innerRadius(r - config.ringWidth - config.ringInset)
				.outerRadius(r - config.ringInset)
				.startAngle(function(d, i) {
					var ratio = d * i;
					return deg2rad(config.minAngle + (ratio * range));
				})
				.endAngle(function(d, i) {
					var ratio = d * (i+1);
					return deg2rad(config.minAngle + (ratio * range));
				});
		}
		that.configure = configure;
		
		function centerTranslation() {
			return 'translate('+r +','+ r +')';
		}
		
		function dialCenterTranslation() {
			return 'translate('+r +','+ r*.7 +')';
		}
		
		function isRendered() {
			return (svg !== undefined);
		}
		that.isRendered = isRendered;
		
		function render(newValue) {
			svg = d3.select(container)
				.append('svg:svg')
					.attr('class', 'gauge')
					.attr('width', config.clipWidth)
					.attr('height', config.clipHeight);
			
			var centerTx = centerTranslation();
			
			var arcs = svg.append('g')
					.attr('class', 'arc')
					.attr('transform', centerTx);
			
			arcs.selectAll('path')
					.data(tickData)
				.enter().append('path')
					.attr('fill', function(d, i) {
						return config.arcColorFn(d * i);
					})
					.attr('d', arc);
			
			var lineData = [ [config.pointerWidth / 2, 0], 
							[0, -pointerHeadLength],
							[-(config.pointerWidth / 2), 0],
							[0, config.pointerTailLength],
							[config.pointerWidth / 2, 0] ];
			var pointerLine = d3.svg.line().interpolate('monotone');
			var pg = svg.append('g').data([lineData])
					.attr('class', 'pointer')
					.attr('transform', centerTx);
					
			pointer = pg.append('path')
				.attr('d', pointerLine/*function(d) { return pointerLine(d) +'Z';}*/ )
				.attr('transform', 'rotate(' +config.minAngle +')');
			
			percentLabel = svg.append('text')
				.attr('transform', dialCenterTranslation())
				.attr("text-anchor","middle")
				.text(newValue + '% ' + config.valueLabel);
			
			update(newValue === undefined ? 0 : newValue);
		}
		that.render = render;
		
		function update(newValue, newConfiguration) {
			if ( newConfiguration  !== undefined) {
				configure(newConfiguration);
			}
			var ratio = scale(newValue);
			var newAngle = config.minAngle + (ratio * range);
			pointer.transition()
				.duration(config.transitionMs)
				.ease('elastic')
				.attr('transform', 'rotate(' +newAngle +')');
			
			percentLabel.text(Math.round(newValue  * 100) / 100 + '% ' + config.valueLabel);
		}
		that.update = update;

		configure(configuration);
		
		return that;
	};
	
	
	var dropDown = container.append('div')
		.attr("class", "power-gauge-select")
		.append('select');
	
	dropDown.selectAll("option")
		.data(chartData.data)
		.enter()
		.append("option")
		.text(function(d){
			return d.name;
		})
		.attr("value", function(d){
			return d.percent;
		});
	
	dropDown.on("change", function() {
		powerGauge.update(d3.event.target.value);
	});
	
	container.append('div')
		.attr('id', 'power-gauge' + chartData.portletId)
		.attr('class', 'power-gauge');
		
	var powerGauge = gauge('#power-gauge' + chartData.portletId, {
		size: width > height*2 ? height*2 - 5 : width,
		clipWidth: width,
		clipHeight: height,
		ringWidth: 60,
		maxValue: 100,
		transitionMs: 4000,
		valueLabel: chartData.valueLabel
	});
	
	powerGauge.render();
	powerGauge.update(chartData.data[0].percent);
}