package com.timestored.sqldash.chart;

import net.jcip.annotations.Immutable;

import com.google.common.base.MoreObjects;

/**
 * Container for example view , that provides a {@link TestCase} together with relevant 
 * name/descriptions. Allows generating a view/chart as an example, showing the table that 
 * generated it..
 */
@Immutable
class ExampleView {
	
	private final TestCase testCase;
	private final String name;
	private final String description;
	
	ExampleView(String name, String description, TestCase testCase) {
		
		this.name = name;
		this.description = description;
		this.testCase = testCase;
	}
	
	public String getName() {
		return name;
	}

	/** @return Description of this example and what it is intended to demonstrate. */
	public String getDescription() {
		return description;
	}

	/** Get the data which can be used to show this example */
	public TestCase getTestCase() {
		return testCase;
	}
	
	@Override public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("description", description)
			.add("testCase", testCase)
			.toString();
	}
	
}
