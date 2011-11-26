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

public class RulingPanelSample extends JFrame {

  private JPromptPanelList promptPanelList;

  public RulingPanelSample(String title) throws HeadlessException {
    super(title);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    promptPanelList = createPromptPanel();
    add(promptPanelList, BorderLayout.CENTER);
    this.setPreferredSize(new Dimension(300, 240));
  }

  private JPromptPanelList createPromptPanel() {
    JPromptPanelList panelList = new JPromptPanelList();
    addThreeSamplePrompt(panelList);
    addThreeSamplePrompt2(panelList);
    panelList.setName("PromptPanel");
    panelList.setRowHeight(80);
    panelList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return panelList;
  }

  private void addThreeSamplePrompt2(JPromptPanelList panelList) {
    for (int i = 3; i < 6; i++) {
      panelList.addPromptPanel(new SamplePrompt2("prompt" + i));
    }
  }

  private void addThreeSamplePrompt(JPromptPanelList panelList) {
    for (int i = 0; i < 3; i++) {
      panelList.addPromptPanel(new SamplePrompt("prompt" + i));
    }
  }

  public static void main(String[] arguments) {
    RulingPanelSample sample = new RulingPanelSample("Sample");
    sample.promptPanelList.setAutoSelectNextUnanswered();
    sample.pack();
    sample.setVisible(true);
  }

}