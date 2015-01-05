function createPreview(target, chartType, data) {
	clearChart(target);

	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});

		var actualData = JSON.parse(data);
		console.log(actualData);		
		
		require([ "src/c3/Pie", "src/c3/Line", "src/c3/Column" ], function(
				C3Pie, C3Line, C3Column) {

			console.log(actualData);
			if (chartType == "PIE") {
				new C3Pie()
					.target(target)
					.data(actualData.data)
					.render();
			}

			if (chartType == "LINE") {
				new C3Line()
				.target(target)
				.data(actualData.data)
				.render();
			}

			if (chartType == "COLUMN") {
				new C3Column()
				.target(target)
				.data(actualData.data)
				.render();
			}

		});
	});
}

function clearChart(target){
	jq('#'+target).empty();
}
