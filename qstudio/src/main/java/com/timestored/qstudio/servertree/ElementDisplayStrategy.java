package com.timestored.qstudio.servertree;

import java.awt.Component;
import java.io.IOException;

import kx.c.KException;

import com.timestored.qstudio.model.AdminModel;

/**
 * Allows displaying details about admin models selected element.
 */
interface ElementDisplayStrategy {
	public Component getPanel(AdminModel adminModel) throws IOException, KException;
}
