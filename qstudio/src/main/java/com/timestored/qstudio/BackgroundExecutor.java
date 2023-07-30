package com.timestored.qstudio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BackgroundExecutor {

	public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
}
