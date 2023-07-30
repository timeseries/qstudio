package com.timestored.qstudio.snippet;

import lombok.Data;

/** A named piece of code that the user can save and re-run easily. */
@Data public class CodeSnippet {
	private final String name;
	private final String code;
	private final boolean isRunnable;
	private final boolean isPasteable;
}
