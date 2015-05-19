package se.caplogic.mappedbus.integrity;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;
import se.caplogic.mappedbus.MappedBusReader;
import se.caplogic.mappedbus.MappedBusWriter;

public class IntegrityTest {
	
	public static final String FILE_NAME = "/home/mikael/tmp/integrity-test";
	
	public static final long FILE_SIZE = 4000000L;
	
	public static final int NUM_WRITERS = 10;
	
	public static final int RECORD_LENGTH = 10;
	
	public static final int NUM_RECORDS = 10000;
	
	public static final int NUM_RUNS = 100;
	
	@Test public void test() throws Exception {
		for (int i = 0; i < NUM_RUNS; i++) {
			runTest();
		}
	}
	
	private void runTest() throws Exception {
		new File(FILE_NAME).delete();

		Writer[] writers = new Writer[NUM_WRITERS];
		for(int i = 0; i < writers.length; i++) {
			writers[i] = new Writer(i);
		}
		for(int i = 0; i < writers.length; i++) {
			writers[i].start();
		}
		for(int i = 0; i < writers.length; i++) {
			writers[i].join();
		}
		
		MappedBusReader reader = new MappedBusReader(FILE_NAME, FILE_SIZE, RECORD_LENGTH);
		byte[] data = new byte[RECORD_LENGTH];
		while(reader.hasNext()) {
			if(!reader.next()) {
				continue; // the record was abandoned, skip it
			}
			int length = reader.readBuffer(data, 0);
			Assert.assertEquals(RECORD_LENGTH, length);
			for(int i=0; i < data.length; i++) {
				if(data[0] != data[i]) {
					fail();
					return;
				}
			}
			
		}
		reader.close();
		
		new File(FILE_NAME).delete();
	}

	class Writer extends Thread {
		
		private final int id;

		public Writer(int id) {
			this.id = id;
		}
		
		public void run() {
			try {
				MappedBusWriter writer = new MappedBusWriter(IntegrityTest.FILE_NAME, IntegrityTest.FILE_SIZE, IntegrityTest.RECORD_LENGTH, true);

				byte[] data = new byte[IntegrityTest.RECORD_LENGTH];
				Arrays.fill(data, (byte)id);
				
				for(int i=0; i < IntegrityTest.NUM_RECORDS; i++) {
					writer.write(data, 0, data.length);
				}
				writer.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}