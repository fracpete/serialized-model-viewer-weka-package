/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    SerializedModelViewerPanel.java
 *    Copyright (C) 2015-2019 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.explorer;

import weka.core.Drawable;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.gui.ComponentHelper;
import weka.gui.ExtensionFileFilter;
import weka.gui.Logger;
import weka.gui.SysErrLog;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.Explorer.LogHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/** 
 * This panel allows the user to view serialized files, outputting the string
 * representation of the objects.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SerializedModelViewerPanel
  extends JPanel
  implements ExplorerPanel, LogHandler {

  /** for serialization. */
  private static final long serialVersionUID = 2078066653508312179L;

  /** the parent frame. */
  protected Explorer m_Explorer = null;

  /** The destination for log/status messages. */
  protected Logger m_Log = new SysErrLog();

  /** the file chooser for loading the models. */
  protected JFileChooser m_FileChooserModel;

  /** the file chooser for saving the textual model content. */
  protected JFileChooser m_FileChooserContent;

  /** the text field for the file name. */
  protected JTextField m_TextFile;

  /** the button for the filechooser. */
  protected JButton m_ButtonFile;

  /** the tabbed pane for showing the content. */
  protected JTabbedPane m_TabbedPane;

  /**
   * Creates the Experiment panel.
   */
  public SerializedModelViewerPanel() {
    super();
    initialize();
    initGUI();
  }

  /**
   * Initializes the members.
   */
  protected void initialize() {
    ExtensionFileFilter   filter;

    m_FileChooserModel = new JFileChooser();
    m_FileChooserModel.setFileSelectionMode(JFileChooser.FILES_ONLY);
    m_FileChooserModel.setMultiSelectionEnabled(false);
    filter = new ExtensionFileFilter(new String[]{"model", "ser"}, "Serialized model files (*.model, *.ser)");
    m_FileChooserModel.addChoosableFileFilter(filter);
    m_FileChooserModel.setAcceptAllFileFilterUsed(true);
    m_FileChooserModel.setFileFilter(filter);

    m_FileChooserContent = new JFileChooser();
    filter = new ExtensionFileFilter(new String[]{"txt"}, "Text files (*.txt)");
    m_FileChooserContent.setFileSelectionMode(JFileChooser.FILES_ONLY);
    m_FileChooserContent.addChoosableFileFilter(filter);
    m_FileChooserContent.setAcceptAllFileFilterUsed(true);
    m_FileChooserContent.setFileFilter(filter);
  }

  /**
   * Initializes the widgets.
   */
  protected void initGUI() {
    JPanel      panel;
    JLabel      label;

    setLayout(new BorderLayout());

    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    add(panel, BorderLayout.NORTH);

    m_TextFile = new JTextField(30);
    m_TextFile.setEditable(false);

    m_ButtonFile = new JButton("...");
    m_ButtonFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadFile();
      }
    });

    label = new JLabel("Model");
    label.setDisplayedMnemonic('M');
    label.setLabelFor(m_ButtonFile);

    panel.add(label);
    panel.add(m_TextFile);
    panel.add(m_ButtonFile);

    m_TabbedPane = new JTabbedPane();
    add(m_TabbedPane, BorderLayout.CENTER);
  }

  /**
   * Creates a new text area with popup menu.
   *
   * @return		the text area
   */
  protected JTextArea newTextArea() {
    final JTextArea 	result;

    result = new JTextArea(20, 80);
    result.setEditable(false);
    result.setFont(new Font("monospaced", Font.PLAIN, 12));
    result.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (isRightClick(e)) {
          e.consume();
          showPopup(result, e);
        }
        else {
          super.mouseClicked(e);
        }
      }
    });

    return result;
  }

  /**
   * Encapsulates the component in a new scrollpane.
   *
   * @param comp	the component to wrap
   * @return		the scroll pane
   */
  protected JScrollPane newScrollPane(JComponent comp) {
    JScrollPane 	result;

    result = new JScrollPane(comp);
    result.getHorizontalScrollBar().setBlockIncrement(20);
    result.getHorizontalScrollBar().setUnitIncrement(20);
    result.getVerticalScrollBar().setBlockIncrement(20);
    result.getVerticalScrollBar().setUnitIncrement(20);

    return result;
  }

  /**
   * Checks whether we have a right-click (or alt+shift+left).
   *
   * @param e the mouse event to analyze
   * @return true if "right" click
   */
  protected boolean isRightClick(MouseEvent e) {
    int modifiers = e.getModifiers();
    if (((modifiers & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK)
      && ((modifiers & MouseEvent.ALT_MASK) == MouseEvent.ALT_MASK)
      && ((modifiers & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK)) {
      return true;
    }
    else if ((modifiers & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
      return true;
    }
    return false;
  }

  /**
   * Brings up popup menu for model content text area.
   *
   * @param e the mouse event to react to
   */
  protected void showPopup(final JTextArea textArea, MouseEvent e) {
    JPopupMenu  menu;
    JMenuItem   menuitem;

    menu = new JPopupMenu();

    menuitem = new JMenuItem("Copy", ComponentHelper.getImageIcon("copy.gif"));
    menuitem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        copyContent(textArea);
      }
    });
    menu.add(menuitem);

    menu.addSeparator();

    menuitem = new JMenuItem("Save...", ComponentHelper.getImageIcon("save.gif"));
    menuitem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveContent(textArea);
      }
    });
    menu.add(menuitem);

    menu.show(textArea, e.getX(), e.getY());
  }

  /**
   * Brings up the filechooser to allow user to load model file.
   */
  protected boolean loadFile() {
    int     retVal;

    retVal = m_FileChooserModel.showOpenDialog(this);
    if (retVal != JFileChooser.APPROVE_OPTION)
      return false;

    return loadFile(m_FileChooserModel.getSelectedFile());
  }

  /**
   * Attempts load the specified model file.
   *
   * @param file the file to load
   */
  protected boolean loadFile(File file) {
    Object[]        	objects;
    StringBuilder   	content;
    int             	i;
    JTextArea		m_TextContent;
    String		graph;

    while (m_TabbedPane.getTabCount() > 0)
      m_TabbedPane.removeTabAt(0);

    try {
      objects = SerializationHelper.readAll(file.getAbsolutePath());
      for (i = 0; i < objects.length; i++) {
	content = new StringBuilder();
        content.append(objects[i].getClass().getName()).append("\n");
        content.append("\n");
        content.append("" + objects[i]);
        content.append("\n");
        m_TextContent = newTextArea();
	m_TextContent.setText(content.toString());
        m_TabbedPane.addTab(objects[i].getClass().getSimpleName(), newScrollPane(m_TextContent));
        if (objects[i] instanceof Drawable) {
          try {
            graph = ((Drawable) objects[i]).graph();
	    content = new StringBuilder();
	    content.append(graph);
	    content.append("\n");
	    m_TextContent = newTextArea();
	    m_TextContent.setText(content.toString());
	    m_TabbedPane.addTab(objects[i].getClass().getSimpleName() + " (graph)", newScrollPane(m_TextContent));
	  }
	  catch (Exception e) {
            System.err.println("Failed to obtain graph from: " + file + "/" + objects[i].getClass().getName());
            e.printStackTrace();
	  }
	}
      }
      m_TextFile.setText("" + file);
      if (m_TabbedPane.getTabCount() > 0)
        m_TabbedPane.setSelectedIndex(0);
      return true;
    }
    catch (Exception e) {
      showErrorMessage("Error loading model file", "Failed to load model file: " + file, e);
      return false;
    }
  }

  /**
   * Copies the current content to the clipboard.
   *
   * @param textArea 	the text are to use
   */
  protected void copyContent(JTextArea textArea) {
    String            content;
    StringSelection   selection;
    Clipboard         clipboard;

    if (textArea.getSelectedText() == null)
      content = textArea.getText();
    else
      content = textArea.getSelectedText();

    selection = new StringSelection(content);
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, selection);
  }

  /**
   * Saves the current content to a file.
   *
   * @param textArea 	the text area to use
   */
  protected void saveContent(JTextArea textArea) {
    int               	retVal;
    String            	content;
    File              	file;
    FileWriter		fwriter;
    BufferedWriter 	bwriter;

    retVal = m_FileChooserContent.showSaveDialog(this);
    if (retVal != JFileChooser.APPROVE_OPTION)
      return;

    if (textArea.getSelectedText() == null)
      content = textArea.getText();
    else
      content = textArea.getSelectedText();

    file    = m_FileChooserContent.getSelectedFile();
    fwriter = null;
    bwriter = null;
    try {
      fwriter = new FileWriter(file);
      bwriter = new BufferedWriter(fwriter);
      bwriter.write(content);
      bwriter.flush();
    }
    catch (Exception e) {
      showErrorMessage("Error writing content", "Failed to write content to " + file, e);
    }
    finally {
      if (bwriter != null) {
        try {
          bwriter.flush();
          bwriter.close();
        }
        catch (Exception e) {
          // ignored
        }
      }
      if (fwriter != null) {
        try {
          fwriter.flush();
          fwriter.close();
        }
        catch (Exception e) {
          // ignored
        }
      }
    }
  }

  /**
   * Sets the Logger to receive informational messages.
   *
   * @param newLog 	the Logger that will now get info messages
   */
  public void setLog(Logger newLog) {
    m_Log = newLog;
  }

  /**
   * Prints an error message for an exception in the console, the log and
   * as dialog.
   *
   * @param title the title for the dialog
   * @param msg the message (without exception)
   * @param e the exception
   */
  protected void showErrorMessage(String title, String msg, Exception e) {
    System.err.println(msg);
    e.printStackTrace();
    msg = msg + "\n" + e;
    m_Log.logMessage(msg);
    JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Tells the panel to use a new set of instances.
   *
   * @param inst 	ignored
   */
  public void setInstances(Instances inst) {
  }

  /**
   * Sets the Explorer to use as parent frame (used for sending notifications
   * about changes in the data).
   * 
   * @param parent	the parent frame
   */
  public void setExplorer(Explorer parent) {
    m_Explorer = parent;
  }
  
  /**
   * returns the parent Explorer frame.
   * 
   * @return		the parent
   */
  public Explorer getExplorer() {
    return m_Explorer;
  }
  
  /**
   * Returns the title for the tab in the Explorer.
   * 
   * @return 		the title of this tab
   */
  public String getTabTitle() {
    return "Model viewer";
  }
  
  /**
   * Returns the tooltip for the tab in the Explorer.
   * 
   * @return 		the tooltip of this tab
   */
  public String getTabTitleToolTip() {
    return "Allows viewing of serialized models, outputting their string representation.";
  }
}
