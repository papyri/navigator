// jQuery plugin: PutCursorAtEnd 1.0
// http://plugins.jquery.com/project/PutCursorAtEnd
// by teedyay
//
// Puts the cursor at the end of a textbox/ textarea

// codesnippet: 691e18b1-f4f9-41b4-8fe8-bc8ee51b48d4
(function($)
{
    jQuery.fn.putCursorAtEnd = function()
    {
    return this.each(function()
    {
        $(this).focus()

        // If this function exists...
        if (this.setSelectionRange)
        {
        // ... then use it
        // (Doesn't work in IE)

        // Double the length because Opera is inconsistent about whether a carriage return is one character or two. Sigh.
        var len = $(this).val().length * 2;
        this.setSelectionRange(len, len);
        }
        else
        {
        // ... otherwise replace the contents with itself
        // (Doesn't work in Google Chrome)
        $(this).val($(this).val());
        }

        // Scroll to the bottom, in case we're in a tall textarea
        // (Necessary for Firefox and Google Chrome)
        this.scrollTop = 999999;
    });
    };
})(jQuery);

$(document).ready(

	/**
	* Functions related to UI text-search controls
	*
	* Note that the main issue is which buttons should be (en|dis)abled depending
	* on the text in the input box. Accordingly, the general logic of the functions
	* grouped here is:
	* (1) define a number of filter functions which return booleans
	* (2) create a data structure (hic.activationRules) associating the appropriate set of
	*     filter functions with the name of each button
	* (3) Each time the text in the input box changes, iterate through the buttons,
	*     testing each against the required filter functions, and (en|dis)able each
	*     accordingly
	*
	* Note that this is not the *entire* functionality of the functions grouped here; 
	* just the bits where the flow is hard to follow. Specific points are made ad loc.
	*/

	function(){
					
		// namespacing
		
		if(typeof info == 'undefined') info = {};
		if(typeof info.papyri == 'undefined') info.papyri = {};
		if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
		if(typeof info.papyri.thill.stringsearch == 'undefined') info.papyri.thill.stringsearch = {};
		var hic = info.papyri.thill.stringsearch;
		
		// "import"
		hic.SEARCH_STRING_STACK = info.papyri.thill.guidesearch.SEARCH_STACK;
		
		// first, we make a copy of the entire text-search control block
		// for easy repetition when users want to add controls
		// (see hic.addNewClause function)
		
		var topSelectorID = "stringsearch-top-controls";
		var topSelector = "." + topSelectorID;
		var lastTopSelector = topSelector + ":last";
		var w = $(topSelector);
		var searchHTML = $(w.clone());

		// next, we define a bunch of regexes for use in the various
		// filtration functions
		
		var whitespaceOnlyRegExp = new RegExp(/^\s*$/);
		var lastWordIsKeywordRegExp = new RegExp(/(AND|OR|NOT|(LEX(:?))|THEN|NEAR|REGEX)\s*$/);
		var lastWordIsKeywordNotNotRegExp = new RegExp(/(AND|OR|LEX|THEN|NEAR|REGEX)\s*$/);
		var alreadyContainsProximityRegExp = new RegExp(/(THEN|NEAR)/);
		var notNotPrecededRegExp = new RegExp(/^\s*NOT\s*$/);
		var proxCountRegExp = new RegExp(/^\d{1,2}$/);
		var proxRegExp = new RegExp(/(THEN|NEAR)\s+\S+/);
		var alreadyContainsRegexRegExp = new RegExp(/REGEX/);
		var alreadyContainsNotRegExp = new RegExp(/NOT/);
		var alreadyContainsConjunctionRegExp = new RegExp(/(AND|OR)/);
		var alreadyContainsLexRegExp = new RegExp(/LEX/);
		var alreadyContainsAndRegExp = new RegExp(/AND/);
		var alreadyContainsOrRegExp = new RegExp(/OR/);	
		var stringIsToBeLemmatisedRegExp = new RegExp(/LEMMA[\s]*(\S+)?$/);	
		
		/*************************
		 * filtration functions  *
		**************************/
		
		hic.textBoxIsEmpty = function(boxval, controls){
		
			if(boxval.match(whitespaceOnlyRegExp)) return true;
			return false;
		
		}
		
		hic.textBoxHasContent = function(boxval, controls){
		
			if(!boxval.match(whitespaceOnlyRegExp)) return true;
			return false;
		
		}
		
		hic.alreadyContainsRegex = function(boxval, controls){
		
			if(boxval.match(alreadyContainsRegexRegExp)) return true;
			return false;
		
		}
		
		hic.alreadyContainsNot = function(boxval, controls){
		
			if(boxval.match(alreadyContainsNotRegExp)) return true
			return false;
		
		}
		
		hic.lastWordIsKeyword = function(boxval, controls){
		
			if(boxval.match(lastWordIsKeywordRegExp)) return true;
			return false;
		
		}
		
		hic.lastWordIsWordButNotKeyword = function(boxval, controls){
		
			boxval = boxval.replace(" ", "");
			if(boxval != "" && !hic.lastWordIsKeyword(boxval, controls)) return true;
			return false;
		
		
		}
		
		hic.lastWordIsKeywordOtherThanNot = function(boxval, controls){
		
			if(boxval.match(lastWordIsKeywordNotNotRegExp)) return true;
			return false;		
		
		}
		
		hic.alreadyContainsProximity = function(boxval, controls){
		
			if(boxval.match(alreadyContainsProximityRegExp)) return true;
			return false;
		
		}
		
		hic.notAloneNotPreceded = function(boxval, controls){
		
			if(boxval.match(notNotPrecededRegExp)) return true;
			return false;
		
		}
		
		hic.alreadyContainsConjunction = function(boxval, controls){
		
			if(boxval.match(alreadyContainsConjunctionRegExp)) return true;
			return false;
		
		}
		
		hic.alreadyContainsLex = function(boxval, controls){
		
			if(boxval.match(alreadyContainsLexRegExp)) return true;
			return false;
		
		}
		
		hic.alreadyContainsAnd = function(boxval, controls){
		
			if(boxval.match(alreadyContainsAndRegExp)) return true;
			return false;
		
		}
		
		hic.alreadyContainsOr = function(boxval, controls){
		
			if(boxval.match(alreadyContainsOrRegExp)) return true;
			return false;
		
		}
		
		hic.stringIsToBeLemmatised = function(boxval, controls){
		
			if(boxval.match(stringIsToBeLemmatisedRegExp)) return true;
			return false;
		
		}
		
		hic.isAwaitingProximityInput = function(boxval, controls){
		
			if(boxval.match(proxRegExp)){
			
				var prxcount = $(controls).find(".prxcount").val();
				if(!prxcount.match(proxCountRegExp)) return true;
				var prxunit = $(controls).find(".prxunit").val();
				if(prxunit == 'none') return true;
			
			}
			return false;
		
		}
		
		hic.onlyOneTextInput = function(boxval, controls){
		
			return $(topSelector).length == 1;
		
		}
		
		/****************************
		 * end filtration functions *
		*****************************/
		
		// build filtration data structure
		
		hic.activationRules = {};
		hic.activationRules["AND"] = [hic.textBoxIsEmpty, hic.lastWordIsKeyword, hic.alreadyContainsOr];
		hic.activationRules["OR"] = [hic.textBoxIsEmpty, hic.lastWordIsKeyword, hic.alreadyContainsAnd, hic.alreadyContainsNot, hic.alreadyContainsProximity];
		hic.activationRules["NOT"] = [hic.lastWordIsKeyword];
		hic.activationRules["LEX"] = [hic.lastWordIsWordButNotKeyword, hic.isAwaitingProximityInput, hic.alreadyContainsRegex, hic.alreadyContainsLex];
		hic.activationRules["THEN"] = [hic.textBoxIsEmpty, hic.alreadyContainsNot, hic.alreadyContainsProximity, hic.lastWordIsKeyword, hic.alreadyContainsRegex, hic.alreadyContainsNot, hic.alreadyContainsConjunction];
		hic.activationRules["NEAR"] = [hic.textBoxIsEmpty, hic.alreadyContainsNot, hic.alreadyContainsProximity, hic.lastWordIsKeywordOtherThanNot, hic.notAloneNotPreceded, hic.alreadyContainsRegex, hic.alreadyContainsNot, hic.alreadyContainsConjunction];
		hic.activationRules["REGEX"] = [hic.textBoxHasContent];
		hic.activationRules["CLEAR"] = [hic.textBoxIsEmpty];
		hic.activationRules["ADD"] = [hic.textBoxIsEmpty, hic.lastWordIsKeyword, hic.isAwaitingProximityInput];
		hic.activationRules["REMOVE"] = [hic.onlyOneTextInput];	
		hic.activationRules["ABBR"] = [hic.stringIsToBeLemmatised];
		
		// (en|dis)ables proximity controls appropriately
		
		hic.doProxControlsActivationCheck = function(boxval, controls){
		
			hic.checkProxCountActivate(boxval, controls);
			hic.checkProxUnitActivate($(controls).find(".prxcount").val(), controls);
		
		}
		
		// (en|dis)ables proximity number input box
		
		hic.checkProxCountActivate = function(boxval, controls){
		
			if(boxval.match(proxRegExp)){
	
				$(controls).find(".prxcount").removeAttr("disabled");
				$(controls).find(".within").css("color", "#000");
				
			}
			else{
			
				var prxcount = $(controls).find(".prxcount");
				$(prxcount).val("");
				$(prxcount).attr("disabled", "disabled");
				$(controls).find(".within").css("color", "#aaa");
			
			}
		
		}
		
		// (en|dis)ables proximity unit (words|characters) select control
		// appropriately
		
		hic.checkProxUnitActivate = function(boxval, controls){
		
			if(boxval.match(proxCountRegExp)){
			
				var prxunit = $($(controls).find(".prxunit"));
				prxunit.removeAttr("disabled");
				if(prxunit.val() != "words" && prxunit.val() != "chars") prxunit.val("chars");
			
			}
			else{
			
				var prxunit = $(controls).find(".prxunit");
				$(prxunit).val("none");
				$(prxunit).attr("disabled", "disabled");
			
			}
		
		}
		
		// iterates through all buttons and their appropriate filtration
		// functions to (en|dis)able appropriately
		
		hic.doButtonActivationCheck = function(textBoxValue, controls){
		
			var buttons = $(".syntax");
			
			OUTER: for(var i = 0; i < buttons.length; i++){
			
				var button = $(buttons[i]);
				var name = button.val().toUpperCase();
				if(name == "+") name = "ADD";
				if(name == "-") name = "REMOVE";
				
				var failCriteria = hic.activationRules[name] || [];
						
				INNER: for(var j = 0; j < failCriteria.length; j++){
				
						var failFunction = failCriteria[j];
						var failed = failFunction(textBoxValue, controls);
						if(failed){

							hic.disableButton(button);
							continue OUTER;
						}
				
				}
				
				hic.activateButton(button);
			
			}
		
		}
		
		hic.activateButton = function(button){
		
			$(button).removeAttr("disabled");
			$(button).removeClass("ui-state-disabled");
			$(button).css("background", "#C0D3BC url(css/custom-theme/images/ui-bg_glass_75_c0d3bc_1x400.png) 50% 50% repeat-x");
		
		}
		
		hic.disableButton = function(button){
		
			$(button).attr("disabled", "disabled");
			$(button).removeClass("ui-state-default");
			$(button).addClass("ui-state-disabled");
		
		}
				
		hic.addKeyWord = function(){

			var keyword = $(this).val().toUpperCase();
			var parent = $(this).parents(topSelector);
			var input = $(parent).find(".keyword");
			var val = input.val();
			var newVal = (val.length > 0 ? val + " " : val) + keyword;
			newVal += " ";
			input.focus();
			input.val(newVal);
			hic.doButtonActivationCheck(input.val(), parent);
			hic.doProxControlsActivationCheck(input.val(), parent);
			
		}
		
		hic.addAbbreviationMark = function(){
		
			var mark = '&#x00B0';
			var parent = $(this).parents(topSelector);
			var input = $(parent).find(".keyword");
			var val = input.val();
			var newVal = val + keyword;
			input.focus();
			input.val(newVal);
			hic.doButtonActivationCheck(input.val(), parent);
			hic.doProxControlsActivationCheck(input.val(), parent);				
		
		}
		
		hic.addNewClause = function(){
		
			var val = $(this).val();
			if(val == 'none') return false;
			var mtr = $(this).parents(topSelector);
			mtr.find(".within").css("color", "#aaa");
			mtr.find("#str-search-controls").remove();
			mtr.find("input.keyword").attr("disabled", "disabled");
			mtr.find("input.prxcount").attr("disabled", "disabled");
			mtr.find(".prx select").attr("disabled", "disabled");
			mtr.after(searchHTML.clone());
			var displayVal = val == "+" ? "" : val.toUpperCase() + " ";
			var textbox = $(lastTopSelector + " .keyword");
			textbox.removeAttr("disabled");
			textbox.focus();
			textbox.val(displayVal);
			hic.doButtonActivationCheck("", $(lastTopSelector));
			hic.doProxControlsActivationCheck("", $(lastTopSelector));
			
		}
		
		hic.doNot = function(){
		
			var textval = $(this).parents(topSelector).find(".keyword").val();
			if(hic.textBoxIsEmpty(textval, null)){
			
				hic.addKeyWord.call($(this));
			
			}
			else{
			
				hic.addNewClause.call($(this));
			
			}
		
		}
		
		hic.clearValues = function(){
		
			var textbox = $(this).parents(topSelector).find(".keyword");
			$(textbox).val("");
			$(this).parents(topSelector).find(".str-operator").text("");	
			hic.clearProxValues(this);
			hic.doButtonActivationCheck("", $(topSelector));
			$(textbox).focus();
			
		}
		
		hic.clearProxValues = function(that){
		
			$(that).parents(topSelector).find(".prxcount").val("");
			$(that).parents(topSelector).find(".prxunit option[value='none']").attr("selected", "selected");			
		
		}
		
		hic.analyzeTextInput = function(evt){
		
			var controls = $(this).parents(topSelector);
			var val = $(this).val();
			val += evt.type.indexOf("key") == -1 ? "" : evt.which;
			if(evt.which == 8){
				
				val = val.substring(0, val.length - 2);
			
			}
			hic.doButtonActivationCheck(val, controls);
			hic.doProxControlsActivationCheck(val, controls);
			
		}
		
		hic.restoreFullTextSearch = function(){
		
			var nowVal = $(topSelector + " .keyword").val();
			if(nowVal && nowVal != ""){
			
				var allstrings = $.cookie(hic.SEARCH_STRING_STACK);
				if(!allstrings || allstrings == "") return false;
				var stringbits = allstrings.split("|");
				var nowsearch = stringbits[stringbits.length - 1];
				var searchbits = hic.trimRecoveredStringSearches(nowsearch);
				hic.addReqdSearchBoxes(searchbits);
				hic.removeSearchFromStack(stringbits);	
				$(topSelector + ":last .keyword").removeAttr("disabled");
				$(topSelector + ":last .keyword").focus();
				$(topSelector + ":last .keyword").putCursorAtEnd();
			
			}
		
		}
		
		hic.trimRecoveredStringSearches = function(nowsearch){
		
			nowsearch = nowsearch.replace(/ OR /g, ")¤(OR ");
			var searchbits = nowsearch.split("¤");
			for(i = 0; i < searchbits.length; i++){
			
				var bit = searchbits[i];
				bit = bit.substring(1, bit.length - 1);		// trim brackets
				searchbits[i] = bit;
 			
			}
		
			return searchbits;
		
		}
		
		hic.addReqdSearchBoxes = function(searchbits){
		
			if(searchbits.length < 2) return;
			var bit = "";
		
			for(i = 1; i < searchbits.length; i++){
			
				hic.addNewClause.call($(topSelector + ":last .keyword"));
				bit = searchbits[i];
				$(topSelector + ":last .keyword").val(bit);
						
			}
			
			var controls = $(topSelector + ":last");
			hic.doButtonActivationCheck(bit, controls);
			hic.doProxControlsActivationCheck(bit, controls);
		
		}
		
		hic.removeSearchFromStack = function(stringbits){
		
			var allstring = "";
			var penult = stringbits.length - 1;
			
			for(i = 0; i < penult; i++){
				
				allstring += stringbits[i];
				if(i < penult - 1) allstring += "|";
			
			}
			
			if(allstring == "") allstring = null;
			$.cookie(hic.SEARCH_STRING_STACK, allstring);
		
		}
	
		$("#text-search-widget").on("focus keypress keyup keydown", ".keyword", hic.analyzeTextInput);
		$("#text-search-widget").on("click keypress keyup", ".prxcount", function(){ hic.checkProxUnitActivate($(this).val(), $(this).parents(".prx")); });
		$("#text-search-widget").on("change", ".prxunit", function(evt){ 

				hic.doButtonActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
				hic.doProxControlsActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
				
		});
		$("#text-search-widget").on("click", ".syntax-add", hic.addNewClause);
		$("#text-search-widget").on("click", ".syntax-lex", hic.addKeyWord);
		$("#text-search-widget").on("click", ".syntax-then", hic.addKeyWord);
		$("#text-search-widget").on("click", ".syntax-near", hic.addKeyWord);
		$("#text-search-widget").on("click", ".syntax-regex", hic.addKeyWord);
		$("#text-search-widget").on("click", ".syntax-and", hic.addNewClause);
		$("#text-search-widget").on("click", ".syntax-or", hic.addNewClause);
		$("#text-search-widget").on("click", ".syntax-not", hic.doNot);
		$("#text-search-widget").on("click", ".syntax-clear", hic.clearValues);
		$("#text-search-widget").on("click", ".syntax-abbr", hic.addAbbreviationMark);
		$("#text-search-widget").on("click", ".syntax-then-not", hic.addKeyWord);
		$("#text-search-widget").on("click", ".syntax-not-after", hic.addKeyWord);		
		$("#text-search-widget").on("click", ".syntax-remove", function(){
		
			var buttonBar = $(this).parent().clone();
			$(this).parent().parent().remove();
			$(lastTopSelector).append(buttonBar);
			$(lastTopSelector).find(".keyword").removeAttr("disabled");
			$(lastTopSelector).find(".keyword").focus();
			var prxcount = $(lastTopSelector + " input.prxcount");
			if(prxcount.val().length > 0){
			
				$(lastTopSelector + " .prx *").removeAttr("disabled");
			
			}
			hic.doButtonActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
			$(lastTopSelector).find(".keyword").putCursorAtEnd();
		});

		hic.doButtonActivationCheck("", $(topSelector));
		hic.restoreFullTextSearch();
		
});