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
import java.awt.event.ActionEvent;

public class SamplePrompt2 extends SamplePrompt {

  private JRadioButton radio1;
  private JRadioButton radio2;

  public SamplePrompt2(String prefix) {
    super(prefix);
  }

  @Override
  protected JPanel createEditorPanel(String prefix) {
    JPanel panel = new JPanel();
    panel.add(new JLabel("Select one:"));
    ButtonGroup buttonGroup = new ButtonGroup();
    radio1 = createRadioButton(prefix, 1);
    radio2 = createRadioButton(prefix, 3);
    buttonGroup.add(radio1);
    panel.add(radio1);
    buttonGroup.add(radio2);
    panel.add(radio2);
    return panel;
  }

  private JRadioButton createRadioButton(String prefix, final int i) {
    JRadioButton radioButton = new JRadioButton(new AbstractAction("Set state " + i) {
      public void actionPerformed(ActionEvent e) {
        setState(i);
        fireEditComplete();
      }
    });
    radioButton.setName(prefix + "-radio" + i);
    return radioButton;
  }

  @Override
  public void adjustFocusToEditor() {
    radio1.requestFocus();
    requestFocusIfSelected(radio2);
  }

  private void requestFocusIfSelected(JRadioButton radioButton) {
    if (radioButton.isSelected())
      radioButton.requestFocus();
  }
}