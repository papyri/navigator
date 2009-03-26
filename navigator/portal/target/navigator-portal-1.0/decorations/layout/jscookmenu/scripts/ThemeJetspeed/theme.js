/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

// directory of where all the images are
var cmThemeJetspeedBase = 'decorations/layout/jscookmenu/images/ThemeJetspeed/';

var cmThemeJetspeed =
{
  	// main menu display attributes
  	//
  	// Note.  When the menu bar is horizontal,
  	// mainFolderLeft and mainFolderRight are
  	// put in <span></span>.  When the menu
  	// bar is vertical, they would be put in
  	// a separate TD cell.

  	// HTML code to the left of the folder item
  	mainFolderLeft: '<img alt="" src="' + cmThemeJetspeedBase + 'blank.gif">',
  	// HTML code to the right of the folder item
  	mainFolderRight: '<img alt="" src="' + cmThemeJetspeedBase + 'arrowdown.gif">',
	// HTML code to the left of the regular item
	mainItemLeft: '<img alt="" src="' + cmThemeJetspeedBase + 'blank.gif">',
	// HTML code to the right of the regular item
	mainItemRight: '<img alt="" src="' + cmThemeJetspeedBase + 'blank.gif">',

	// sub menu display attributes

	// HTML code to the left of the folder item
	folderLeft: '<img alt="" src="' + cmThemeJetspeedBase + 'blank.gif">',
	// HTML code to the right of the folder item
	folderRight: '<img alt="" src="' + cmThemeJetspeedBase + 'arrow.gif">',
	// HTML code to the left of the regular item
	itemLeft: '<img alt="" src="' + cmThemeJetspeedBase + 'blank.gif">',
	// HTML code to the right of the regular item
	itemRight: '<img alt="" src="' + cmThemeJetspeedBase + 'blank.gif">',
	// cell spacing for main menu
	mainSpacing: 0,
	// cell spacing for sub menus
	subSpacing: 0,
	// auto dispear time for submenus in milli-seconds
	delay: 500
};

// for sub menu horizontal split
var cmThemeJetspeedHSplit = [_cmNoAction, '<td colspan="3" style="height: 5px; overflow: hidden"><div class="ThemeJetspeedMenuSplit"></div></td>'];
// for vertical main menu horizontal split
var cmThemeJetspeedMainHSplit = [_cmNoAction, '<td colspan="3" style="height: 5px; overflow: hidden"><div class="ThemeJetspeedMenuSplit"></div></td>'];
// for horizontal main menu vertical split
var cmThemeJetspeedMainVSplit = [_cmNoAction, '|'];
