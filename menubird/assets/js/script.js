/* Author:

*/

/*Page Transitions*/

/*Search*/
	$("#searchButton").click(function(){
	  $('#resultTitle').html('Analyzing...');
	  Android.take_pic();
	  window.location.hash = 'page2';
	  $("#resultsPage").animate({
	    left: "0%"
	  }, 200 );
	});
	
	$("#backButton").click(function(){
	  back();
	});

$(function(){
	
})

function back() {
	
	  $("#resultsPage").animate({
	    left: "100%"
	  }, 200 );

}


function setText(txt) {
	$('#resultTitle').html(txt);
}
