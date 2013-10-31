/*
  Copyright (C) 2005 University Of New South Wales.
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.robocup.gamecontroller.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PenaltyAdapter implements ActionListener {

	protected MainGUI gui;
	protected short code;

	public PenaltyAdapter(MainGUI gui, short code) {
		super();
		this.gui = gui;
		this.code = code;
	}

	public void actionPerformed(ActionEvent evt) {
		gui.penalty_clicked(code);
	}

}
