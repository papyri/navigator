$(document).ready(function(){

          $("#controls input").click(
            function() {
              if (this.checked) {
                $("."+this.name).show();
                if (this.name == "transcription") {
                  $(".image").css('width','50%');
                  $(".translation").css('width','50%');
                }
              } else {
                $("."+this.name).hide();
                if (this.name == "transcription") {
                  $(".image").css('width','100%');
                  $(".translation").css('width','100%');
                }
              }
            }
          );
    	
    	  var showEditHistory = function(){
    	  
    	      	hideAllHistory();
    	  		$("#edit-history-list").show("blind", 250); 
    	  		$("#edit-history").unbind("click");
    	  		$("#edit-history").bind("click", hideEditHistory);
    	  
    	  }

		  var hideEditHistory = function(){
			
          		if(!($("#edit-history-list").css("display") == "none"))  {$("#edit-history-list").hide("blind", 250); }
    	  		$("#edit-history").unbind("click");
    	  		$("#edit-history").bind("click", showEditHistory);			
		   }

		  var showAllHistory = function(){

    	  		hideEditHistory();
    	  		$("#all-history-list").show("blind", 250); 
    	  		$("#all-history").unbind("click");
    	  		$("#all-history").bind("click", hideAllHistory);
		  
		  }
		  
		  var hideAllHistory = function(){
		  
    	  		if(!($("#all-history-list").css("display") == "none"))  { $("#all-history-list").hide("blind", 250); }
    	  		$("#all-history").unbind("click");
    	  		$("#all-history").bind("click", showAllHistory);		  
		  
		  }

          $("#edit-history").bind("click", showEditHistory);
          $("#all-history").bind("click", showAllHistory);

});
