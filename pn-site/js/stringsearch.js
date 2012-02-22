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
		
			if(boxval.trim().length > 0 && !hic.lastWordIsKeyword(boxval, controls)) return true;
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
		
		// (en|dis)ables proximity controls appropriately
		
		hic.doProxControlsActivationCheck = function(boxval, controls){
		
			hic.checkProxCountActivate(boxval, controls);
			hic.checkProxUnitActivate($(controls).find(".prxcount").val(), controls);
		
		}
		
		// (en|dis)ables proximity number input box
		
		hic.checkProxCountActivate = function(boxval, controls){
		
			if(boxval.match(proxRegExp)){
			
				$(controls).find(".prxcount").removeAttr("disabled");
				$("#within").css("color", "#000");
				
			}
			else{
			
				var prxcount = $(controls).find(".prxcount");
				$(prxcount).val("");
				$(prxcount).attr("disabled", "disabled");
				$("#within").css("color", "#aaa");
			
			}
		
		}
		
		// (en|dis)ables proximity unit (words|characters) select control
		// appropriately
		
		hic.checkProxUnitActivate = function(boxval, controls){
		
			if(boxval.match(proxCountRegExp)){
			
				$(controls).find(".prxunit").removeAttr("disabled");
				$(controls).find(".prxunit").val("chars");
			
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
			//$(button).addClass("ui-state-default");
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
			input.val(newVal);
			input.focus();
			hic.doButtonActivationCheck(input.val(), parent);
			hic.doProxControlsActivationCheck(input.val(), parent);
			
		}
		
		hic.addNewClause = function(){
		
			var val = $(this).val();
			if(val == 'none') return false;
			var mtr = $(this).parents(topSelector);
			mtr.find("#str-search-controls").remove();
			mtr.find("input.keyword").attr("disabled", "disabled");
			mtr.find("input.prxcount").attr("disabled", "disabled");
			mtr.find(".prx select").attr("disabled", "disabled");
			mtr.after(searchHTML.clone());
			var displayVal = val == "+" ? "" : val.toUpperCase() + " ";
			var textbox = $(lastTopSelector + " .keyword");
			textbox.val(displayVal);
			hic.doButtonActivationCheck("", $(lastTopSelector));
			hic.doProxControlsActivationCheck("", $(lastTopSelector));
			textbox.focus();
			
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
	
		/* TODO: live() now deprecated; replace with on() syntax */
		$(".keyword").live("focus keypress keyup keydown", hic.analyzeTextInput);
		$(".prxcount").live("click keypress keyup", function(){ hic.checkProxUnitActivate($(this).val(), $(this).parents(".prx")); });
		$(".prxunit").live("change", function(evt){ 

				hic.doButtonActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
				hic.doProxControlsActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
				
		});
		$(".syntax-add").live("click", hic.addNewClause);
		$(".syntax-lex").live("click", hic.addKeyWord);
		$(".syntax-then").live("click", hic.addKeyWord);
		$(".syntax-near").live("click", hic.addKeyWord);
		$(".syntax-regex").live("click", hic.addKeyWord);
		$(".syntax-and").live("click", hic.addNewClause);
		$(".syntax-or").live("click", hic.addNewClause);
		$(".syntax-not").live("click", hic.doNot);
		$(".syntax-clear").live("click", hic.clearValues);
		$(".syntax-remove").live("click", function(){
		
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
		
		});

		hic.doButtonActivationCheck("", $(topSelector));
		
});