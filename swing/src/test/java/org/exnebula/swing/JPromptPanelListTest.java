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

import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.interception.MainClassAdapter;

import java.awt.*;
import javax.swing.*;

public class JPromptPanelListTest extends UISpecTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setAdapter(new MainClassAdapter(RulingPanelSample.class));
  }

  public void testCreateMainWindowAndSeeAllPromptsOnViewCell() {
    for (int i = 0; i < 6; i++) {
      assertNotNull(findJLabel("prompt" + i + "-state"));
    }
    assertNotNull(getPromptPanel());
  }

  public void testWhenClickOnFirst_showFirstPanel() {
    Mouse.doClickInRectangle(getMainWindow(), new Rectangle(10, 10, 10, 10), false, Key.Modifier.NONE);
    assertNotNull(getMainWindow().getButton("prompt0-button"));
  }

  public void testSelectingShowEditorForSelected() {
    getJPromptPanelList().setActivePrompt(2);
    assertNotNull(getMainWindow().getButton("prompt2-button"));
  }

  public void testOnceSelectedCanClearActiveField() {
    getJPromptPanelList().setActivePrompt(0);
    assertNotNull(getMainWindow().getButton("prompt0-button"));

    getJPromptPanelList().deactivatePrompt();
    assertNotNull(findJLabel("prompt0-state"));
  }

  public void testOnlyFireEditCompleteIfListenerExists() {
    try {
      selectFirstPanelAndClickButton();
    } catch (Exception e) {
      fail("Should not throw exception: " + e);
    }
  }

  public void testWhenSelectedSecondInSmallWindow_showSecondAsFirst() {
    getJPromptPanelList().setActivePrompt(1);
    assertNotNull(getMainWindow().getButton("prompt1-button"));
  }

  public void testAfterSelectDownArrow_selectsNext() {
    getJPromptPanelList().setActivePrompt(0);
    getPromptPanel().pressKey(Key.DOWN);
    assertNotNull(getMainWindow().getButton("prompt1-button"));
  }

  public void testAfterLastDownArrow_doesNothing() {
    getJPromptPanelList().setActivePrompt(5);
    getPromptPanel().pressKey(Key.DOWN);
    assertNotNull(getMainWindow().getRadioButton("prompt5-radio1"));
  }

  public void testAfterSelectFirstUpArrow_doesNothing() {
    getJPromptPanelList().setActivePrompt(0);
    getPromptPanel().pressKey(Key.UP);
    assertNotNull(getMainWindow().getButton("prompt0-button"));
  }

  public void testAfterSelectSecondUpArrow_selectPrevious() {
    getJPromptPanelList().setActivePrompt(1);
    getPromptPanel().pressKey(Key.UP);
    assertNotNull(getMainWindow().getButton("prompt0-button"));
  }

  public void testSelectOutOfBoundThrowsException() {
    try {
      getJPromptPanelList().setActivePrompt(10);
    } catch (IndexOutOfBoundsException e) {
    } catch (Exception e) {
      fail("Should have thrown index out of bound");
    } finally {
      assertEquals(-1, getJPromptPanelList().getActivePromptIndex());
    }
  }

  public void testFinishEdit_moveSelectionToReturnOfNextActionMover() {
    final JPromptPanelList promptPanel = getJPromptPanelList();
    createAndRegisterPromptPanelEditListener(promptPanel);
    selectFirstPanelAndClickButton();
    assertNotNull(getMainWindow().getButton("prompt1-button"));
  }

  private void createAndRegisterPromptPanelEditListener(final JPromptPanelList promptPanel) {
    PromptPanelEditListener ppel = new PromptPanelEditListener() {
      public void editComplete(int promptIndex) {
        promptPanel.setActivePrompt(promptIndex + 1);
      }
    };
    promptPanel.setPromptPanelEditListener(ppel);
  }

  private void selectFirstPanelAndClickButton() {
    getJPromptPanelList().setActivePrompt(0);
    getMainWindow().getButton("prompt0-button").click();
  }

  private JLabel findJLabel(String name) {
    final Component[] components = getMainWindow().getSwingComponents(JLabel.class, name);
    if (components.length == 1)
      return (JLabel) components[0];
    return null;
  }

  private Panel getPromptPanel() {
    return getMainWindow().getPanel("PromptPanel");
  }

  private JPromptPanelList getJPromptPanelList() {
    final Component[] swingComponents = getMainWindow().getSwingComponents(JPromptPanelList.class);
    assertEquals(1, swingComponents.length);
    assertTrue(swingComponents[0] instanceof JPromptPanelList);
    return (JPromptPanelList) swingComponents[0];
  }

}