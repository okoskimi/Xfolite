/*
 * This file is part of: Xfolite (J2ME XForms client)
 *
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
 *
 * Contact: Oskari Koskimies <oskari.koskimies@nokia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser
 * General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.nokia.xfolite.client.util;

import java.util.Vector;
import javax.microedition.lcdui.*;


/**
 * This class is used in S60 2nd edition phones (6680), since the callSerially does not
 * always run the Runnable that is placed there.
 * @author mikkhonk
 *
 */

public class EventQueue {
	private Vector queue = new Vector();

	private static EventQueue instance;

	private boolean keepRunning = true;

	private QueueThread queueThread = null;

	private Display display;

	public static EventQueue getInstance() {
		if (instance == null)
			instance = new EventQueue();
		return instance;
	}

	public EventQueue() {
		this.queueThread = new QueueThread();
		this.queueThread.start();
	}

	public void callSerially(Runnable r, Display d) {
		synchronized (queue) {
			this.display = d;
			//Suite.log().debug("EQ: add task. count:" + queue.size());
			queue.addElement(r);
		}
	}

	public void stopQueue() {
		keepRunning = false;
		queueThread = null;
	}

	public class QueueThread extends Thread {
		// Note that this is implemented in such a way that if this is never called, it does not
		// matter. This is because of the 6680 bug.
		public void doOneRound() {
			Object o = null;
			synchronized (queue) {
				if (queue.size() > 0) {
					o = queue.firstElement();
					queue.removeElementAt(0);
				}
			}
			if (o != null) {
				if (o instanceof Runnable) {
					// if (queue.size()>0)
					//#debug
                    System.out.println("EQ: task. count:" + queue.size());
					((Runnable) o).run();
					//#debug
                    System.out.println("EQ: task finished. count:" + queue.size());
				}
			}

		}

		public void run() {
			while (keepRunning) {
				try {
					{
						if (queue.size()>0)
						{
							display.callSerially(new Runnable() {
								public void run() 
								{
										doOneRound();
								}
							});
						}
						Thread.sleep(150);
					}
				} catch (Throwable t) {
                    //#debug warn
					System.out.println("EventQueue error: " + t);
				}
			}
		}

	}
}
