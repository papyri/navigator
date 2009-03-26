/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * author: Steve Milek
 */

dojo.provide("jetspeed.widget.EditorTable");

dojo.require("dojo.widget.SortableTable");

jetspeed.widget.EditorTable = function()
{
    dojo.widget.SortableTable.call(this);
    this.widgetType = "EditorTable";
    this.headerSortUpClass="selectedUp";
	this.headerSortDownClass="selectedDown";
};

dojo.inherits( jetspeed.widget.EditorTable, dojo.widget.SortableTable);

dojo.lang.extend( jetspeed.widget.EditorTable, {
    saveWarningDialogWidgetId: null,
    
    /* derived class protocol - attach-to or override this methods */
    updateEditor: function( rowData )
    {

    },
    saveEntrySubmit: function( selectedRowData, /* boolean */ removeEntry )
    {

    },
    clearAndDisableEditor: function()
    {

    },
    getNewEntryPrototype: function()
    {
        return {};
    },
    
    /* base class protocol */
    render: function(bDontPreserve)
    {
        bDontPreserve = true;    // for EditorTable, all calls to render should not reset data (i.e. call SortableTable.parseDataFromTable())
        jetspeed.widget.EditorTable.superclass.render.call( this, bDontPreserve );
    },

    buildRendering: function(args, frag)
    {
        jetspeed.widget.EditorTable.superclass.buildRendering.call( this, args, frag );
        if ( args.templateCssPath )
        {
            dojo.html.insertCssFile( dojo.uri.dojoUri(args.templateCssPath), document, true );
        }
    },


    /* methods */
    hasRowChanged: function( rowData )
    {
        if ( ! rowData ) return false;
        var tId = rowData.Id;
        var masterData = this.getData( this.js_masterdata, tId );
        var changed = ( rowData.__isNew || rowData.__isModified ) ? true : false;
        if ( ! changed )
        {
            for ( var slotKey in masterData )
            {
                if ( rowData[ slotKey ] != masterData[ slotKey ] )
                {
                    //dojo.debug( "slot " + slotKey + " changed - old: " + masterData[ slotKey ] + " new: " + rowData[ slotKey ] ) ;
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    },
    entryIsNew: function( rowData )
    {
        if ( ! rowData ) return false;
        return rowData.__isNew ;
    },
    setModified: function( rowData )
    {
        if ( ! rowData ) return;
        rowData.__isModified = true;
    },
    getSelectedRow: function()
    {
        if ( this.selected && this.selected.length == 1 )
        {
            var tId = this.selected[0].Id;
            var data = this.getData( this.data, tId );
            return data;
        }
        return null;
    },
    getData: function( tableWidgetData, matchId )
    {
        if ( ! tableWidgetData ) return null ;
        for( var i = 0 ; i < tableWidgetData.length; i++ )
        {
            if ( tableWidgetData[ i ].Id == matchId )
            {
                return tableWidgetData[ i ];
            }
        }
        return null;
    },
    getDataIndex: function( tableWidgetData, matchId )
    {
        if ( ! tableWidgetData ) return -1;
        for( var i = 0 ; i < tableWidgetData.length; i++ )
        {
            if ( tableWidgetData[ i ].Id == matchId )
            {
                return i;
            }
        }
        return -1;
    },
    processTableRowEvent: function( e )
    {
        var row = dojo.html.getParentByType( e.target, "tr" );
        var rowData = this.getObjectFromRow( row );
        this.updateEditor( rowData );
    },

    checkForChanges: function()
    {
        var selectedRowData = this.getSelectedRow();
    
        if ( ! selectedRowData ) return false;
        var hasChanged = this.hasRowChanged( selectedRowData );
        return ( ! hasChanged ? false : ( selectedRowData.__isNew ? "new" : "modified" ) );
    },

    updateClonedData: function( fromData, toData )
    {
        if ( ! fromData || ! toData ) return;
        for ( var slotKey in fromData )
        {
            toData[ slotKey ] = fromData[ slotKey ];
        }
    },

    printSelection: function()
    {
        if ( this.selected && this.selected.length == 1 )
            dojo.debug( this.widgetId + " selection: " + jsDebugShallow( this.selected[0] ) );
        else
            dojo.debug( this.widgetId + " selection: null" );
    },

    newEntry: function()
    {
        if ( this.saveWarningDialogWidgetId )
        {
            if ( this.checkForChanges() )
            {
                dojo.widget.byId( this.saveWarningDialogWidgetId ).show();
                return;
            }
        }
    
        var newEntry = dojo.lang.shallowCopy( this.getNewEntryPrototype() );
        var tId = 1;
        for ( var i = 0 ; i < this.js_masterdata.length; i++ )
        {
            if ( this.js_masterdata[i].Id >= tId )
                tId = this.js_masterdata[i].Id + 1;
        }
        newEntry.Id = tId;
        this.js_masterdata.push( dojo.lang.shallowCopy( newEntry ) );
        newEntry.__isNew = true;
        this.data.push( newEntry );
        this.selected = [ dojo.lang.shallowCopy( newEntry ) ];

        this.render(true);
        this.showSelections();

        this.updateEditor( newEntry );
    },

    deleteEntry: function()
    {
        var selectedRowData = this.getSelectedRow();
    
        if ( ! selectedRowData ) return;
        var tId = selectedRowData.Id;
    
        if ( ! selectedRowData.__isNew  )
            this.saveEntrySubmit( selectedRowData, true );

        var tIndex = this.getDataIndex( this.js_masterdata, tId );
        if ( tIndex != -1 )
            this.js_masterdata.splice( tIndex, 1 );

        tIndex = this.getDataIndex( this.data, tId );
        if ( tIndex != -1 )
            this.data.splice( tIndex, 1 );

        this.selected = [];
        this.render(true);
        this.showSelections();
    
        this.clearAndDisableEditor();
    },
    saveEntry: function()
    {
        var selectedRowData = this.getSelectedRow();

        if ( ! selectedRowData ) { dojo.raise( "saveEntry can't find selectedRowData" ) ; return; } 
        var masterData = this.getData( this.js_masterdata, selectedRowData.Id );
        if ( ! masterData ) { dojo.raise( "saveEntry can't find masterdata" ) ; return; } 

        this.saveEntrySubmit( selectedRowData );

        //delete masterData.__isNew;
        //delete masterData.__isModified;
        delete selectedRowData.__isNew;
        delete selectedRowData.__isModified;
        this.updateClonedData( selectedRowData, masterData );
        this.updateClonedData( selectedRowData, this.selected[0] );
        this.updateEditor( selectedRowData );
    },

    revertEntry: function()
    {
        var selectedRowData = this.getSelectedRow();
    
        if ( ! selectedRowData ) return;

        if ( selectedRowData.__isNew )
        {
            deleteEntry();
        }
        else
        {
            delete selectedRowData.__isModified;
            var masterData = this.getData( this.js_masterdata, selectedRowData.Id );
            if ( ! masterData ) return;
            this.updateClonedData( masterData, selectedRowData );
            this.updateClonedData( masterData, this.selected[0] );
            this.render(true);
            this.showSelections();
            this.updateEditor( masterData );
        }
    },

    okToChangeSelectionOrExit: function( invocation )   // listSelectionChangeOk
    {
        if ( this.checkForChanges() )
        {
            if ( this.saveWarningDialogWidgetId )
            {
                dojo.widget.byId( this.saveWarningDialogWidgetId ).show();
                return false;
            }
            else
            {
                this.saveEntry();
            }
        }
        if ( invocation != null )
            invocation.proceed();
        return true;
    },

    dojoDebugTableData: function()
    {
        dojo.debug( debugTableData() );
    },
    debugTableData: function()
    {
        var tTableWidget = this;
        // format: js_masterdata[index][key]=value (data[index][key]
        buff = tTableWidget.widgetId + " data:" + "\r\n";
        for ( var masterDataIndex = 0 ; masterDataIndex < tTableWidget.js_masterdata.length ; masterDataIndex++ )
        {
            buff += "[" + masterDataIndex + "]" + "\r\n";
            var slotsUsed = new Object();
            for ( var slotKey in tTableWidget.js_masterdata[masterDataIndex] )
            {
                buff += "   " + slotKey + "=" + tTableWidget.js_masterdata[masterDataIndex][ slotKey ];
                if ( slotKey == "__isModified" || slotKey == "__isNew" )
                    buff += "\r\n";
                else
                {
                    var dataVal = null;
                    if ( tTableWidget.data.length <= masterDataIndex )
                       buff += " <out-of-bounds>" + "\r\n";
                    else
                    {
                       dataVal = tTableWidget.data[masterDataIndex][ slotKey ];
                       buff += " (" + ( dataVal == null ? "null" : dataVal ) + ")" + "\r\n";
                    }
                }
            }
        }
        return buff;
    }
});
