package com.monkey.pickled.src.tests.uno;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sun.star.text.XTextDocument;

import com.monkey.pickled.src.tests.helper.UnoHelper;

public class WriterTest {
	
	private XTextDocument xTextDocument;

	@Before
	public void setUp() throws Exception {
		xTextDocument = UnoHelper.getWriterDocument();
	}

	@Test
	public void test() {
		assertNotNull(xTextDocument);
	}

}
