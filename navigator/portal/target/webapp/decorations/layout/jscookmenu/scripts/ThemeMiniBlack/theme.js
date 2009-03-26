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
var cmThemeMiniBlack =
{
  	// main menu display attributes
  	//
  	// Note.  When the menu bar is horizontal,
  	// mainFolderLeft and mainFolderRight are
  	// put in <span></span>.  When the menu
  	// bar is vertical, they would be put in
  	// a separate TD cell.

  	// HTML code to the left of the folder item
  	mainFolderLeft: '',
  	// HTML code to the right of the folder item
  	mainFolderRight: '',
	// HTML code to the left of the regular item
	mainItemLeft: '',
	// HTML code to the right of the regular item
	mainItemRight: '',

	// sub menu display attributes

	// HTML code to the left of the folder item
	folderLeft: '',
	// HTML code to the right of the folder item
	folderRight: '',
	// HTML code to the left of the regular item
	itemLeft: '',
	// HTML code to the right of the regular item
	itemRight: '',
	// cell spacing for main menu
	mainSpacing: 0,
	// cell spacing for sub menus
	subSpacing: 0,
	// auto dispear time for submenus in milli-seconds
	delay: 100
};

// horizontal split, used only in sub menus
var cmThemeMiniBlackHSplit = [_cmNoAction, '<td colspan="3" style="height: 3px; overflow: hidden"><div class="ThemeMiniBlackMenuSplit"></div></td>'];
// horizontal split, used only in main menu
var cmThemeMiniBlackMainHSplit = [_cmNoAction, '<td colspan="3"><div class="ThemeMiniBlackMenuSplit"></div></td>'];
// vertical split, used only in main menu
var cmThemeMiniBlackMainVSplit = [_cmNoAction, '<div class="ThemeMiniBlackMenuVSplit"></div>'];
