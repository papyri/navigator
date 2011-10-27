$(document).ready(

	/**
	* Various UI-related functions
	*
	*/

	function(){
					
		// first, a little namespacing
		
		if(typeof info == 'undefined') info = {};
		if(typeof info.papyri == 'undefined') info.papyri = {};
		if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
		if(typeof info.papyri.thill.guidesearch == 'undefined') info.papyri.thill.guidesearch = {};
			
		// alias to save typing	
		var hic = info.papyri.thill.guidesearch;
		hic.reqd_on = {};
		hic.reqd_off = {};
		hic.selectedRadios = [];
		
		// 'Search Type' is really a proxy for setting the fields to be searched
		// and the string transformations to use in search. Certain combinations
		// of search target and string config are thus forbidden.
		
		// for reqd_on and reqd_off members
		// keys are name of element clicked
		// values are list of elements that *must* be 
		// switched on or off onclick.

        	hic.reqd_off["substring"] = ["#target-metadata", "#target-translations", "#target-all"];
		hic.reqd_on["lemmas"] = ["#caps", "#marks", "#target-text"];
		hic.reqd_off["lemmas"] = ["#target-metadata", "#target-translations", "#target-all"];
		hic.reqd_off["target-metadata"] = ["#betaYes"];
		hic.reqd_off["target-translations"] = ["#betaYes"];
		hic.reqd_on["target-all"] = ["#phrase", "#caps", "#marks"];
		hic.reqd_off["lem"] = hic.reqd_off["lemmas"];
		
		/**
		 * Restricts user options so that only possible string-search configurations
		 * can be set
		 * TODO: needs to be revised in wake of poor user response!
		 */
		 
	    
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
	    			//$(offanda[i]).attr("disabled", "disabled");
	    		
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
	    	var mixedsearch = false;
	    	
	    	// if a string is set for search, than the associated text, target, and option
	    	// fields must also be set.
	    	
	    	var textel = $("input[name='STRING']");
	    	if(!textel.attr("value").match(/^\s*$/)){

				if(textel.attr("value").indexOf(":") != -1) mixedsearch = true;
				
                filteredels.push(textel);
	    		var betas = $("#betaYes:checked");
	    		if(betas.length > 0) filteredels.push(betas);
	    		
	    		var caps = $("#caps:checked");
	    		if(caps.length > 0) filteredels.push(caps);
	    		
	    		var marks = $("#marks:checked");
	    		if(marks.length > 0) filteredels.push(marks);

                if(!mixedsearch){
                
	    		     var type = $("input[name='type']").filter(":checked");
	    		     filteredels.push(type);
				     filteredels.push($("input[name='target']").filter(":checked"));	
	    		
	    		     if(type.val() == "proximity"){
	    		
	    			    filteredels.push($("input[name='within']"));
	    		
	    		     }
	    		
	    		}
	    	
	    	}		
	    	// image filter elements
	    	var internals = $("input:checkbox[name='INT']:checked");
	    	if(internals.length > 0) filteredels.push(internals);
	    	var externals = $("input:checkbox[name='EXT']:checked");
			if(externals.length > 0) filteredels.push(externals);
			var printpubs = $("input:checkbox[name='PRINT']:checked");
			if(printpubs.length > 0) filteredels.push(printpubs);
			
			// date mode selector
			var datemode = $("input:radio[name='DATE_MODE']:checked");
			if(datemode.length > 0) filteredels.push(datemode);
			
			var vol = $("#id-volume");
			var volno = vol.val();
			if(volno != "" && volno != "n.a.") filteredels.push(vol);
			
			var ident = $("#id-idno");
			var identno = ident.val();
			if(identno != "" && identno != "n.a.") filteredels.push(ident);
	
	    	var datestart = $("#DATE_START_TEXT");
	    	var startval = datestart.val();
	    	if(startval != "") filteredels.push(datestart);
	    	
	    	var dateend = $("#DATE_END_TEXT");
	    	var endval = dateend.val();
	    	if(endval != "") filteredels.push(dateend);
	    	
	    	// note that there are two separate means of entering dates: 
	    	// using a drop-down selector, or via text input
	    	// to avoid the need to disambiguate these and decide issues
	    	// of priority, the selectors (DATE_START and DATE_END controls)
	    	// never submit their values directly to the server
	    	// instead their values are copied into the relevant text
	    	// input fields (DATE_START_TEXT and DATE_END_TEXT), which
	    	// *are* submitted
	    	
	    	var opts = $("select").not("[name='DATE_START']").not("[name='DATE_END']");

	    	for(var i = 0; i < opts.length; i++){
	    	
	    		var opt = $(opts[i]);
	    		
	    		// Era selection should be submitted only if there is a numerical
	    		// year value to go with it
	    		if(opt.attr("name") == "DATE_START_ERA" || opt.attr("name") == "DATE_END_ERA"){
	    		
	    			var prefix = opt.attr("name").substring(0, opt.attr("name").length - 4);
	    			var correlatedText = prefix + "_TEXT";
	    			var correlatedValue = $("input[name='" + correlatedText + "']").val();
	    			if(correlatedValue != "" && correlatedValue != "n.a.") filteredels.push(opt);
	
	    		}
	    		else if(opt.attr("value") != "default" && !opt.attr("disabled")){
	    		
	    			filteredels.push(opt);
	    			
	    		}

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
				val = val.replace(/#/g, "^");
				querystring += name + "=" + val;
				
				if(k < filteredels.length - 1) querystring += "&";
				
			}

            var current = window.location;

            if(current.toString().match(/\?/)) {
			
				var currentbits = current.toString().split("?");
				current = currentbits[0];
			
			}
            if(mixedsearch) querystring += "&type=user_defined&target=user_defined"			
			var hrefwquery = current + "?" + querystring;
			window.location = hrefwquery;
			return false;

	    }
	    
	    /**
	     * Monitors the text being entered into the search box for a variety of inputs:
	     * (i)  for continuous conversion from betacode, if required
	     * (ii) for entry of a colon character, to switch into direct string search mode
	     */
	     
	    hic.monitorTextInput = function(){
	    
	    	$(this).unbind('focus');
			$(this).unbind('keypress');
			$(this).unbind('keyup');
	    	var betaOn = $("#beta-on").attr("checked");
	    	colonFound = false;
	    	var selectedRadios = [];
	    	
	    	if(betaOn){

				$(this).keypress(function(event){ return convertCharToggle(this, true, event); });
	    		$(this).keyup(function(event){ return convertStr( this, event ); });	    	    	
	    	
	    	}
	    	else{
			
			$(this).keyup(function(event){			
				event.stopPropagation();
				var val = $(this).val();
				if(!colonFound && val.match(":")) {
				
					colonFound = true;
					$(".stringsearch-section input:radio").attr("disabled", "disabled");
					$(".stringsearch-section input:checkbox").removeAttr("disabled");
					selectedRadios = $(".stringsearch-section input:radio:checked");
					$(".stringsearch-section input:radio:checked").removeAttr("checked");
		
				}
				// check to make sure user hasn't deleted a previously-entered colon char
				else if(!val.match(":") && colonFound){

		            colonFound = false;
					$(".stringsearch-section input:radio").removeAttr("disabled");
				    for(var i = 0; i < selectedRadios.length; i++){

                	    selectedRadios[i].click();   
			    
			    	} 
				
			}
						
			});
	    
	    	}
	    	
	    }
	    
	    /***********************************************
	     START HIDE/REVEAL
	     **********************************************/
	    
	    /**
	     * Hides search controls in order to expand result display
	     *
	     * TODO: clunky. Fix.
	     */
	    hic.hideSearch = function(evt){

		var currentValsWrapperLeft = $("#vals-and-records-wrapper").position().left;
		var initialHeight = $("#facet-wrapper").height();
		var initialWidgetHeight = $("#facet-widgets-wrapper").height();
		var finalHeight = initialHeight > initialWidgetHeight ? initialHeight : initialWidgetHeight;
	    	var newValsWidth = hic.getValsAndRecordsWidth("hide-search");
	    	$("#facet-wrapper").height(initialHeight);
	    	$("#facet-widgets-wrapper").animate({ left: -($("#facet-widgets-wrapper").width() + 23) }, 325);
	    	$("#vals-and-records-wrapper").css({"position":"absolute", "left": currentValsWrapperLeft });
	    	$("#vals-and-records-wrapper").animate({ left: 23, width: newValsWidth }, 325, "swing",
	    		
    		function(){
	    			
    			$("#facet-wrapper").height(finalHeight);
    			$("#facet-widgets-wrapper").addClass("search-closed");
    			$("#facet-widgets-wrapper").offset({ left:-23 });
    			$("#facet-widgets-wrapper").removeClass("search-open");
    			$("#search-toggle").addClass("toggle-closed");
    			$("#search-toggle").removeClass("toggle-open");
    			$("#vals-and-records-wrapper").removeClass("vals-and-records-min");
    			$("#vals-and-records-wrapper").addClass("vals-and-records-max");
    			$("#vals-and-records-wrapper").css({"position":"relative" });
    			var height = initialWidgetHeight > $("#vals-and-records-wrapper").height() ? initialWidgetHeight : $("#vals-and-records-wrapper").height();
    			$("#search-toggle-pointer").text(">>");
    			$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });	    			
    			hic.positionTogglePointer();

    		}
	    	
	    	
    	);
    	$("#search-toggle").unbind('click');
    	$("#search-toggle").click(hic.showSearch);
	    
   }
    
	    /**
	     * Shrinks search results in order to display search panel.
	     *
	     * TODO: Clunky. Fix.
	     */
		
		hic.showSearch = function(evt){
		
			var widgetWrapperWidth = 500;
			var newWidgetWidthVal = widgetWrapperWidth;
			$("#vals-and-records-wrapper").css("position", "absolute");
			$("#facet-widgets-wrapper").removeClass("search-closed");
			$("#facet-widgets-wrapper").css("left", "-" + newWidgetWidthVal + "px");
	    	$("#facet-widgets-wrapper").addClass("search-open");
			var newValsWidth = hic.getValsAndRecordsWidth("show-search");
			$("#facet-widgets-wrapper").animate({ left: 0 }, 325);
			$("#vals-and-records-wrapper").animate({ left: newWidgetWidthVal + 23, width: newValsWidth }, 325, 
			
				function(){
				
					var widgetHeight = $("#facet-widgets-wrapper").height();
					var resultsHeight = $("#vals-and-records-wrapper").height();
					var greaterHeight = widgetHeight > resultsHeight ? widgetHeight : resultsHeight;
					$("#facet-wrapper").height(greaterHeight);
					$("#search-toggle").height($("#facet-wrapper").height());
	    			$("#search-toggle").removeClass("toggle-closed");
	    			$("#search-toggle").addClass("toggle-open");
	    			$("#vals-and-records-wrapper").addClass("vals-and-records-min");
	    			$("#vals-and-records-wrapper").removeClass("vals-and-records-max");	
	    			$("#search-toggle-pointer").text("<<");
	    			hic.positionTogglePointer();

				}
			
			);
			$("#search-toggle").unbind('click');
			$("#search-toggle").click(hic.hideSearch);
		
		}
		
		hic.getValsAndRecordsWidth = function(direction){
		
		      var fullWidth = $(window).width();
		      var searchWidth = (direction == "hide-search") ? 23 : 500;
		      var ownMargin = 23;
		      var ownPadding = 0.02 * fullWidth;
		      var widgetPadding = 25;
		      return fullWidth - searchWidth - ownMargin - ownPadding - widgetPadding - 1;
		
		}
		
		hic.positionTogglePointer = function(){
		
			$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });
		
		}
		
		$(".toggle-open").click(hic.hideSearch);
		$(".toggle-closed").click(hic.showSearch);		
		$("#vals-and-records-wrapper").width(hic.getValsAndRecordsWidth("init"));
		hic.positionTogglePointer();
		$("#search-toggle").height($("#facet-wrapper").height());

		
	    /***********************************************
	     END HIDE/REVEAL
	     **********************************************/
		
		/**
		 * Passes values selected using drop-down date selector
		 * to appropriate text input
		 */
		
		$("select[name='DATE_START'], select[name='DATE_END']").change(function(){ 
			
			var val = $(this).val();
			val = val == "0" ? 1 : val;
			var era = val < 0 ? "BCE" : "CE";
			var correspondingTextInput = $(this).attr("name") + "_TEXT";
			var correspondingEraInput = $(this).attr("name") + "_ERA";
			var passedValue = val == "Unknown" ? "n.a." : Math.abs(val);
			$("input[name='" + correspondingTextInput + "']").val(passedValue);
			$("select[name='" + correspondingEraInput + "']").val(era);
			$("form[name='facets']").submit();

		
		});
		
		/**
		 * Ensures that date era selector change events do not trigger submit unless a date
		 * value has been entered
		 *
		 */
		$("select[name='DATE_START_ERA'], select[name='DATE_END_ERA']").change(function(){
		
	    	var prefix = $(this).attr("name").substring(0, $(this).attr("name").length - 4);
	    	var correlatedText = prefix + "_TEXT";
	    	var correlatedValue = $("input[name='" + correlatedText + "']").val();
	    	if(correlatedValue == "" || correlatedValue == "n.a.") return false;		
			$("form[name='facets']").submit();
		
		});

		/**
		 * Ensures that 'n.a.' style disappears when the user enters new
		 * values manually
		 *
		 */
		$("input#DATE_START_TEXT, input#DATE_END_TEXT").focus(function(){
		
			$(this).css("font-style", "normal");
		
		});
		
		/**
		 * Applies special styling to 'n.a.' value in DATE_START_TEXT and
		 * DATE_END_TEXT controls
		 *
		 */
		$("input#DATE_START_TEXT, input#DATE_END_TEXT").blur(function(){
		
			if($(this).val() == "n.a."){
			
				$(this).css("font-style", "italic");
				
			}
		
		});
		
		/**
		 * Autocomplete functionality for volume text input
		 */
		$("#id-volume").autocomplete({
			
			source: $("#volume-autocomplete").text().split(' ').sort(function(a,b){return a - b;}),
			select: function(event, ui){ 
				
				$("#id-volume").val(ui.item.value);
				hic.tidyQueryString();
				
			}
		});
				
		/**
		 * Autocomplete functionality for id number text input
		 */
		$("#id-idno").autocomplete({
		
			source: $("#idno-autocomplete").text().split(' '),
			select: function(event, ui){
			
				$("#id-idno").val(ui.item.value);
				hic.tidyQueryString();
			}
		
		});
		
		// changing date mode causes tidy and submit
		$("input:radio[name='DATE_MODE']").change(hic.tidyQueryString);
		// turning betacode on/off selects text input
		$("#beta-on").change(function(){$("#keyword").focus();});
		// entry into string search triggers text monitoring 
		$("#keyword").focus(hic.monitorTextInput);
		// $("#keyword").blur(function(){ $("#keyword").focus(hic.monitorTextInput) });
		$("#text-search-widget").find("input[name='target']").click(hic.configureSearchSettings);
		$("#text-search-widget").find("input[name='type']").click(hic.configureSearchSettings);
		// select substring as default
		$("#substring").click();
		// submit triggers tidy ...
		$("form[name='facets']").submit(hic.tidyQueryString);
		// ... unless checks need to be in place first
		$("form select").not("select[name='DATE_START']").not("select[name='DATE_START_ERA']").not("select[name='DATE_END']").not("select[name='DATE_END_ERA']").change(hic.tidyQueryString);
	}

);
