function visualize(target, chartType, data) {

	require([ "js/visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "js/visualization/widgets"
		});

		var actualData = JSON.parse(data);
		require([ "src/c3/Pie", "src/c3/Line", "src/c3/Column" ], function(
				C3Pie, C3Line, C3Column) {

			console.log(actualData);
			if (chartType == "pie") {
				new C3Pie().target(target).data(actualData).render();
			}

			if (chartType == "line") {
				new C3Line().target(target).data(actualData).render();
			}

			if (chartType == "column") {
				new C3Column().target(target).data(actualData).render();
			}

		});
	});
}
