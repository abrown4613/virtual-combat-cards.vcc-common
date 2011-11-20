/*
 * Copyright (C) 2008-2011 - Thomas Santana <tms@exnebula.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.exnebula.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class JPromptPanelList extends JPanel {

  private JPanel contentPane;
  private JScrollPane scrollPane;
  private List<PromptPanelCell> cells;
  private int rowHeight = 0;
  private int activeCellIndex = -1;
  private static final String actionMapSelectPrevious = "JPromptPanelList.SelectPrevious";
  private static final String actionMapSelectNext = "JPromptPanelList.SelectNext";
  private PromptPanelEditListener editListener;
  private final int gapBetweenCells = 5;

  public JPromptPanelList() {
    super(new GridLayout(1, 1));
    cells = new ArrayList<PromptPanelCell>();
    contentPane = createContentPanel();
    scrollPane = createScrollPane();
    add(scrollPane);

    registerResizeHandler();
    setWhenAncestorInputMap("pressed UP", createMoveSelectionAction(actionMapSelectPrevious, -1));
    setWhenAncestorInputMap("pressed DOWN", createMoveSelectionAction(actionMapSelectNext, +1));

    setRowHeight(25);
  }

  public void setActivePrompt(int selectedRow) {
    if (activeCellIndex != -1) {
      cells.get(activeCellIndex).showViewComponent(false);
    }
    PromptPanelCell activeCell = cells.get(selectedRow);

    activeCellIndex = selectedRow;
    activeCell.showEditorComponent(true);
    activeCell.adjustFocusToEditor();

    adjustScrollPaneVisibleArea(selectedRow);
  }

  public void addPromptPanel(PromptPanel promptPanel) {
    PromptPanelCell cell = new PromptPanelCell(this, promptPanel);
    cells.add(cell);
    cell.setHeight(rowHeight);
    contentPane.add(cell);
    contentPane.add(Box.createRigidArea(new Dimension(0, gapBetweenCells)));
  }

  public void setRowHeight(int newRowHeight) {
    this.rowHeight = newRowHeight;
    scrollPane.getVerticalScrollBar().setBlockIncrement(rowHeight);
    scrollPane.getVerticalScrollBar().setUnitIncrement(rowHeight);
    for (PromptPanelCell cell : cells) {
      cell.setHeight(newRowHeight);
    }
  }

  public int getActivePromptIndex() {
    return activeCellIndex;
  }

  public void setPromptPanelEditListener(PromptPanelEditListener listener) {
    this.editListener = listener;
  }

  void fireEditComplete() {
    if (editListener != null)
      editListener.editComplete(activeCellIndex);
  }


  private JPanel createContentPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBackground(UIManager.getColor("List.background"));
    panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Component component = contentPane.getComponentAt(e.getPoint());

        if (component instanceof PromptPanelCell) {
          PromptPanelCell cell = (PromptPanelCell) component;
          int index = cells.indexOf(cell);
          if (index >= 0)
            setActivePrompt(index);
        }
      }
    });
    return panel;
  }

  private void registerResizeHandler() {
    this.addComponentListener(new ComponentListener() {
      public void componentResized(ComponentEvent e) {
        adjustScrollPaneVisibleArea(activeCellIndex);
      }

      public void componentMoved(ComponentEvent e) {

      }

      public void componentShown(ComponentEvent e) {

      }

      public void componentHidden(ComponentEvent e) {

      }
    });
  }

  private JScrollPane createScrollPane() {
    JScrollPane scrollPane = new JScrollPane(contentPane);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("pressed UP"), "none");
    scrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("pressed DOWN"), "none");
    return scrollPane;
  }

  private void setWhenAncestorInputMap(String keyStroke, Action action) {
    Object actionName = action.getValue(Action.NAME);
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(keyStroke), actionName);
    getActionMap().put(actionName, action);
  }

  private Action createMoveSelectionAction(String name, final int delta) {
    return new AbstractAction(name) {
      public void actionPerformed(ActionEvent e) {
        if (activeCellIndex != -1) {
          int index = activeCellIndex + delta;
          if (0 <= index && index < cells.size())
            setActivePrompt(index);
        }
      }
    };
  }

  private void adjustScrollPaneVisibleArea(int selectedRow) {
    final int adjustedRowHeight = rowHeight + gapBetweenCells;
    int suggestedPosition = selectedRow * adjustedRowHeight;
    final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
    if ((scrollBar.getVisibleAmount() >= 2 * rowHeight) && selectedRow > 0) {
      suggestedPosition = suggestedPosition - adjustedRowHeight;
    }
    scrollBar.setValue(suggestedPosition);
  }
}