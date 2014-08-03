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

package com.nokia.xfolite.xforms.model;

public class BinaryAttachment {

	// TODO: persistent storage?
	//RFs iFs;

	private boolean hasBuffer;
	private byte[] iData;
	private InstanceItem iItem;
	private String iFilename;
	private String iMime;
	private String iFilelink;

	/**
	 * @param aData The buffer. NOTE! This object takes the ownership of the aData.
	 */
	public void SetData(byte[] aData)
	{
		this.iData=aData;
	}
	public void SetFilename(String aFilename)
	{
		this.iFilename=aFilename;
	}
	public void SetMime(String aMime)
	{
		this.iMime=aMime;
	}
	public void SetFilelink(String aFilelink)
	{
		this.iFilelink=aFilelink;
	}
	public void SetInstanceItem(InstanceItem aItem)
	{
		this.iItem=aItem;
	}
	
	public  InstanceItem GetInstanceItem()
	{
		return this.iItem;
	}
	public String GetMime()
	{
		return this.iMime;
	}
	public String GetFileLink()
	{
		return this.iFilelink;
	}
	public String GetFileName()
	{
		return this.iFilename;
	}
	
	public byte[] GetBuffer()
	{
		return this.iData;
	}

}
