dojo.provide("jetspeed.widget.PortalTooltipManager");

dojo.require("dojo.widget.PopupContainer");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.uri.Uri");
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.html.style");
dojo.require("dojo.html.util");

dojo.widget.defineWidget(
	"jetspeed.widget.PortalTooltipManager",
	[ dojo.widget.HtmlWidget, dojo.widget.PopupContainerBase ],
	function(){
		this.connections = [];
	},
	{
		// summary
		//		Pops up a tooltip (a help message) when you hover over a node

        startNorm: 1,
        startForce: 2,
        startAbort: 3,
			
        templateString: '<div dojoAttachPoint="containerNode" style="display:none;position:absolute;" class="portalTooltip" ></div>',

		fillInTemplate: function(args, frag){
            // no need for superclass.fillInTemplate - it is not implemented in any superclass

			//copy style from input node to output node
			var source = this.getFragNodeRef(frag);
			dojo.html.copyStyle(this.domNode, source);

			//apply the necessary css rules to the node so that it can popup
			this.applyPopupBasicStyle();
		},

        _setCurrent: function( portalTooltipDisplay )
        {   
            var proceed = this.startNorm;
            var curTooltipDisplay = this._curr;
            if ( curTooltipDisplay != null )
            {
                if ( ! ( curTooltipDisplay.connectNode.parentNode === portalTooltipDisplay.connectNode ) )
                    curTooltipDisplay.close();
                else
                {
                    if ( curTooltipDisplay._tracking )
                    {
                        if ( ! this.isShowingNow )
                            proceed = this.startAbort;
                        else
                        {
                            curTooltipDisplay._onUnHover();
                            proceed = this.startForce;
                        }
                    }
                    else
                    {
                        curTooltipDisplay.close();
                    }
                }
            }
            if ( proceed != this.startAbort )
                this._curr = portalTooltipDisplay;
            
            return proceed;
        },
        _isCurrent: function( portalTooltipDisplay, clear )
        {
            if ( this._curr === portalTooltipDisplay )
            {
                if ( clear ) this._curr = null;
                return true;
            }
            return false;
        },

    	open: function( portalTooltipDisplay, /*Integer*/x, /*Integer*/y, /*DomNode*/parent, /*Object*/explodeSrc, /*String?*/orient, /*Array?*/padding )
        {
            dojo.widget.PopupContainerBase.prototype.open.call( this, x, y, parent, explodeSrc, orient, padding );
        },

        close: function(/*Boolean?*/force){
            var curTooltipDisplay = this._curr;
            if ( curTooltipDisplay != null )
                curTooltipDisplay.close( force );
        },
        
        _close: function(/*Boolean?*/force){
            dojo.widget.PopupContainerBase.prototype.close.call( this, force );
        },

        addNode: function( node, caption, mouseDownStop, showDelayOverride, captionSelectFunctionObject, captionSelectFunctionName, jsObj, jsUI, djEvtObj )
        {
            var tooltipDisplay = new jsObj.widget.PortalTooltipDisplay( node, caption, mouseDownStop, showDelayOverride, captionSelectFunctionObject, captionSelectFunctionName, this, jsUI, djEvtObj );
            this.connections.push( tooltipDisplay );
            return tooltipDisplay;
        },
        removeNodes: function( tooltipDisplayObjs )
        {
            if ( tooltipDisplayObjs == null || tooltipDisplayObjs.length == 0 ) return;
            
            for ( var i = 0 ; i < tooltipDisplayObjs.length; i++ )
            {
                tooltipDisplayObjs[i].destroy();
            }

            var clistNew = [];
            var clist = this.connections;
            for ( var i = 0 ; i < clist.length; i++ )
            {
                if ( ! clist[i].isDestroyed )
                    clistNew.push( clist[i] );
            }
            this.connections = clistNew;
        },

		checkSize: function()
        {
			// Override checkSize() in HtmlWidget.
			// checkSize() is called when the user has resized the browser window,
			// but that doesn't affect this widget (or this widget's children)
			// so it can be safely ignored
		},
		uninitialize: function()
        {
            var clist = this.connections;
            for ( var i = 0 ; i < clist.length; i++ )
            {
                clist[i].destroy();
            }
		}
	}
);

