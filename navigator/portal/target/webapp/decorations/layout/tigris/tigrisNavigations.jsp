<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@page language="java" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="org.apache.jetspeed.portalsite.PortalSiteRequestContext"%>

<%@page import="org.apache.jetspeed.portalsite.Menu"%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.apache.jetspeed.portalsite.MenuElement"%>
<%@page import="org.apache.jetspeed.layout.JetspeedPowerTool"%>
<%@page import="javax.portlet.RenderRequest"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.apache.jetspeed.portalsite.MenuOption"%>
<%@page import="org.apache.jetspeed.portalsite.MenuSeparator"%>

<portlet:defineObjects/>

  <%!
  
	  /**
	   * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
	   */
  
      private final static int INITIAL_BUFFER = 1024;
      
      /**
        * orientation: How the individual menu elements are oriented.
        *              supported configuration represents the constants below.
        */
      private final static int LEFT_TO_RIGHT = 1;
      private final static int RIGHT_TO_LEFT = 2;
      private final static int TOP_TO_BOTTOM = 10;
      private final static int BOTTOM_TO_TOP = 11;

      /**
       * titleOrder: How the title element text appears
       *             supported configuration represents the constants below.
       */

      private final static int TITLE_ORDER_FIRST = 20;
      private final static int TITLE_ORDER_LAST = 21;
      private final static int TITLE_ORDER_NONE = 22;
      
      /**
       * menuStyle: currently only BREADCRUMBS_STYLE is understood.
       */
      private final static int BREADCRUMBS_STYLE = 0;

      private final static Map MenuOptionTypes = new HashMap();
      private final static int PAGE_TYPE = 0;
      private final static int FOLDER_TYPE = 1;
      private final static int LINK_TYPE = 2;
      private final static Map MenuElementTypes = new HashMap();
      private final static int OPTION_TYPE = 0;
      private final static int SEPARATOR_TYPE = 1;
      private final static int MENU_TYPE = 2;
      
      static {
        MenuOptionTypes.put("page",  new Integer(PAGE_TYPE));
        MenuOptionTypes.put("folder", new Integer(FOLDER_TYPE));
        MenuOptionTypes.put("link",   new Integer(LINK_TYPE));
        MenuElementTypes.put("option", new Integer(OPTION_TYPE));
        MenuElementTypes.put("separator", new Integer(SEPARATOR_TYPE));
        MenuElementTypes.put("menu", new Integer(MENU_TYPE));
      }
      
      /**
       *
       */
      private String getNormalizedString(String s)
      {
          return ((s == null) ? "" : s);    
      }
      
      
      private String getMenuElementOptionHTML(JetspeedPowerTool jpt,
                          MenuOption menuElmOption, Locale locale,
                          boolean addTarget)
      {
          final String TAB_INDENT_1 = "\t\t\t";
          final String TAB_INDENT_2 = "\t\t\t\t";
              
          StringBuffer sb = new StringBuffer(INITIAL_BUFFER);
          // get all our text
          String lnkTitle = getNormalizedString(menuElmOption.getTitle(locale));
          String lnkName  = getNormalizedString(menuElmOption.getShortTitle(locale));
          String lnkUrl = getNormalizedString(jpt.getAbsoluteUrl(menuElmOption.getUrl()));

          
          // now output accoding to menu type
          int menuType = ((Integer)MenuOptionTypes.get(menuElmOption.getType())).intValue();
          if (menuType == PAGE_TYPE)
          {
              sb.append(TAB_INDENT_1).
                 append("<div>\n").append(TAB_INDENT_2).
                 append("<a href=\"").
                 append(lnkUrl).append("\"").
                 append("class=\"LinkPage\" ").append("title=\"").
                 append(lnkTitle).append("\">").
                 append(lnkName).append("</a>\n").
                 append(TAB_INDENT_1).append("</div>\n");

          }
          else if (menuType == LINK_TYPE)
          {
              String lnkTarget = getNormalizedString(menuElmOption.getTarget());
              sb.append(TAB_INDENT_1).
              append("<div>\n").append(TAB_INDENT_2).
              append("<a href=\"").
              append(lnkUrl).append("\" ");
              if (addTarget)
              {
                  sb.append(" target=\"").append(lnkTarget).append("\"");
              }
              sb.append(" class=\"Link\" ").append(" title=\"").
              append(lnkTitle).append("\">").
              append(lnkName).append("</a>\n").
              append(TAB_INDENT_1).append("</div>\n");
          }
          else if (menuType == FOLDER_TYPE)
          {
              sb.append(TAB_INDENT_1).
              append("<div>\n").append(TAB_INDENT_2).
              append("<a href=\"").
              append(lnkUrl).append("\"").
              append("class=\"LinkFolder\" ").append("title=\"").
              append(lnkTitle).append("\">").
              append(lnkName).append("</a>\n").
              append(TAB_INDENT_1).append("</div>\n");
          }
          
          return sb.toString();
      } // E: getMenuOptionHTML()
      
      /**
       *  Helper method to create HTML content supporting the layout decoration
       *  in which it exists. This is mainly used to create the breadcrumbs.
       */
      
      private String getLinksNavigation(RenderRequest req, Menu menu, Locale locale,
                          int orientation, int titleOrder, 
                          int style, String delimiter)
      {
          final String TAB_INDENT_1 = "\t\t";
          final String TAB_INDENT_2 = "\t\t  ";
          final String TAB_INDENT_3 = "\t\t    ";

          JetspeedPowerTool jpt = (JetspeedPowerTool) req.getAttribute("jpt");
          StringBuffer sb = new StringBuffer(INITIAL_BUFFER);
        
          // get the delimiter right
          if ((null == delimiter) || delimiter.equalsIgnoreCase(""))
          {
             if ( style == BREADCRUMBS_STYLE)
             {
               delimiter = "&nbsp;&gt;&gt;&nbsp;";
             }
             else
             {
          	   delimiter = "&nbsp;";
             }
          }
        
          String menuTitle = getNormalizedString(menu.getTitle(locale));
          String menuName = getNormalizedString(menu.getShortTitle(locale));
          
          sb.append(TAB_INDENT_1).
             append("<div class=\"FolderList\"><!-- B: div class FolderList -->\n");
        
          //check title
          if (titleOrder == TITLE_ORDER_FIRST)
          {
            if (orientation == LEFT_TO_RIGHT)
            {
                sb.append(TAB_INDENT_2).
                   append("<span title=\"").append(menuTitle).append("\">").
                   append(menuName).append("&nbsp;</span>\n");
            }
            else if (orientation == TOP_TO_BOTTOM)
            {
                sb.append(TAB_INDENT_2).append("<div class=\"pagetitle\" title=\"").
                   append(menuTitle).append("\">").
                   append(menuName).append(TAB_INDENT_2).
                   append("</div>\n");
            }
          }

          //start the menu
          Iterator menuIterator = menu.getElements().iterator();
          int i = 0;
          int size = menu.getElements().size();
          
          while(menuIterator.hasNext())
          {
             i++;
             Object menuElmObj = menuIterator.next();
             int menuElmType = ((Integer)MenuElementTypes.get(
                                            ((MenuElement)menuElmObj).getElementType())).intValue();
             
             if (menuElmType == OPTION_TYPE)
             {
                 MenuOption menuElm = (MenuOption) menuElmObj;
                 int menuType = ((Integer)MenuOptionTypes.get(menuElm.getType())).intValue();
  
                 // get all our text
                 String lnkTitle = getNormalizedString(menuElm.getTitle(locale));
                 String lnkName  = getNormalizedString(menuElm.getShortTitle(locale));
                 String lnkUrl = getNormalizedString(jpt.getAbsoluteUrl(menuElm.getUrl()));
 
                 if ( (style != BREADCRUMBS_STYLE) || 
                       !(menuType == PAGE_TYPE) ||
                       (i < size) )
                 {
                   //start element
                   if (orientation == LEFT_TO_RIGHT)
                   {
                        sb.append(TAB_INDENT_2).append("<span>");
                   }
                   else if (orientation == TOP_TO_BOTTOM)
                   {
                        sb.append(TAB_INDENT_2).append("<div><!-- S: menu element -->\n");
                   }
                   
                   // add the content
                   if (menuType == PAGE_TYPE)
                   {
                       sb.append("<a href=\"").
                            append(lnkUrl).append("\"").
                            append(" class=\"LinkPage\" title=\"").
                              append(lnkTitle).append("\">").
                          append(lnkName).
                          append("</a>");
                   }
                   else if (menuType == LINK_TYPE)
                   {
                       sb.append("<a href=\"").
                            append(lnkUrl).append("\"").
                            append(" class=\"Link\" title=\"").
                         append(lnkTitle).append("\">").
                          append(lnkName).
                         append("</a>");
                       
                   }
                   else if (menuType == FOLDER_TYPE)
                   {
                       sb.append("<a href=\"").
                            append(lnkUrl).append("\"").
                            append(" class=\"LinkFolder\" title=\"").
                          append(lnkTitle).append("\">").
                            append(lnkName).
                          append("</a>");
                   }
                   else
                   {
                       sb.append("<a href=\"").
                            append(lnkUrl).append("\"").
                            append(" title=\"").
                          append(lnkTitle).append("\">").
                            append(lnkName).
                          append("</a>");
                   }
                   
                   if (orientation == LEFT_TO_RIGHT)
                   {
                     sb.append(delimiter).append("</span>\n");
                   }
                   else if (orientation == TOP_TO_BOTTOM)
                   {
                     sb.append(TAB_INDENT_2).append("</div>\n");
                   }
                 }
                 else
                 {
                     if (orientation == LEFT_TO_RIGHT)
                     {
                       sb.append(TAB_INDENT_2).append("<span title=\"").
                          append(lnkTitle).append("\">").
                          append(lnkName).append("&nbsp;").append("</span>\n");
                     }
                     else if (orientation == TOP_TO_BOTTOM)
                     {
                         sb.append(TAB_INDENT_2).append("<div title=\"").
                            append(lnkTitle).append("\">").
                            append(lnkName).append("</div>\n");
                     }
                     
                 } // Not BREADCRUMB_STYLE etc.
             } // option type
             else if(menuElmType == SEPARATOR_TYPE)
             {
                 MenuSeparator menuElm = (MenuSeparator) menuElmObj;
                 String sepTitle = menuElm.getTitle(locale);
                 if (null == sepTitle)
                     sepTitle = "";
                 String sepText  = menuElm.getText(locale);
                 if (null == sepText)
                     sepText = "";
                 if (orientation == LEFT_TO_RIGHT)
                 {
                   sb.append(TAB_INDENT_2).append("<span title=\"").append(sepTitle).append("\">").
                      append(sepText).append(delimiter).append("</span>\n");
                 }
                 else if (orientation == TOP_TO_BOTTOM)
                 {
                     sb.append(TAB_INDENT_2).append("<div class=\"pagetitle\" title=\"").
                        append(sepTitle).append("\">").
                        append(sepText).append("</div>\n");
                 }
               
             }
          } // while menu items
          if (titleOrder == TITLE_ORDER_LAST)
          {
              if (orientation == LEFT_TO_RIGHT)
              {
                sb.append(TAB_INDENT_2).append("<span title=\"").append(menuTitle).append("\">").
                   append(menuName).append("&nbsp;").append("</span>\n");
                  
              }
              else
              {
                sb.append(TAB_INDENT_2).append("<div class=\"pagetitle\" title=\"").
                   append(menuTitle).append("\">").
                   append(menuName).append("</div>\n");
              }
          }
  
          sb.append("\t\t</div><!-- B: div class FolderList -->\n");
          return sb.toString();
          
      } // getLinksNavigation
      
      /*
      *  getLinksWithIconNavigation
      *     This is for tigris, assumes a table already present.
      */
      private String getLinksWithIconNavigation(RenderRequest req, Menu menu, Locale locale,
                          int orientation)
      {
         JetspeedPowerTool _jpt = (JetspeedPowerTool) req.getAttribute("jpt");
         StringBuffer sb = new StringBuffer(INITIAL_BUFFER);

         String TR_TD_START = "\n\t\t\t <tr>\n \t\t\t\t <td>\n";
         String TR_TD_END = "\n\t\t\t\t </td>\n \t\t\t </tr>\n";
         
         final String DIV_TOOLGROUP = "\t\t\t\t\t<div class=\"toolgroup\"><!-- div class toolgroup -->\n";
         final String DIV_BODY = "\t\t\t\t\t\t<div class=\"body\"><!-- div class body -->\n";
         final String DIV_FOLDERLIST = "\t\t\t\t\t\t\t\t<div class=\"FolderList\"><!-- div class FolderList -->\n";
         
         boolean rowStarted = false;
         boolean elmStarted = false;

         //start the menu
         Iterator menuIteartor = menu.getElements().iterator();
         int i = 0;
         
         while(menuIteartor.hasNext())
         {
            i++;
            Object menuElmObj = menuIteartor.next();
            int menuElmType = ((Integer)MenuElementTypes.get(
                                           ((MenuElement) menuElmObj).
                                              getElementType())).intValue();
            
            if (menuElmType == OPTION_TYPE)
            {
                MenuOption menuElmOption = (MenuOption) menuElmObj;
                //start the xhtml/html elements
                if (!rowStarted)
                {
                  sb.append(TR_TD_START).
                     append(DIV_TOOLGROUP).
                     append(DIV_BODY).
                     append(DIV_FOLDERLIST);
                }
                else if (!elmStarted)
                {
                    sb.append(DIV_BODY).
                       append(DIV_FOLDERLIST);
                    
                }
                // get all our text
                String lnkTitle = menuElmOption.getTitle(locale);
                if (null == lnkTitle)
                    lnkTitle = "";
                String lnkName  = menuElmOption.getShortTitle(locale);
                if (null == lnkName)
                    lnkName = "";
                String lnkUrl = _jpt.getAbsoluteUrl(menuElmOption.getUrl());
                if (null == lnkUrl)
                    lnkUrl = "";
                // now output accoding to menu type
                int menuType = ((Integer)MenuOptionTypes.get(menuElmOption.getType())).intValue();
                if (menuType == PAGE_TYPE)
                {
                    sb.append("\t\t\t\t\t\t\t<div>\n\t\t\t\t\t\t\t\t<a href=\"").
                       append(lnkUrl).append("\"").
                       append("class=\"LinkPage\" ").append("title=\"").
                       append(lnkTitle).append("\">").
                       append(lnkName).append("</a>\n\t\t\t\t\t\t\t\t</div>\n");

                }
                else if (menuType == LINK_TYPE)
                {
                    String lnkTarget = menuElmOption.getTarget();
                    if (null == lnkTarget)
                        lnkTarget = "";
                    sb.append("\t\t\t\t\t\t\t<div>\n\t\t\t\t\t\t\t\t<a href=\"").
                    append(lnkUrl).append("\"").
                    append(" target=\"").append(lnkTarget).append("\" ").
                    append("class=\"Link\" ").append("title=\"").
                    append(lnkTitle).append("\">").
                    append(lnkName).append("</a>\n\t\t\t\t\t\t\t\t</div>\n");
                }
                else if (menuType == FOLDER_TYPE)
                {
                    sb.append("\t\t\t\t\t\t\t<div>\n\t\t\t\t\t\t\t\t<a href=\"").
                    append(lnkUrl).append("\"").
                    append("class=\"LinkFolder\" ").append("title=\"").
                    append(lnkTitle).append("\">").
                    append(lnkName).append("</a>\n\t\t\t\t\t\t\t\t</div>\n");
                    
                }
                rowStarted = true;
                elmStarted = true;
            }
            else if (menuElmType == MENU_TYPE)
            {
                if (!rowStarted)
                {
                    sb.append(TR_TD_START).
                    append(DIV_TOOLGROUP).
                    append(DIV_BODY).
                    append(DIV_FOLDERLIST);
                    
                }
                else if (!elmStarted)
                {
                    sb.append(DIV_BODY).
                    append(DIV_FOLDERLIST);
                }
                
                sb.append(
                        getNestedLinksIconNavigation(req,(Menu) menuElmObj, locale, orientation)
                        );
                //TODO includeNested
                rowStarted = true;
                elmStarted = true;
            }
            else if (menuElmType == SEPARATOR_TYPE)
            {
                MenuSeparator menuElmSeparator = (MenuSeparator) menuElmObj;
                String sepTitle = menuElmSeparator.getTitle(locale);
                if (null == sepTitle)
                    sepTitle = "";
                String sepText  = menuElmSeparator.getText(locale);
                if (null == sepText)
                    sepText = "";
                if (!rowStarted)
                {
                    sb.append(TR_TD_START).
                    append(DIV_TOOLGROUP);
                }
                else if (elmStarted)
                {
                    sb.append("\t\t\t\t\t\t\t\t</div>\n").
                       append("\t\t\t\t\t\t\t</div>\n");
                }
                sb.append("\t\t\t\t\t\t\t\t<div class=\"label\">").
                   append(sepText).append("</div>\n");
                rowStarted = true;
                elmStarted = false; //already outputed
            }
            
         } // while loop
         
         if (elmStarted)
         {
             sb.append("\t\t\t\t\t\t\t\t</div>\n").
             append("\t\t\t\t\t\t\t</div>\n");
             
         }
         else if (rowStarted)
         {
             sb.append("</div>\n").
                append(TR_TD_END);
         }
         return sb.toString();
      } //E: getLinksWithIconNavigation

      
      private String getNestedLinksIconNavigation(RenderRequest req, Menu menu, Locale locale,
              int orientation)
      {
         JetspeedPowerTool jpt = (JetspeedPowerTool) req.getAttribute("jpt");
         String menuTitle = menu.getTitle(locale);
         String menuName = menu.getShortTitle(locale);
         StringBuffer sb = new StringBuffer(INITIAL_BUFFER);
         if ((null != menuTitle) && !(menuTitle.equalsIgnoreCase("")))
         {
             sb.append("\t\t\t\t\t\t\t\t\t").
                append("<div class=\"pagetitle\" ").
                append("title=\"").append(menuTitle).
                append("\">").append(menuName).
                append(menuName).append("</div>\n");
         }

         sb.append("\t\t\t\t\t\t\t\t\t").append("<div><!-- S: nested menu -->\n");

         //start the menu
         Iterator menuIteartor = menu.getElements().iterator();
         int i = 0;
         
         while(menuIteartor.hasNext())
         {
             i++;
             Object menuElmObj = menuIteartor.next();
             int menuElmType = ((Integer)MenuElementTypes.get(
                                            ((MenuElement) menuElmObj).
                                               getElementType())).intValue();
             if (menuElmType == OPTION_TYPE)
             {
                 sb.append(
                     getMenuElementOptionHTML(jpt,(MenuOption)menuElmObj,locale,true));
             }
             else if (menuElmType == MENU_TYPE)
             {
                 sb.append(
                         getNestedLinksIconNavigation(req,(Menu)menuElmObj,locale,orientation)
                         );
             }
             else if (menuElmType == SEPARATOR_TYPE)
             {
                 MenuSeparator menuElmSeparator = (MenuSeparator) menuElmObj;
                 String sepTitle = menuElmSeparator.getTitle(locale);
                 if (null == sepTitle)
                     sepTitle = "";
                 String sepText  = menuElmSeparator.getText(locale);
                 if (null == sepText)
                     sepText = "";
                 sb.append("\t\t\t\t\t\t\t\t").
                    append("<div class=\"pagetitle\" ").
                    append(" title=\"").append(sepTitle).append("\">").
                    append(sepText).append("</div>\n");
                 
             }
         } //while

         sb.append("\t\t\t\t\t\t\t\t\t").append("</div><!-- E: nested menu -->\n");

         return sb.toString();

      } //E: getNestedLinksIconNavigation
      
      /**
       *  Helper method to create HTML content supporting page TABS
       *  
       */
      private String getTabsNavigationContent(RenderRequest req, Menu menu, Locale locale,int orientation)
      {

          final String TAB_INDENT_1 = "\t\t";
          final String TAB_INDENT_2 = "\t\t  ";
          final String TAB_INDENT_3 = "\t\t    ";

          JetspeedPowerTool _jpt = (JetspeedPowerTool) req.getAttribute("jpt");
          PortalSiteRequestContext site = (PortalSiteRequestContext) req.getAttribute("psrc");
          
          StringBuffer sb = new StringBuffer(INITIAL_BUFFER);
          
          sb.append(TAB_INDENT_1);
          sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n").
             append(TAB_INDENT_2).append("<tr>\n");
          
          Iterator menuIteartor = menu.getElements().iterator();
          int i = 0;
          
          while(menuIteartor.hasNext())
          {
              i++;
              Object menuElmObj = menuIteartor.next();
              int menuElmType = ((Integer)MenuElementTypes.get(
                                             ((MenuElement) menuElmObj).
                                                getElementType())).intValue();
              
              if (menuElmType != OPTION_TYPE)
              {
                  continue;
              }
              MenuOption menuElm = (MenuOption) menuElmObj;
              String tabTitle = getNormalizedString(menuElm.getTitle(locale));
              String tabName = getNormalizedString(menuElm.getShortTitle(locale));
              
              if (orientation == LEFT_TO_RIGHT)
              {
                  if (menuElm.isSelected(site))
                  {
                      sb.append(TAB_INDENT_3).
                         append("<td class=\"LTabLeft\" nowrap=\"nowrap\">&nbsp;</td>\n").
                         append(TAB_INDENT_3).
                         append("<td class=\"LTab\" align=\"center\" valign=\"middle\" nowrap=\"nowrap\" title=\"").
                         append(tabTitle).append("\">").append(tabName).append("</td>\n").
                         append(TAB_INDENT_3).
                         append("<td class=\"LTabRight\"  nowrap=\"nowrap\">&nbsp;</td>\n");
                      
                  }
                  else
                  {
                      String tabUrl = _jpt.getAbsoluteUrl(menuElm.getUrl());
                      sb.append(TAB_INDENT_3).
                      append("<td class=\"LTabLeftLow\" nowrap=\"nowrap\">&nbsp;</td>\n").
                      append(TAB_INDENT_3).
                      append("<td class=\"LTabLow\" align=\"center\" ").
                                append("valign=\"middle\" nowrap=\"nowrap\" title=\"").
                                append(tabTitle).append("\">").
                      append("<a href=\"").append(tabUrl).append("\">").
                        append(tabName).
                      append("</a>").append("</td>\n").
                      append(TAB_INDENT_3).
                      append("<td class=\"LTabRightLow\"  nowrap=\"nowrap\">&nbsp;</td>\n");
                  }
              }
              else
              {
                  
              }
          }
          sb.append(TAB_INDENT_2).append("</tr>\n").
             append(TAB_INDENT_1).
             append("</table>");
          
          return sb.toString();
      }
  %>

  
