
dojo.provide("dojo.widget.Checkbox");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.event.*");
dojo.require("dojo.html.style");
dojo.require("dojo.html.selection");
dojo.widget.defineWidget(
"dojo.widget.Checkbox",
dojo.widget.HtmlWidget,
{templatePath: dojo.uri.dojoUri('src/widget/templates/Checkbox.html'),
templateCssPath: dojo.uri.dojoUri('src/widget/templates/Checkbox.css'),
name: "",
id: "",
checked: false,
tabIndex: "",
value: "on",
postMixInProperties: function(){dojo.widget.Checkbox.superclass.postMixInProperties.apply(this, arguments);
if(!this.disabled && this.tabIndex==""){ this.tabIndex="0"; }},
fillInTemplate: function(){this._setInfo();},
postCreate: function(){var notcon = true;
this.id = this.id !="" ? this.id : this.widgetId;
if(this.id != ""){var labels = document.getElementsByTagName("label");
if (labels != null && labels.length > 0){for(var i=0; i<labels.length; i++){if (labels[i].htmlFor == this.id){labels[i].id = (labels[i].htmlFor + "label");
this._connectEvents(labels[i]);
dojo.widget.wai.setAttr(this.domNode, "waiState", "labelledby", labels[i].id);
break;}}}}
this._connectEvents(this.domNode);
this.inputNode.checked=this.checked;},
_connectEvents: function( node){dojo.event.connect(node, "onmouseover", this, "mouseOver");
dojo.event.connect(node, "onmouseout", this, "mouseOut");
dojo.event.connect(node, "onkey", this, "onKey");
dojo.event.connect(node, "onclick", this, "_onClick");
dojo.html.disableSelection(node);},
_onClick: function( e){if(this.disabled == false){this.checked = !this.checked;
this._setInfo();}
e.preventDefault();
e.stopPropagation();
this.onClick();},
setValue: function( bool){if(this.disabled == false){this.checked = bool;
this._setInfo();}},
onClick: function(){},
onKey: function( e){var k = dojo.event.browser.keys;
if(e.key == " "){this._onClick(e);}},
mouseOver: function( e){this._hover(e, true);},
mouseOut: function( e){this._hover(e, false);},
_hover: function( e,  isOver){if (this.disabled == false){var state = this.checked ? "On" : "Off";
var style = "dojoHtmlCheckbox" + state + "Hover";
if (isOver){dojo.html.addClass(this.imageNode, style);}else{dojo.html.removeClass(this.imageNode,style);}}},
_setInfo: function(){var state = "dojoHtmlCheckbox" + (this.disabled ? "Disabled" : "") + (this.checked ? "On" : "Off");
dojo.html.setClass(this.imageNode, "dojoHtmlCheckbox " + state);
this.inputNode.checked = this.checked;
if(this.disabled){this.inputNode.setAttribute("disabled",true);}else{this.inputNode.removeAttribute("disabled");}
dojo.widget.wai.setAttr(this.domNode, "waiState", "checked", this.checked);}}
);
dojo.widget.defineWidget(
"dojo.widget.a11y.Checkbox",
dojo.widget.Checkbox,
{templatePath: dojo.uri.dojoUri('src/widget/templates/CheckboxA11y.html'),
fillInTemplate: function(){},
postCreate: function(args, frag){this.inputNode.checked=this.checked;
if (this.disabled){this.inputNode.setAttribute("disabled",true);}},
_onClick: function(){this.onClick();}}
);