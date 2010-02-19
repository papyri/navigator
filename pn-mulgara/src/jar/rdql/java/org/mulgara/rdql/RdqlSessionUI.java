/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 */

package org.mulgara.rdql;

/**
 * Swing based RDQL session command line shell.
 *
 * @created 2004-01-15
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:22 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
import java.beans.*;
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.apache.log4j.*;

import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;

public class RdqlSessionUI extends JScrollPane implements Runnable, KeyListener,
    ActionListener, PropertyChangeListener {

  /**
   * The logging category to log to
   */
  private final static Logger log = Logger.getLogger(RdqlSessionUI.class);

  /**
   * Inputstream to take user input.
   */
  private InputStream in;

  /**
   * Outputstream to display user output.
   */
  private PrintStream out;

  /**
   * Used to pipe input.
   */
  private InputStream inPipe;

  /**
   * Used to pipe output.
   */
  private OutputStream outPipe;

  /**
   * The RDQL session to send queries and used to send results.
   */
  private RdqlSession rdqlSession;

  /**
   * The list of history items.
   */
  private ArrayList history = new ArrayList();

  /**
   * Current index into the history.
   */
  private int historyIndex = 0;

  /**
   * Current cursor position.
   */
  private int cursorPosition = 0;

  /**
   * The UI widget for displaying all text.
   */
  private JTextPane text;

  /**
   * The default styled document.
   */
  private DefaultStyledDocument doc;

  /**
   * Create a new UI representation.
   *
   * @param newRdqlSession the rdql session to call when we receive commands and
   *   when we want to display them.
   */
  public RdqlSessionUI(RdqlSession newRdqlSession) {

    super();

    rdqlSession = newRdqlSession;
    doc = new DefaultStyledDocument();
    text = new PasteablePane(doc);
    text.setFont(new Font("Monospaced", Font.PLAIN, 12));
    text.setMargin(new Insets(5, 5, 5, 5));
    text.addKeyListener(this);
    setViewportView(text);
    UIManager.addPropertyChangeListener(this);

    outPipe = new PipedOutputStream();
    try {

      in = new PipedInputStream((PipedOutputStream) outPipe);
    }
    catch (IOException e) {

      log.error("Error creating input stream", e);
    }

    PipedOutputStream pout = new PipedOutputStream();
    out = new PrintStream(pout);
    try {

      inPipe = new PipedInputStream(pout);
    }
    catch (IOException e) {

      log.error("Error creating input pipe", e);
    }

    // Start the inpipe watcher
    new Thread(this).start();
    requestFocus();
  }

  public void requestFocus() {

    super.requestFocus();
    text.requestFocus();
  }

  /**
   * Handle key pressed event.
   *
   * @param e the key that was pressed.
   */
  public void keyPressed(KeyEvent e) {

    switch (e.getKeyCode()) {

      // Enter pressed
      case (KeyEvent.VK_ENTER):
        if (e.getID() == KeyEvent.KEY_PRESSED) {

          enterPressed();
          cursorPosition = textLength();
          text.setCaretPosition(cursorPosition);
        }
        e.consume();
        text.repaint();
      break;

      // Up history
      case (KeyEvent.VK_UP):
        if (e.getID() == KeyEvent.KEY_PRESSED) {

          historyUp();
        }
        e.consume();
      break;

      // Down history
      case (KeyEvent.VK_DOWN):
        if (e.getID() == KeyEvent.KEY_PRESSED) {

          historyDown();
        }
        e.consume();
      break;

      // Left or delete.
      case (KeyEvent.VK_LEFT):
      case (KeyEvent.VK_DELETE):

        if (text.getCaretPosition() <= cursorPosition) {

          e.consume();
        }
      break;

      // Go right.
      case (KeyEvent.VK_RIGHT):

        if (text.getCaretPosition() < cursorPosition) {

          // move caret first!
        }
        text.repaint();
      break;

      // Control-A go to start of line.
      case (KeyEvent.VK_A):
        if ( (e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          text.setCaretPosition(cursorPosition);
          e.consume();
        }
      break;

      // Control-E go to end of line.
      case (KeyEvent.VK_E):
        if ( (e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          text.setCaretPosition(textLength());
          e.consume();
        }
      break;

      // Control-U remove line
      case (KeyEvent.VK_U):
        if ( (e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          replaceText("", cursorPosition, textLength());
          historyIndex = 0;
          e.consume();
        }
      break;

      // Home go to start of line
      case (KeyEvent.VK_HOME):
        text.setCaretPosition(cursorPosition);
        e.consume();
      break;

      // Go to end of line
      case (KeyEvent.VK_END):
        text.setCaretPosition(textLength());
        e.consume();
      break;

      // Ignore modifiers
      case (KeyEvent.VK_ALT):
      case (KeyEvent.VK_CAPS_LOCK):
      case (KeyEvent.VK_CONTROL):
      case (KeyEvent.VK_ESCAPE):
      case (KeyEvent.VK_F1):
      case (KeyEvent.VK_F2):
      case (KeyEvent.VK_F3):
      case (KeyEvent.VK_F4):
      case (KeyEvent.VK_F5):
      case (KeyEvent.VK_F6):
      case (KeyEvent.VK_F7):
      case (KeyEvent.VK_F8):
      case (KeyEvent.VK_F9):
      case (KeyEvent.VK_F10):
      case (KeyEvent.VK_F11):
      case (KeyEvent.VK_F12):
      case (KeyEvent.VK_INSERT):
      case (KeyEvent.VK_META):
      case (KeyEvent.VK_PAUSE):
      case (KeyEvent.VK_PRINTSCREEN):
      case (KeyEvent.VK_SHIFT):
      case (KeyEvent.VK_SCROLL_LOCK):

        // Do nothing.

      break;

      // Handle normal characters
      default:

        if ( (e.getModifiers() & (InputEvent.ALT_MASK | InputEvent.CTRL_MASK |
            InputEvent.META_MASK)) == 0) {

          if (text.getCaretPosition() < cursorPosition) {

            text.setCaretPosition(textLength());
          }
          text.repaint();
        }

        // Handle back space
        if (e.paramString().indexOf("Backspace") != -1) {

          if (text.getCaretPosition() <= cursorPosition) {

            e.consume();
            break;
          }
        }
      break;
    }
  }

  public void keyTyped(KeyEvent e) {

    if (e.paramString().indexOf("Backspace") != -1) {

      if (text.getCaretPosition() <= cursorPosition) {

        e.consume();
      }
    }
  }

  public void keyReleased(KeyEvent e) {

    // Do nothing.
  }

  private synchronized void type(KeyEvent e) {

    // Do nothing.
  }

  /**
   * Returns the length of the current text buffer.
   *
   * @return length of the current text buffer.
   */
  private int textLength() {

    return text.getDocument().getLength();
  }

  /**
   * Replaces the given string to a position in the currently displayed line.
   *
   * @param newString the string to add.
   * @param start the starting position.
   * @param end the end position.
   */
  private void replaceText(String newString, int start, int end) {

    text.select(start, end);
    text.replaceSelection(newString);
  }

  /**
   * When the enter key has been pressed process the current command.
   */
  private void enterPressed() {

    String command = getCommand();

    // Create null command.
    if (command.length() != 0) {

      // Put the command at the end of the array.
      history.add(command);
      command = command + System.getProperty("line.separator");

      // If the array gets too large remove the last entry.
      if (history.size() > 30) {

        history.remove(0);
      }

      Answer answer = rdqlSession.executeCommand(command);

      println();

      try {

        // print the results
        if (answer != null) {

          while (answer.next()) {

            print("[ ");

            for (int index = 0; index < answer.getNumberOfVariables();
                index++) {

              print(String.valueOf(answer.getObject(index)));

              if (index < (answer.getNumberOfVariables() - 1)) {

                print(", ");
              }
            }

            println(" ]");
          }
        }
      }
      catch (TuplesException te) {

      }

      String lastMessage = rdqlSession.getLastMessage();
      if ((lastMessage != null) && (lastMessage != "")) {

        print(lastMessage);
      }
    }

    println();
    print(RdqlSession.PROMPT);
    historyIndex = 0;
    outputLine(command);
    text.repaint();
  }

  /**
   * Returns the current command.
   *
   * @return the current command.
   */
  private String getCommand() {

    String command = "";
    try {

      command = text.getText(cursorPosition, textLength() - cursorPosition);
    }
    catch (BadLocationException e) {

      log.error("Failed to get text command at position: " + cursorPosition,
          e);
    }

    return command;
  }

  /**
   * Display the next command in the history buffer.
   */
  private void historyUp() {

    // Ensure there's a history and that the index never goes above the array
    // size.
    if ((history.size() != 0) && (historyIndex != history.size())) {

      historyIndex++;
      displayHistoryLine();
    }
  }

  /**
   * Display the previous command in the history buffer.
   */
  private void historyDown() {

    // Ensure there's a history and that the index is initially above 1.
    if ((history.size() != 0) && (historyIndex > 1)) {

      historyIndex--;
      displayHistoryLine();
    }
  }

  /**
   * Displays the history line to the screen.
   */
  private void displayHistoryLine() {

    String showline = (String) history.get(history.size() - historyIndex);
    replaceText(showline, cursorPosition, textLength());
    text.setCaretPosition(textLength());
    text.repaint();
  }

  /**
   * Print out the line to standard out.
   *
   * @param line the line to put to standard out.
   */
  private void outputLine(String line) {

    StringBuffer buffer = new StringBuffer();
    int lineLength = line.length();

    if (outPipe != null) {

      try {

        outPipe.write(line.getBytes());
        outPipe.flush();
      }
      catch (IOException e) {

        log.error("Error getting line", e);
      }
    }
  }

  /**
   * Prints a message to the UI with a line separator.
   *
   * @param message the message to display.
   */
  public void println(String message) {

    print(message + System.getProperty("line.separator"));
    text.repaint();
  }

  /**
   * Prints empty line.
   */
  public void println() {

    print(System.getProperty("line.separator"));
    text.repaint();
  }

  /**
   * Prints a message to the UI.
   *
   * @param message the message to display.
   */
  public void print(final String message) {

    invokeAndWait(new Runnable() {

      public void run() {

        append(message);
        cursorPosition = textLength();
        text.setCaretPosition(cursorPosition);
      }
    });
  }

  /**
   * Print out an error message to the UI.
   *
   * @param errorMessage the error message to display.
   */
  public void error(String errorMessage) {

    print(errorMessage, Color.red);
  }

  /**
   * Print out the message with the given color using the current font.
   *
   * @param message the message to display.
   * @param color the color to use.
   */
  public void print(String message, Color color) {

    print(message, null, color);
  }

  /**
   * Print out the message with the given font and colour.  Uses invoke and
   * wait.
   *
   * @param message the message to display.
   * @param font the font to use.
   * @param color the color to use.
   */
  public void print(final String message, final Font font, final Color color) {

    invokeAndWait(new Runnable() {

      public void run() {

        try {

          AttributeSet oldStyle = text.getCharacterAttributes();
          setStyle(font, color);
          append(message);
          cursorPosition = textLength();
          text.setCaretPosition(cursorPosition);
          text.setCharacterAttributes(oldStyle, true);
        }
        catch (Exception e) {

          log.error("Error when printing: " + message, e);
        }
      }
    });
  }

  /**
   * Sets the new style of a font and color to the text.
   *
   * @param font the new font.
   * @param color the new color.
   * @return the attributes of the given font and color.
   */
  private AttributeSet setStyle(Font font, Color color) {

    MutableAttributeSet attr = new SimpleAttributeSet();
    StyleConstants.setForeground(attr, color);

    // Don't set if null
    if (font != null) {

      StyleConstants.setFontFamily(attr, font.getFamily());
      StyleConstants.setFontSize(attr, font.getSize());
    }
    text.setCharacterAttributes(attr, false);
    return text.getCharacterAttributes();
  }

  /**
   * Append the given string to the existing string.
   *
   * @param newString the string to append to.
   */
  private void append(String newString) {

    int length = textLength();
    text.select(length, length);
    text.replaceSelection(newString);
  }

  /**
   * Thread that runs while waiting for input.
   */
  public void run() {

    try {

      byte[] buffer = new byte[255];
      int read;
      while ( (read = inPipe.read(buffer)) != -1) {

        print(new String(buffer, 0, read));
      }
    }
    catch (IOException e) {

      log.error("Error reading input", e);
    }
  }

  public void propertyChange(PropertyChangeEvent event) {

    // Empty implementation
  }

  public void actionPerformed(ActionEvent event) {

    // Empty implementation
  }

  /**
   * If not in the event thread run via SwingUtilities.invokeAndWait()
   */
  private void invokeAndWait(Runnable runnable) {

    if (!SwingUtilities.isEventDispatchThread()) {

      try {

        SwingUtilities.invokeAndWait(runnable);
      }
      catch (Exception e) {

        log.error("Error while executing invoke and wait", e);
      }
    }
    else {

      runnable.run();
    }
  }

  /**
   * Extension to JTextPane to put all pastes at the end of the command line.
   */
  class PasteablePane extends JTextPane {

    public PasteablePane(StyledDocument doc) {

      super(doc);
    }

    public void paste() {

      text.setCaretPosition(textLength());
      super.paste();
    }
  }
}
