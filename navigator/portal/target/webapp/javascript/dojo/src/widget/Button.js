
dojo.provide("dojo.widget.Button");
dojo.require("dojo.lang.extras");
dojo.require("dojo.html.*");
dojo.require("dojo.html.selection");
dojo.require("dojo.widget.*");
dojo.widget.defineWidget(
"dojo.widget.Button",
dojo.widget.HtmlWidget,
{isContainer: true,
caption: "",
templatePath: dojo.uri.dojoUri("src/widget/templates/ButtonTemplate.html"),
templateCssPath: dojo.uri.dojoUri("src/widget/templates/ButtonTemplate.css"),
inactiveImg: "src/widget/templates/images/soriaButton-",
activeImg: "src/widget/templates/images/soriaActive-",
pressedImg: "src/widget/templates/images/soriaPressed-",
disabledImg: "src/widget/templates/images/soriaDisabled-",
width2height: 1.0/3.0,
fillInTemplate: function(){if(this.caption){this.containerNode.appendChild(document.createTextNode(this.caption));}
dojo.html.disableSelection(this.containerNode);},
postCreate: function(){this._sizeMyself();},
_sizeMyself: function(){if(this.domNode.parentNode){var placeHolder = document.createElement("span");
dojo.html.insertBefore(placeHolder, this.domNode);}
dojo.body().appendChild(this.domNode);
this._sizeMyselfHelper();
if(placeHolder){dojo.html.insertBefore(this.domNode, placeHolder);
dojo.html.removeNode(placeHolder);}},
_sizeMyselfHelper: function(){var mb = dojo.html.getMarginBox(this.containerNode);
this.height = mb.height;
this.containerWidth = mb.width;
var endWidth= this.height * this.width2height;
this.containerNode.style.left=endWidth+"px";
this.leftImage.height = this.rightImage.height = this.centerImage.height = this.height;
this.leftImage.width = this.rightImage.width = endWidth+1;
this.centerImage.width = this.containerWidth;
this.centerImage.style.left=endWidth+"px";
this._setImage(this.disabled ? this.disabledImg : this.inactiveImg);
if ( this.disabled ) {dojo.html.prependClass(this.domNode, "dojoButtonDisabled");
this.domNode.removeAttribute("tabIndex");
dojo.widget.wai.setAttr(this.domNode, "waiState", "disabled", true);} else {dojo.html.removeClass(this.domNode, "dojoButtonDisabled");
this.domNode.setAttribute("tabIndex", "0");
dojo.widget.wai.setAttr(this.domNode, "waiState", "disabled", false);}
this.domNode.style.height=this.height + "px";
this.domNode.style.width= (this.containerWidth+2*endWidth) + "px";},
onMouseOver: function( e){if( this.disabled ){ return; }
dojo.html.prependClass(this.buttonNode, "dojoButtonHover");
this._setImage(this.activeImg);},
onMouseDown: function( e){if( this.disabled ){ return; }
dojo.html.prependClass(this.buttonNode, "dojoButtonDepressed");
dojo.html.removeClass(this.buttonNode, "dojoButtonHover");
this._setImage(this.pressedImg);},
onMouseUp: function( e){if( this.disabled ){ return; }
dojo.html.prependClass(this.buttonNode, "dojoButtonHover");
dojo.html.removeClass(this.buttonNode, "dojoButtonDepressed");
this._setImage(this.activeImg);},
onMouseOut: function( e){if( this.disabled ){ return; }
if( e.toElement && dojo.html.isDescendantOf(e.toElement, this.buttonNode) ){return;}
dojo.html.removeClass(this.buttonNode, "dojoButtonHover");
dojo.html.removeClass(this.buttonNode, "dojoButtonDepressed");
this._setImage(this.inactiveImg);},
onKey: function( e){if (!e.key) { return; }
var menu = dojo.widget.getWidgetById(this.menuId);
if (e.key == e.KEY_ENTER || e.key == " "){this.onMouseDown(e);
this.buttonClick(e);
dojo.lang.setTimeout(this, "onMouseUp", 75, e);
dojo.event.browser.stopEvent(e);}
if(menu && menu.isShowingNow && e.key == e.KEY_DOWN_ARROW){dojo.event.disconnect(this.domNode, "onblur", this, "onBlur");}},
onFocus: function( e){var menu = dojo.widget.getWidgetById(this.menuId);
if (menu){dojo.event.connectOnce(this.domNode, "onblur", this, "onBlur");}},
onBlur: function( e){var menu = dojo.widget.getWidgetById(this.menuId);
if ( !menu ) { return; }
if ( menu.close && menu.isShowingNow ){menu.close();}},
buttonClick: function( e){if(!this.disabled){try { this.domNode.focus(); } catch(e2) {};
this.onClick(e);}},
onClick: function( e) {},
_setImage: function( prefix){this.leftImage.src=dojo.uri.dojoUri(prefix + "l.gif");
this.centerImage.src=dojo.uri.dojoUri(prefix + "c.gif");
this.rightImage.src=dojo.uri.dojoUri(prefix + "r.gif");},
_toggleMenu: function( menuId){var menu = dojo.widget.getWidgetById(menuId);
if ( !menu ) { return; }
if ( menu.open && !menu.isShowingNow) {var pos = dojo.html.getAbsolutePosition(this.domNode, false);
menu.open(pos.x, pos.y+this.height, this);} else if ( menu.close && menu.isShowingNow ){menu.close();} else {menu.toggle();}},
setCaption: function( content){this.caption=content;
this.containerNode.innerHTML=content;
this._sizeMyself();},
setDisabled: function( disabled){this.disabled=disabled;
this._sizeMyself();}});
dojo.widget.defineWidget(
"dojo.widget.DropDownButton",
dojo.widget.Button,
{menuId: "",
downArrow: "src/widget/templates/images/whiteDownArrow.gif",
disabledDownArrow: "src/widget/templates/images/whiteDownArrow.gif",
fillInTemplate: function(){dojo.widget.DropDownButton.superclass.fillInTemplate.apply(this, arguments);
this.arrow = document.createElement("img");
dojo.html.setClass(this.arrow, "downArrow");
dojo.widget.wai.setAttr(this.domNode, "waiState", "haspopup", this.menuId);},
_sizeMyselfHelper: function(){this.arrow.src = dojo.uri.dojoUri(this.disabled ? this.disabledDownArrow : this.downArrow);
this.containerNode.appendChild(this.arrow);
dojo.widget.DropDownButton.superclass._sizeMyselfHelper.call(this);},
onClick: function( e){this._toggleMenu(this.menuId);}});
dojo.widget.defineWidget(
"dojo.widget.ComboButton",
dojo.widget.Button,
{menuId: "",
templatePath: dojo.uri.dojoUri("src/widget/templates/ComboButtonTemplate.html"),
splitWidth: 2,
arrowWidth: 5,
_sizeMyselfHelper: function( e){var mb = dojo.html.getMarginBox(this.containerNode);
this.height = mb.height;
this.containerWidth = mb.width;
var endWidth= this.height/3;
if(this.disabled){dojo.widget.wai.setAttr(this.domNode, "waiState", "disabled", true);
this.domNode.removeAttribute("tabIndex");}
else {dojo.widget.wai.setAttr(this.domNode, "waiState", "disabled", false);
this.domNode.setAttribute("tabIndex", "0");}
this.leftImage.height = this.rightImage.height = this.centerImage.height =
this.arrowBackgroundImage.height = this.height;
this.leftImage.width = endWidth+1;
this.centerImage.width = this.containerWidth;
this.buttonNode.style.height = this.height + "px";
this.buttonNode.style.width = endWidth + this.containerWidth + "px";
this._setImage(this.disabled ? this.disabledImg : this.inactiveImg);
this.arrowBackgroundImage.width=this.arrowWidth;
this.rightImage.width = endWidth+1;
this.rightPart.style.height = this.height + "px";
this.rightPart.style.width = this.arrowWidth + endWidth + "px";
this._setImageR(this.disabled ? this.disabledImg : this.inactiveImg);
this.domNode.style.height=this.height + "px";
var totalWidth = this.containerWidth+this.splitWidth+this.arrowWidth+2*endWidth;
this.domNode.style.width= totalWidth + "px";},
_setImage: function(prefix){this.leftImage.src=dojo.uri.dojoUri(prefix + "l.gif");
this.centerImage.src=dojo.uri.dojoUri(prefix + "c.gif");},
rightOver: function( e){if( this.disabled ){ return; }
dojo.html.prependClass(this.rightPart, "dojoButtonHover");
this._setImageR(this.activeImg);},
rightDown: function( e){if( this.disabled ){ return; }
dojo.html.prependClass(this.rightPart, "dojoButtonDepressed");
dojo.html.removeClass(this.rightPart, "dojoButtonHover");
this._setImageR(this.pressedImg);},
rightUp: function( e){if( this.disabled ){ return; }
dojo.html.prependClass(this.rightPart, "dojoButtonHover");
dojo.html.removeClass(this.rightPart, "dojoButtonDepressed");
this._setImageR(this.activeImg);},
rightOut: function( e){if( this.disabled ){ return; }
dojo.html.removeClass(this.rightPart, "dojoButtonHover");
dojo.html.removeClass(this.rightPart, "dojoButtonDepressed");
this._setImageR(this.inactiveImg);},
rightClick: function( e){if( this.disabled ){ return; }
try { this.domNode.focus(); } catch(e2) {};
this._toggleMenu(this.menuId);},
_setImageR: function(prefix){this.arrowBackgroundImage.src=dojo.uri.dojoUri(prefix + "c.gif");
this.rightImage.src=dojo.uri.dojoUri(prefix + "r.gif");},
onKey: function( e){if (!e.key) { return; }
var menu = dojo.widget.getWidgetById(this.menuId);
if(e.key== e.KEY_ENTER || e.key == " "){this.onMouseDown(e);
this.buttonClick(e);
dojo.lang.setTimeout(this, "onMouseUp", 75, e);
dojo.event.browser.stopEvent(e);} else if (e.key == e.KEY_DOWN_ARROW && e.altKey){this.rightDown(e);
this.rightClick(e);
dojo.lang.setTimeout(this, "rightUp", 75, e);
dojo.event.browser.stopEvent(e);} else if(menu && menu.isShowingNow && e.key == e.KEY_DOWN_ARROW){dojo.event.disconnect(this.domNode, "onblur", this, "onBlur");}}});
