$(document).ready(

	function(){
			
		var hic = {};
		hic.reqd_on = {};
		hic.reqd_off = {};
		

		hic.reqd_on["lemmas"] = ["#caps", "#marks", "#target-text"];
		hic.reqd_off["lemmas"] = ["#target-metadata", "#target-translations", "#target-all"];
		hic.reqd_off["target-metadata"] = ["#betaYes"];
		hic.reqd_off["target-translations"] = ["#betaYes"];
	    
	    hic.configureSearchSettings = function(){
	    
	    	var reqd_disabled = [];

	    	var eltype = $(this).attr("name");
	    
	    	if(eltype == "type"){
	    		
	    		var target = $("#text-search-widget").find("input[name='target']:checked").attr("id");
	    		if(hic.reqd_on[target]) jQuery.merge(reqd_disabled, hic.reqd_on[target]); 
	    		if(hic.reqd_off[target]) jQuery.merge(reqd_disabled, hic.reqd_off[target]); 
	    	
	    	}
	    	else if(eltype == "target"){
	    	
	    		var type = $("#text-search-widget").find("input[name='type']:checked").attr("id");
	    		if(hic.reqd_on[type]) jQuery.merge(reqd_disabled, hic.reqd_on[type]);
	    		if(hic.reqd_off[type]) jQuery.merge(reqd_disabled, hic.reqd_off[type]);
	    		
	    	}

	    	var disableds = $("#text-search-widget input:disabled");
	    	
	    	for(var i = 0; i < disableds.length; i++){

	    		var dis = disableds[i];
	    		
	    		var disid = $(dis).attr("id");
	    		disid = "#" + disid;
	    		
	    		var foundInArray = false;
	    		
	    		for(var j = 0; j < reqd_disabled.length; j++){

	    			if(disid == reqd_disabled[j]) foundInArray = true;
	    		
	    		}
	    		
	    		if(!foundInArray) $(disid).removeAttr("disabled"); 
	    	
	    	}
	    
			var id = $(this).attr("id");
			
			var onanda = hic.reqd_on[id];
			
			if(onanda){
			
				for(var i = 0; i < onanda.length; i++){
				
					$(onanda[i]).attr("checked", "checked");
					$(onanda[i]).attr("disabled", "disabled");
				
				}
		
			}
	    	
	    	var offanda = hic.reqd_off[id];
	    	
	    	if(offanda){
	    	
	    		for(var i = 0; i < offanda.length; i++){
	    		
	    			$(offanda[i]).removeAttr("checked");
	    			$(offanda[i]).attr("disabled", "disabled");
	    		
	    		}
	    	
	    	
	    	}
	    }
	
		$("#text-search-widget").find("input[name='target']").click(hic.configureSearchSettings);
		$("#text-search-widget").find("input[name='type']").click(hic.configureSearchSettings);
		
	}

);