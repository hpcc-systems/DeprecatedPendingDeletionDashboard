function createPreview(target, chartType, data) {

	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});

		var actualData = JSON.parse(data);
		console.log(actualData);
		//actualData = [["Geography", 75, 68],["English", 45, 55],["Math", 98, 92],["Science", 66, 60]];
		
		require([ "src/c3/Pie", "src/c3/Line", "src/c3/Column" ], function(
				C3Pie, C3Line, C3Column) {

			console.log(actualData);
			if (chartType == "pie") {
				new C3Pie()
					.target(target)
					.data(actualData.data)
					.render();
			}

			if (chartType == "line") {
				new C3Line().target(target).data(actualData.data).render();
			}

			if (chartType == "column") {
				new C3Column().target(target).data(actualData.data).render();
			}

		});
	});
}