jetspeed.widget.PortalTooltipDisplay = function( connectNode, caption, mouseDownStop, showDelayOverride, captionSelectFunctionObject, captionSelectFunctionName, tooltipMgr, jsUI, djEvtObj )
{
    this.connectNode = connectNode;
    this.caption = caption;
    this.mouseDownStop = mouseDownStop;
    this.tooltipMgr = tooltipMgr;
    this.domNode = tooltipMgr.domNode;
    if ( showDelayOverride != null )
        this.showDelay = showDelayOverride;
    if ( captionSelectFunctionName != null && captionSelectFunctionObject != null )
    {
        this.captionSelectFncObj = captionSelectFunctionObject;
        this.captionSelectFnc = captionSelectFunctionObject[captionSelectFunctionName];
    }
    jsUI.evtConnect( "after", connectNode, "onmouseover", this, "_onMouseOver", djEvtObj );
    if ( mouseDownStop )
        jsUI.evtConnect( "after", connectNode, "onmousedown", this, "_onMouseDown", djEvtObj );
    
};
dojo.lang.extend( jetspeed.widget.PortalTooltipDisplay,
{
    // showDelay: Integer
    //      Number of milliseconds to wait after hovering over the object, before
    //      the tooltip is displayed.
    showDelay: 750,
    
    // hideDelay: Integer
    //      Number of milliseconds to wait after moving mouse off of the object (or
    //      off of the tooltip itself), before erasing the tooltip
    // NOTE: hideDelay should be less than showDelay
    hideDelay: 100,

    captionSelectFnc: null,

    _onMouseOver: function(e)
    {
        var jsObj = jetspeed;
        if ( jsObj.widget._movingInProgress || jsObj.ui.isWindowActionMenuOpen() )
        {
            //jsObj.stopEvent(e);
            return;
        }
        this._mouse = {x: e.pageX, y: e.pageY};
        this._abort = false;
        var haveSetTracking = true;
        if ( this._tracking )
        {
            haveSetTracking = false;
            //dojo.debug( "ERROR: tooltip should not be tracking: caption=" + this.caption );
        }
        this._tracking = true;
        var proceed = this.tooltipMgr._setCurrent( this );
        // Start tracking mouse movements, so we know when to cancel timers or erase the tooltip
        if ( proceed != this.tooltipMgr.startAbort )
        {
            if ( haveSetTracking )
                jsObj.ui.evtConnect( "after", document.documentElement, "onmousemove", this, "_onMouseMove" );
            this._onHover(e, proceed);
        }
        else if ( haveSetTracking )
        {
            this._tracking = false;
        }
    },

    _onMouseMove: function(e)
    {
        this._mouse = {x: e.pageX, y: e.pageY};

        if ( dojo.html.overElement(this.connectNode, e) || dojo.html.overElement(this.domNode, e) )
        {
            this._onHover(e);
        }
        else
        {
            // mouse has been moved off the element/tooltip
            // note: can't use onMouseOut to detect this because the "explode" effect causes
            // spurious onMouseOut events (due to interference from outline), w/out corresponding _onMouseOver
            this._onUnHover(e);
        }
    },
    _onMouseDown: function(e)
    {
        this._abort = true;
        jetspeed.stopEvent(e);
        this._onUnHover(e);
        if ( this.tooltipMgr.isShowingNow )
        {
            this.close();
        }
    },

    _onHover: function(e, startType)
    {
        if ( this._hover ) { return; }
        this._hover = true;

        // If the tooltip has been scheduled to be erased, cancel that timer
        // since we are hovering over element/tooltip again
        if ( this._hideTimer )
        {
            clearTimeout(this._hideTimer);
            delete this._hideTimer;
        }
        
        // If tooltip not showing yet then set a timer to show it shortly
        if ( ( ! this.tooltipMgr.isShowingNow || startType == this.tooltipMgr.startForce ) && ! this._showTimer )
        {
            this._showTimer = setTimeout( dojo.lang.hitch(this, "open"), this.showDelay );
        }
    },

    _onUnHover: function(e)
    {
        if ( ! this._hover && ! this._abort ) { return; }

        this._hover=false;

        if( this._showTimer )
        {
            clearTimeout(this._showTimer);
            delete this._showTimer;
        }

        if ( this.tooltipMgr.isShowingNow && ! this._hideTimer && ! this._abort )
        {
            this._hideTimer = setTimeout( dojo.lang.hitch(this, "close"), this.hideDelay );
        }
        
        // If we aren't showing the tooltip, then we can stop tracking the mouse now;
        // otherwise must track the mouse until tooltip disappears
        if ( ! this.tooltipMgr.isShowingNow && this._tracking )
        {
            jetspeed.ui.evtDisconnect( "after", document.documentElement, "onmousemove", this, "_onMouseMove" );
            this._tracking = false;
        }
    },

    open: function()
    {   // summary: display the tooltip; usually not called directly.
        var jsObj = jetspeed;
        if ( this.tooltipMgr.isShowingNow || this._abort ) { return; }
        if ( jsObj.widget._movingInProgress || jsObj.ui.isWindowActionMenuOpen() ) { this.close(); return; }

        var tCaption = this.caption;
        if ( this.captionSelectFnc != null )
        {
            var customCaption = this.captionSelectFnc.call( this.captionSelectFncObj );
            if ( customCaption )
                tCaption = customCaption;
        }
        if ( ! tCaption )
            this.close();
        else
        {
            this.domNode.innerHTML = tCaption;
            this.tooltipMgr.open( this, this._mouse.x, this._mouse.y, null, [this._mouse.x, this._mouse.y], "TL,TR,BL,BR", [10,15] );
        }
    },

    close: function(/*Boolean?*/force)
    {   // summary: hide the tooltip; usually not called directly.

        this.tooltipMgr._isCurrent( this, true );
        
        if ( this._showTimer )
        {
            clearTimeout(this._showTimer);
            delete this._showTimer;
        }

        if ( this._hideTimer )
        {
            clearTimeout(this._hideTimer);
            delete this._hideTimer;
        }

        if ( this._tracking )
        {
            jetspeed.ui.evtDisconnect( "after", document.documentElement, "onmousemove", this, "_onMouseMove" );
            this._tracking = false;
        }

        this.tooltipMgr._close(force);
        this._hover =  false;
    },

    _position: function()
    {
        this.tooltipMgr.move.call( this.tooltipMgr, this._mouse.x, this._mouse.y, [10,15], "TL,TR,BL,BR" );
    },

    destroy: function()
    {
        if ( this.isDestroyed ) return;
        this.close();
        var djEvtObj = dojo.event;
        var jsUI = jetspeed.ui;
        var connectNode = this.connectNode;
        jsUI.evtDisconnect( "after", connectNode, "onmouseover", this, "_onMouseOver", djEvtObj );
        if ( this.mouseDownStop )
            jsUI.evtDisconnect( "after", connectNode, "onmousedown", this, "_onMouseDown", djEvtObj );
        this.isDestroyed = true;
    }
}
);
