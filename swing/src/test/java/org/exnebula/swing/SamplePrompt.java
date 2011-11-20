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
import java.awt.event.ActionEvent;

class SamplePrompt implements PromptPanel {
  private JPanel viewPanel;
  private JPanel editorPanel;
  public JLabel stateLabel;
  protected String prefix;
  private int state = 0;
  private EditCompletionListener editCompletionListener;

  public SamplePrompt(String prefix) {
    this.prefix = prefix;
    stateLabel = new JLabel("State: " + 0);
    viewPanel = createViewPanel(prefix);
    editorPanel = createEditorPanel(prefix);
    editCompletionListener = null;
    setState(state);
  }

  public JComponent getViewComponent() {
    return viewPanel;
  }

  public JComponent getEditorComponent() {
    return this.editorPanel;
  }

  public void setEditCompletionListener(EditCompletionListener listener) {
    editCompletionListener = listener;
  }

  public String getPromptTile() {
    return "Question for " + prefix;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "(" + prefix + ")";
  }

  public void adjustFocusToEditor() {
    editorPanel.getComponent(0).requestFocus();
  }

  protected int getState() {
    return state;
  }

  protected void setState(int value) {
    state = value;
    stateLabel.setText("State: " + state + " for " + prefix);
  }

  protected JPanel createEditorPanel(String prefix) {
    JPanel panel = new JPanel();
    for (Component component : createControls(prefix)) {
      panel.add(component);
    }
    return panel;
  }

  private Component[] createControls(final String prefix) {
    JButton button = new JButton(new AbstractAction(prefix) {
      public void actionPerformed(ActionEvent e) {
        setState(getState() + 1);
        if (editCompletionListener != null)
          editCompletionListener.editComplete();
      }
    });
    button.setName(prefix + "-button");
    JCheckBox checkBox = new JCheckBox("Check here");
    return new Component[]{button, checkBox};
  }

  private JPanel createViewPanel(String prefix) {
    JPanel panel = new JPanel(new FlowLayout());
    panel.add(stateLabel);
    stateLabel.setName(prefix + "-state-view");
    return panel;
  }

}