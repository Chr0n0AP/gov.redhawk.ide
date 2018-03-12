/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package gov.redhawk.ide.snapshot.writer.internal;

import gov.redhawk.bulkio.util.StreamSRIUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nxm.sys.lib.Table;
import BULKIO.PrecisionUTCTime;
import BULKIO.StreamSRI;

public class BinSriDataWriter extends BinDataWriter {

	private static final String METADATA_FILE_EXENSION = "sri";
	/**the string representation of the format to print the start and end times of files*/
	private final String timeFormat = "yyyy-MM-dd HH:mm:ss:SSS z";
	private PrecisionUTCTime startTime;
	private PrecisionUTCTime endTime;
	private Date startDate;

	@Override
	public List<File> getOutputFileList() {
		return Arrays.asList(new File[] { getFileDestination(), getMetaDataFile() });
	}

	@Override
	public void open() throws IOException {
		super.open();
		startDate = new Date();
	}

	@Override
	public void pushPacket(byte[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	public void pushPacket(char[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	public void pushPacket(double[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	public void pushPacket(float[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	public void pushPacket(int[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	public void pushPacket(long[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	public void pushPacket(short[] data, int offset, int length, PrecisionUTCTime time) throws IOException {
		if (startTime == null) {
			startTime = time;
		}
		endTime = time;
		super.pushPacket(data, offset, length, time);
	}

	@Override
	protected void saveMetaData() throws IOException {
		Table rootTable = new Table();
		Table generalTable = (Table) rootTable.addTable("General Information");
		putGeneralInformation(generalTable);

		StreamSRI sri = getSRI();
		if (sri != null) {
			StreamSRIUtil.putSriInfo(getSRI(), rootTable);
		}
		if (startTime != null) {
			Table startTable = (Table) rootTable.addTable("First Packet Time");
			putTime(startTime, startTable);
		}
		if (endTime != null) {
			Table stopTable = (Table) rootTable.addTable("Last Packet Time");
			putTime(endTime, stopTable);
		}

		generalTable.put("BulkIOType", getSettings().getType().name());

		List<String> list = rootTable.toConfigFile();
		FileWriter output = new FileWriter(getMetaDataFile());
		BufferedWriter buffer = new BufferedWriter(output);
		PrintWriter out = new PrintWriter(buffer);
		try {
			for (String s : list) {
				out.println(s);
			}
		} finally {
			out.close();
		}
	}

	private void putGeneralInformation(Table table) {
		SimpleDateFormat format = new SimpleDateFormat(this.timeFormat);
		int divisor = (getSRI().mode == 1) ? 2 : 1;
		table.put("Number of Samples", getNumSamples() / divisor);
		table.put("Data Byte Order", getByteOrder());
		table.put("Start Time", format.format(startDate));
		table.put("End Time", format.format(new Date()));
	}

	private void putTime(PrecisionUTCTime time, Table table) {
		table.put("tcmode", time.tcmode);
		table.put("tfsec", time.tfsec);
		table.put("twsec", time.twsec);
		table.put("tcstatus", time.tcstatus);
		table.put("toff", time.toff);
	}

	@Override
	protected String getMetaDataFileExtension() {
		return METADATA_FILE_EXENSION;
	}

}
