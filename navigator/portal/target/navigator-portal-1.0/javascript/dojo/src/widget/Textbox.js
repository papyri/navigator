
dojo.provide("dojo.widget.Textbox");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Manager");
dojo.require("dojo.widget.Parse");
dojo.require("dojo.xml.Parse");
dojo.require("dojo.lang.array");
dojo.require("dojo.lang.common");
dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.widget", "validate", null, "fr,ja,ROOT,zh-cn");
dojo.widget.defineWidget(
"dojo.widget.Textbox",
dojo.widget.HtmlWidget,
{className: "",
name: "",
value: "",
type: "",
trim: false,
uppercase: false,
lowercase: false,
ucFirst: false,
digit: false,
htmlfloat: "none",
templatePath: dojo.uri.dojoUri("src/widget/templates/Textbox.html"),
textbox: null,
fillInTemplate: function() {this.textbox.value = this.value;},
filter: function() {if (this.trim) {this.textbox.value = this.textbox.value.replace(/(^\s*|\s*$)/g, "");} 
if (this.uppercase) {this.textbox.value = this.textbox.value.toUpperCase();} 
if (this.lowercase) {this.textbox.value = this.textbox.value.toLowerCase();} 
if (this.ucFirst) {this.textbox.value = this.textbox.value.replace(/\b\w+\b/g, 
function(word) { return word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase(); });} 
if (this.digit) {this.textbox.value = this.textbox.value.replace(/\D/g, "");}},
onfocus: function() {},
onblur: function() { this.filter(); },
mixInProperties: function(localProperties, frag) {dojo.widget.Textbox.superclass.mixInProperties.apply(this, arguments);
if ( localProperties["class"] ) {this.className = localProperties["class"];}}}
);
