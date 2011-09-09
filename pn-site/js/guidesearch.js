$(document).ready(

	/**
	* Functions aimed at tidying the search UI functionality
	*
	*/

	function(){
			
		$("#id-volume").autocomplete({
			
			source: $("#volume-autocomplete").text().split(' ').sort(function(a,b){return a - b;})
			
		});
				
		
		$("#id-idno").autocomplete({
		
			source: $("#idno-autocomplete").text().split(' ')
		
		});
			
		// first, a little namespacing
		
		if(typeof info == 'undefined') info = {};
		if(typeof info.papyri == 'undefined') info.papyri = {};
		if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
		if(typeof info.papyri.thill.guidesearch == 'undefined') info.papyri.thill.guidesearch = {};
			
		// alias to save typing	
		var hic = info.papyri.thill.guidesearch;
		hic.reqd_on = {};
		hic.reqd_off = {};
		
		// 'Search Type' is really a proxy for setting the fields to be searched
		// and the string transformations to use in search. Certain combinations
		// of search target and string config are thus forbidden.
		
		// for reqd_on and reqd_off members
		// keys are name of element clicked
		// values are list of elements that *must* be 
		// switched on or off onclick.

		hic.reqd_on["lemmas"] = ["#caps", "#marks", "#target-text"];
		hic.reqd_off["lemmas"] = ["#target-metadata", "#target-translations", "#target-all"];
		hic.reqd_off["target-metadata"] = ["#betaYes"];
		hic.reqd_off["target-translations"] = ["#betaYes"];
		hic.reqd_on["target-all"] = ["#caps", "#marks"];
		hic.reqd_off["lem"] = hic.reqd_off["lemmas"];
	    
	    hic.configureSearchSettings = function(){
	    
	    	var reqd_disabled = [];
	    	
	    	// step one: re-enable disabled selectors as required

	    	var eltype = $(this).attr("name");
	    	
	    	// these two conditionals build an array (reqd_disabled) of all elements that
	    	// *must* be disabled given the new setting
	    	
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
	    	
	    	// then enable all currently disabled elements not found in the
	    	// reqd_disabled array

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
	    
	    	// now, check and/or disabled all reqd elements
	    
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
	    
	    /**
	    * Without javascript, the form automatically sends values for every form field to the server -
	    * including those wtih a null or default value, leading to very long and illegible querystrings.
	    * This method strips out all default/null submitted values before passing them on to
	    * the server.
	    */
	    
	    
	    hic.tidyQueryString = function(){
	    
	    	var querystring = "";
	    	var filteredels = [];
	    	
	    	// if a string is set for search, than the associated text, target, and option
	    	// fields must also be set.
	    	
	    	var textel = $("input[name='STRING']");
	    	if(!textel.attr("value").match(/^\s*$/)){

	    		filteredels.push(textel);
	    		var type = $("input[name='type']").filter(":checked");
	    		filteredels.push(type);
				filteredels.push($("input[name='target']").filter(":checked"));	
    		
	    		var betas = $("#betaYes:checked");
	    		if(betas.length > 0) filteredels.push(betas);
	    		
	    		var caps = $("#caps:checked");
	    		if(caps.length > 0) filteredels.push(caps);
	    		
	    		var marks = $("#marks:checked");
	    		if(marks.length > 0) filteredels.push(marks);
	    		
	    		if(type.val() == "proximity"){
	    		
	    			filteredels.push($("input[name='within']"));
	    		
	    		}
	    	
	    	}		
	    	
	    	var internals = $("input:checkbox[name='INT']:checked");
	    	if(internals.length > 0) filteredels.push(internals);
	    	var externals = $("input:checkbox[name='EXT']:checked");
			if(externals.length > 0) filteredels.push(externals);
			var printpubs = $("input:checkbox[name='PRINT']:checked");
			if(printpubs.length > 0) filteredels.push(printpubs);
			
			var vol = $("#id-volume");
			var volno = vol.val();
			if(volno != "" && volno != "n.a.") filteredels.push(vol);
			
			var ident = $("#id-idno");
			var identno = ident.val();
			if(identno != "" && identno != "n.a.") filteredels.push(ident);
	    	
	    	var opts = $("select");

	    	for(var i = 0; i < opts.length; i++){
	    	
	    		var opt = $(opts[i]);
	    		if(opt.attr("value") != "default" && !opt.attr("disabled")) filteredels.push(opt);
	    	
	    	}
	    	
	    	var hiddens = document.getElementsByTagName("input");
	    	
	    	for(var j = 0; j < hiddens.length; j++){
	    	
	    		var hidden = hiddens[j];
	    		var htype = hidden.getAttribute("type");
	    		// note weirdness here - jQuery cannot retrieve attributes from hidden input fields
	    		// standard js .getAttribute is thus used
	    		if(htype == "hidden") filteredels.push(hidden);
	    	
	    	}
			
			for(var k = 0; k < filteredels.length; k++){
			
				var fel = filteredels[k];
				var name = $(fel).attr("name");
				var val = $(fel).attr("value");
				// workaround for jQuery hidden field blindness
				if(typeof name == 'undefined' || typeof val == 'undefined'){
				
					name = fel.getAttribute("name");
					val = fel.getAttribute("value");
				}
				querystring += name + "=" + val;
				
				if(k < filteredels.length - 1) querystring += "&";
				
			}

			var current = window.location;
			
			if(current.toString().match(/\?/)) {
			
				var currentbits = current.toString().split("?");
				current = currentbits[0];
			
			}
			
			var hrefwquery = current + "?" + querystring;
			window.location = hrefwquery;
			return false;

	    }
	    
	    // this is working just fine at capturing text input
	    // and triggering when needed. just a question of working
	    // out what behaviours are desired
	    
	    hic.monitorTextInput = function(){
	    
	    	$(this).unbind('focus');
			
			$(this).keyup(function(event){
			
				event.stopPropagation();
				var val = $(this).val();
				if(val.match("lem:")) {
				
					// do some stuff here
				
				}
				
			
			});
	    
	    }
	    
	    hic.hideSearch = function(evt){

			var currentValsWrapperLeft = $("#vals-and-records-wrapper").position().left;
			var initialHeight = $("#facet-wrapper").height();
			var initialWidgetHeight = $("#facet-widgets-wrapper").height();
			var valsWrapperMinWidth = 800;
			var valsWrapperPixelWidth = 0.95 * $("#facet-wrapper").width();
	    	var newValsWidth = valsWrapperPixelWidth < valsWrapperMinWidth ? valsWrapperMinWidth : valsWrapperPixelWidth;
	    	$("#facet-widgets-wrapper").animate({ left: -($("#facet-widgets-wrapper").width() + 23) }, 325);
	    	$("#vals-and-records-wrapper").css({"position":"absolute", "left": currentValsWrapperLeft });
	    	$("#vals-and-records-wrapper").animate({ left: 23, width: newValsWidth }, 325, "swing",
	    		
	    		function(){
	    			
	    			$("#facet-wrapper").height(initialHeight);
	    			$("#facet-widgets-wrapper").addClass("search-closed");
	    			$("#facet-widgets-wrapper").offset({ left:-23 });
	    			$("#facet-widgets-wrapper").removeClass("search-open");
	    			$("#search-toggle").addClass("toggle-closed");
	    			$("#search-toggle").removeClass("toggle-open");
	    			$("#vals-and-records-wrapper").removeClass("vals-and-records-min");
	    			$("#vals-and-records-wrapper").addClass("vals-and-records-max");
	    			$("#vals-and-records-wrapper").css({"position":"absolute" });
	    			var height = initialWidgetHeight > $("#vals-and-records-wrapper").height() ? initialWidgetHeight : $("#vals-and-records-wrapper").height();
	    			$("#search-toggle-pointer").text(">>");
	    			$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });
	    			hic.positionTogglePointer();


	    		}
	    	
	    	
	    	);
	    	$("#search-toggle").unbind('click');
	    	$("#search-toggle").click(hic.showSearch);
	    
	    }
	
		hic.showSearch = function(evt){
		
			var widgetWrapperMinWidth = 600;
			var widgetWrapperPixelWidth = 0.33 * $("#facet-wrapper").width();
			var newWidgetWidthVal = widgetWrapperPixelWidth < widgetWrapperMinWidth ? widgetWrapperMinWidth : widgetWrapperPixelWidth;
			newWidgetWidthVal += 23;
			newWidgetWidthVal = newWidgetWidthVal;
			$("#vals-and-records-wrapper").css("position", "absolute");
			$("#facet-widgets-wrapper").removeClass("search-closed");
			$("#facet-widgets-wrapper").css("left", "-" + newWidgetWidthVal + "px");
	    	$("#facet-widgets-wrapper").addClass("search-open");

			var valsWrapperMinWidth = 800;
			var valsWrapperPixelWidth = 0.6 * $("#facet-wrapper").width();
			var newValsWidth = valsWrapperPixelWidth < valsWrapperMinWidth ? valsWrapperMinWidth : valsWrapperPixelWidth;
			$("#facet-widgets-wrapper").animate({ left: 0 }, 325);
			$("#vals-and-records-wrapper").animate({ left: newWidgetWidthVal + 23, width: newValsWidth }, 325, 
			
				function(){
				
					$("#vals-and-records-wrapper").css({ "position" : "relative", "left" : 23 });
					$("#search-toggle").height($("#facet-wrapper").height());
	    			$("#search-toggle").removeClass("toggle-closed");
	    			$("#search-toggle").addClass("toggle-open");
	    			$("#vals-and-records-wrapper").addClass(".vals-and-records-min");
	    			$("#vals-and-records-wrapper").removeClass("vals-and-records-max");	
	    			$("#search-toggle-pointer").text("<<");
	    			hic.positionTogglePointer();

				}
			
			);
			$("#search-toggle").unbind('click');
			$("#search-toggle").click(hic.hideSearch);
		
		}
		
		hic.positionTogglePointer = function(){
		
			$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });
		
		}
		
		
		hic.positionTogglePointer();
		$("#text-search-widget").find("input[name='target']").click(hic.configureSearchSettings);
		$("#text-search-widget").find("input[name='type']").click(hic.configureSearchSettings);
		$("form[name='facets']").submit(hic.tidyQueryString);
		$("form select").change(hic.tidyQueryString);
		$("#search-toggle").height($("#facet-wrapper").height());
		$(".toggle-open").click(hic.hideSearch);
		$(".toggle-closed").click(hic.showSearch);
		
		//$("#keyword").focus(hic.monitorTextInput);
		//$("#keyword").blur(function(){ $("#keyword").focus(hic.monitorTextInput) });
	}

);