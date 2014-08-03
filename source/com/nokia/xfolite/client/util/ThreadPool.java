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

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


/**
 * This class is used in S60 2nd edition phones (6680), since the VM does not 
 * garbage collect threads that have finished.
 * @author mikkhonk
 *
 */

public class ThreadPool {

	// private int maxTimers=10;
	private long initDelay = 100;

	private Vector freeTimers = new Vector();

	private Vector busyTimers = new Vector();

	private static ThreadPool instance;

	public static ThreadPool getInstance() {
		if (instance == null)
			instance = new ThreadPool();
		return instance;
	}

	public void callParallel(final Runnable r) {
		if (freeTimers.size() == 0) {
			synchronized (freeTimers) 
			{
				freeTimers.addElement(new Timer());
			}
		}
		Timer t = null;
		if (freeTimers.size() > 0) {
			synchronized (freeTimers) 
			{
				t = (Timer) freeTimers.firstElement();
				freeTimers.removeElementAt(0);
				busyTimers.addElement(t);
			}
		} else {
			// t=(Timer)this.busyTimers.elementAt(0);
            //#debug error
			System.out.println("No free timers");
		}
		final Timer t2 = t;
		t.schedule(new TimerTask() {
			public void run() {
				// TODO Auto-generated method stub
				r.run();
				synchronized (freeTimers) 
				{
					freeTimers.addElement(t2);
					busyTimers.removeElement(t2);
					/*
					Suite.log().debug(
							"Par task. fr:" + freeTimers.size() + " bu:"
									+ busyTimers.size());
									*/
				}
			}

		}, initDelay);
	}

	public void stopPool() {
		// TODO Auto-generated method stub
		Enumeration timersEnum = this.busyTimers.elements();
		while (timersEnum.hasMoreElements())
		{
			Timer t = (Timer)timersEnum.nextElement();
			t.cancel();
		}
		timersEnum = this.freeTimers.elements();
		while (timersEnum.hasMoreElements())
		{
			Timer t = (Timer)timersEnum.nextElement();
			t.cancel();
		}
		
	}
}
