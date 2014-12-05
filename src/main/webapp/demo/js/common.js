function clearChart(divId) 
{
	jq('$'+divId).empty();	
}

function showCreationError(divId) {
	jq('$'+divId).append("Error occured while creating this chart");
}