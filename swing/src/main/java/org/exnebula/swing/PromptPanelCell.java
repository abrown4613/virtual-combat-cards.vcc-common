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

class PromptPanelCell extends JPanel implements PromptPanel.EditCompletionListener {
  final private PromptPanel promptPanel;
  final private JPromptPanelList parent;
  private JLabel cellLabel;
  private JPanel editViewPanel;


  public PromptPanelCell(JPromptPanelList parent, PromptPanel aPromptPanel) {
    this.parent = parent;
    this.promptPanel = aPromptPanel;
    promptPanel.setEditCompletionListener(this);

    setLayout(new CardLayout());
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    cellLabel = new JLabel(promptPanel.getPromptTile());
    cellLabel.setHorizontalAlignment(SwingConstants.CENTER);
    cellLabel.setAlignmentX(0.5f);
    final Dimension preferredSize = cellLabel.getPreferredSize();
    cellLabel.setPreferredSize(new Dimension(2000, preferredSize.height));

    editViewPanel = new JPanel();
    editViewPanel.setLayout(new CardLayout());
    editViewPanel.add(promptPanel.getViewComponent(), "view");
    editViewPanel.add(promptPanel.getEditorComponent(), "editor");

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(cellLabel);
    add(editViewPanel);
    showViewComponent();
  }

  public void setHeight(int height) {
    setMinimumSize(new Dimension(25, height));
    setPreferredSize(new Dimension(100, height));
    setMaximumSize(new Dimension(Short.MAX_VALUE, height));
  }

  public void editComplete() {
    parent.fireEditComplete();
  }

  @Override
  public String toString() {
    return "PromptPanelCell(" + promptPanel.toString() + ")";
  }

  public void showViewComponent() {
    this.setBackground(UIManager.getColor("List.background"));
    this.setBackground(new Color(0xD3, 0xD3, 0xE4));
    ((CardLayout) editViewPanel.getLayout()).show(editViewPanel, "view");
    promptPanel.getViewComponent();
  }

  public void showEditorComponent() {
    this.setBackground(UIManager.getColor("List.selectionBackground"));
    cellLabel.setBackground(UIManager.getColor("List.selectionBackground"));
    ((CardLayout) editViewPanel.getLayout()).show(editViewPanel, "editor");
    promptPanel.getEditorComponent();
  }

  public void adjustFocusToEditor() {
    promptPanel.adjustFocusToEditor();
  }
}