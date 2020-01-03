package com.monkey.pickled.src.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.monkey.pickled.src.tests.base.UnoSuite;
import com.monkey.pickled.src.tests.uno.WriterTest;

@RunWith(UnoSuite.class)
@SuiteClasses({WriterTest.class})
public class UnoTests {

}
