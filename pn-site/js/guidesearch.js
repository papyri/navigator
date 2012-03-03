$(document).ready(

	/**
	* Various UI-related functions
	*
	*/

	function(){
	
		// activate tm search
		$("li.dialog").each(function(i) {
          $(this).after("<li><a href=\"#\" onclick=\"javascript:jQuery('#"+this.id+"').dialog()\">"+this.title+"<\/a><\/li>");
          $(this).hide();
        }); 
					
		// a little namespacing
		
		if(typeof info == 'undefined') info = {};
		if(typeof info.papyri == 'undefined') info.papyri = {};
		if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
		if(typeof info.papyri.thill.guidesearch == 'undefined') info.papyri.thill.guidesearch = {};
			
		// alias to save typing	
		var hic = info.papyri.thill.guidesearch;
		hic.HIDE_REVEAL_COOKIE = "togglestate";		// for persising show/hide search panel
		hic.BETA_COOKIE = "betacode";				// for persisting beta-as-you-type settings
		hic.SEARCH_STACK = "searchstack";			// back-button behaviour for string-search
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

		hic.reqd_on["target-metadata"] = ["#caps", "#marks"];
		hic.reqd_off["target-metadata"] = ["#beta-on"];
		hic.reqd_on["target-translations"] = ["#caps", "#marks"];
		hic.reqd_off["target-translations"] = ["#beta-on"];
		
		/**
		 * Restricts user options so that only possible string-search configurations
		 * can be set
		 * TODO: needs to be revised in wake of poor user response!
		 */
		
	   	hic.configureSearchSettings = function(){
	   	
	   		var val = $(this).val();
	   		
	   		if(val == "text"){
	   		
	   			$("#beta-on, #caps, #marks").removeAttr("disabled", "disabled");
	   			hic.checkBetacode();
	   			
	   		} 
	   		else{
	   		
	   			$("#beta-on").removeAttr("checked");
	   			$("#beta-on").attr("disabled", "disabled");
	   			$("#caps").attr("checked", "checked");
	   			$("#caps").attr("disabled", "disabled");
	   			$("#marks").attr("checked", "checked");
	   			$("#marks").attr("disabled", "disabled");	   			
	   		
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
	    	// mixedsearch refers to string searches of the user-defined type
	    	// i.e., not necessarily corresponding to the string-search types
	    	// defined by the interface controls
	    	var mixedsearch = false;
	    	
	    	// if a string is set for search, than the associated text, target, and option
	    	// fields must also be set.
	    	
	    	var textval = hic.buildTextSearchString();
	    	if(!textval.match(/^\s*$/)){

				if(textval.indexOf(":") != -1) mixedsearch = true;
				
				var textel = $("<input type=\"text\" name=\"STRING\"></input>");
				textel.val(textval);
                filteredels.push(textel);
	    		var betas = $("#betaYes:checked");
	    		if(betas.length > 0) filteredels.push(betas);
	    		
	    		var caps = $("#caps:checked");
	    		if(caps.length > 0) filteredels.push(caps);
	    		
	    		var marks = $("#marks:checked");
	    		if(marks.length > 0) filteredels.push(marks);

                if(!mixedsearch){
                 
				     filteredels.push($("input[name='target']").filter(":checked"));	
	    		
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
	    	
	    	var docsperpage = $("#DOCS_PER_PAGE");
	    	var docsval = docsperpage.val();
	    	if(docsval.match(/^\d{1,3}$/) && docsval > 0) filteredels.push(docsperpage);
	    	
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
	    		if(htype == "hidden"){
	    		
	    			// the hidden collection field should be overriden by settings in the
	    			// control itself
	    			if(hidden.getAttribute("name") == "COLLECTION"){
	    			
	    				var collvalue = $("select[name='COLLECTION']").attr("value");
	    				if(collvalue != "default"){
	    				
	    					filteredels.push($("select[name='COLLECTION']"));
	    					continue;
	    						
	    				}
	    				else{
	    				
	    					filteredels.push(hidden);
	    				
	    				}
	    				
	    			}
	    		
	    			filteredels.push(hidden);			
	    			
	    		}
	    	
	    	}
	    	
	    	var params = {};
			
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
				params[name] = val;
				
			}
			
			if(mixedsearch){
			
				params["target"] = "user_defined";
			
			}
			
			$.get("http://localhost/search", params);

            var current = window.location;

            if(current.toString().match(/\?/)) {
			
				var currentbits = current.toString().split("?");
				current = currentbits[0];
			
			}
			hic.concatenateSearchToCookie(textval);
			var hrefwquery = current + "?" + $.param(params);
			window.location = hrefwquery;
			return false;
	    }
	    
	    hic.buildTextSearchString = function(){
	    
	    	var proxRegExp = new RegExp(/\s+(THEN|NEAR)\s+/);
	    	var totalSearchString = "";
	    	var stringcontrols = $(".stringsearch-top-controls");

	    	for(var i = 0; i < stringcontrols.length; i++){
	    	
	    		var keyword = $(stringcontrols[i]).find(".keyword").val();
	    		var searchString = keyword.replace(/(\s+)/g, " ");
	    		searchString = $.trim(searchString);	    		
	    		if(searchString.length == 0) continue;
	    		searchString = "(" + searchString + ")";
	    		if(keyword.match(proxRegExp)){
	    		
	    			var proxcount = $(stringcontrols[i]).find(".prxcount").val().match(/^\d{1,2}$/) ? $(stringcontrols[i]).find(".prxcount").val() : "1";
	    			var proxunit = $(stringcontrols[i]).find(".prxunit").val() == "words" ? "words" : "chars";	// default to 'chars'
	    			searchString += "~" + proxcount + proxunit;
	    		
	    		}	 
	    		searchString = i == 0 ? searchString : "¤" + searchString;
	    		totalSearchString += searchString;
	    	
	    	}
			totalSearchString = totalSearchString.replace(/\)¤\(OR/g, " OR");
	    	return totalSearchString;
	    
	    }
	    
	    hic.concatenateSearchToCookie = function(textval){
	    
	    	var searchstack = $.cookie(hic.SEARCH_STACK) ? $.cookie(hic.SEARCH_STACK) : "";
	    
	    	if(textval){
	    	
	    		if(searchstack.length > 1) searchstack += "|";
	    		searchstack += textval;
	    	
	    	}

	    	$.cookie(hic.SEARCH_STACK, searchstack);
	    
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

			var widthcomp = $("#search-toggle").outerWidth() + 1;
			var delay = evt.data.delay;
			var currentValsWrapperLeft = $("#vals-and-records-wrapper").position().left;
			var initialHeight = $("#facet-wrapper").height();
			var initialWidgetHeight = $("#facet-widgets-wrapper").height();
			var finalHeight = initialHeight > initialWidgetHeight ? initialHeight : initialWidgetHeight;
	    	var newValsWidth = hic.getValsAndRecordsWidth("hide-search");
	    	$("#facet-wrapper").height(initialHeight);
	    	$("#facet-widgets-wrapper").animate({ left: -($("#facet-widgets-wrapper").width() + widthcomp) }, delay);
	    	window.setTimeout(function(){
	    		$(".title-long").css("display", "block")
	    		$(".title-short").css("display", "none");
	    		}, 150);
	    	$("#vals-and-records-wrapper").css({"position":"absolute", "left": currentValsWrapperLeft });
	    	$("#vals-and-records-wrapper").animate({ left: 0, width: newValsWidth }, delay, "swing",
	    		
    			function(){
	    			
	    			$("#reset-all, #search").addClass("hidden-buttons");
    				$("#facet-wrapper").height(finalHeight);
    				$("#facet-widgets-wrapper").addClass("search-closed");
    				$("form[name='facets']").addClass("search-closed");
    				$("#facet-widgets-wrapper").offset({ left:-widthcomp });
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

    			});
    	$("#search-toggle").unbind('click');
    	$("#search-toggle").click(hic.showSearch);
    	$.cookie(hic.COOKIE, 0);
	    
   }
    
	    /**
	     * Shrinks search results in order to display search panel.
	     *
	     * TODO: Clunky. Fix.
	     */
		
		hic.showSearch = function(evt){
		
			$("#reset-all, #search").removeClass("hidden-buttons");
			var widgetWrapperWidth = 500;
			var widthcomp = $("#search-toggle").outerWidth() + 1;
			var newWidgetWidthVal = widgetWrapperWidth;
			$("#vals-and-records-wrapper").css("position", "absolute");
			$("#facet-widgets-wrapper").removeClass("search-closed");
	    	$("form[name='facets']").removeClass("search-closed");
			$("#facet-widgets-wrapper").css("left", "-" + newWidgetWidthVal + "px");
	    	$("#facet-widgets-wrapper").addClass("search-open");
			var newValsWidth = hic.getValsAndRecordsWidth("show-search");
			window.setTimeout(function(){
	    		$(".title-long").css("display", "none");
	    		$(".title-short").css("display", "block");
	    	}, 200);
			$("#facet-widgets-wrapper").animate({ left: 0 }, 325);
			$("#vals-and-records-wrapper").animate({ left: newWidgetWidthVal - widthcomp, width: newValsWidth }, 325, 
			
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
			$("#search-toggle").click({ delay:325 }, hic.hideSearch);
			$.cookie(hic.COOKIE, null);
		
		}
		
		hic.getValsAndRecordsWidth = function(direction){
		
		      var fullWidth = $(window).width();
		      var searchWidth = (direction == "hide-search") ? $("#search-togger").outerWidth() + 1 : 500;
		      var ownMargin = 23;
		      var ownPadding = 0.02 * fullWidth;
		      var widgetPadding = 25;
		      return fullWidth - searchWidth - ownMargin - ownPadding - widgetPadding - 1;
		
		}
		
		hic.positionTogglePointer = function(){
		
			$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });
		
		}
			
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
		
		/**
		 * Autocomplete for provenance
		 */
		
		$("#id-place").autocomplete({
		
			//source: $("#place-autocomplete").text().split('¤'),
			source: function(req, responseFn){
			
				var re = $.ui.autocomplete.escapeRegex(req.term);
				var matcher = new RegExp( "^" + re, "i" );
				var a = $.grep(  $("#place-autocomplete").text().split('¤'), function(item,index){ return matcher.test(item);});
        		responseFn( a );				
			
			},
			select: function(event, ui){
			
				$("#id-place").val(ui.item.value);
				hic.tidyQueryString();
			}
		
		});
		
		hic.isSubsequentPage = function(){
		
			var pageno = decodeURI((RegExp('page=(\\d+)(&|$)').exec(location.search)||[,null])[1]);
			if(pageno != 'null') return true;
			return false;
		
		}
		
		hic.checkBetacode = function(){

			if($.cookie(hic.BETA_COOKIE) == "beta-on"){
			
				$("#beta-on").attr("checked", "checked");
			
			} else {
		
				$("#beta-on").removeAttr("checked");
		
			}		
		
		}
	
		$("#text-search-widget").find("input[name='target']").click(hic.configureSearchSettings);
		// select substring as default
		$("#substring").click();
		$(".toggle-open").click({ delay: 325 }, hic.hideSearch);
		$(".toggle-closed").click(hic.showSearch);		
		$("#vals-and-records-wrapper").width(hic.getValsAndRecordsWidth("init"));
		hic.positionTogglePointer();
		$("#search-toggle").height($("#facet-wrapper").height());
		// changing date mode causes tidy and submit
		$("input:radio[name='DATE_MODE']").change(hic.tidyQueryString);
		// turning betacode on/off selects text input
		$("#beta-on").change(function(){
			
			$(".stringsearch-top-controls:last .keyword").focus();
			var beta = $(this).attr("checked") ? "beta-on" : "beta-off";
			$.cookie(hic.BETA_COOKIE, beta);
			
		});
		// entry into string search triggers text monitoring 
		$(".stringsearch-top-controls:last .keyword").live("focus", hic.monitorTextInput);

		//$(".stringsearch-top-controls:last .keyword").live("blur", function(){ $("#keyword").focus(hic.monitorTextInput) });
		// submit triggers tidy ...
		$("form[name='facets']").submit(hic.tidyQueryString);
		// ... unless checks need to be in place first
		$("form select").not("select[name='DATE_START']").not("select[name='DATE_START_ERA']").not("select[name='DATE_END']").not("select[name='DATE_END_ERA']").not("select[name='prxunit']").change(hic.tidyQueryString);
		// sets cookie on click to record to allow reversion to current search results
		$("td.identifier a").click(function(e){  hic.setCookie("lbpersist", window.location.search, 12); return true; });
		$("#reset-all").click(function(e){
		
			$.cookie(hic.SEARCH_STACK, null);
			return true;
		
		});
		hic.checkBetacode();
		if($.cookie(hic.HIDE_REVEAL_COOKIE) == 0 && hic.isSubsequentPage()){
		
			var e = {};
			e.data = {};
			e.data.delay = 0;
			hic.hideSearch(e);
		
		}

	
	}
	


);
