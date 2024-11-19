package com.timestored.swingxx;

import java.io.IOException;
import java.util.Set;

import bibliothek.gui.DockFrontend;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;


/* Util functions for helping with bibliothek DockFrontend */
public class DockerHelper {

	/** Get an xml string that stores the current window layout **/
	public static String getLayout(DockFrontend frontend) {
		XElement xroot = new XElement( "layout" );
		frontend.writeXML( xroot );
		String layoutXml = xroot.toString();
		return layoutXml;
	}
	
	/** load a layout from an xml string **/
	public static void loadLayout(String layoutXml, DockFrontend frontend) {
		if(layoutXml.length() > 0) {
            try {
			     /* "read" does not delete already existing layouts, so we clean up
	             * before applying a new set of layouts. */
	            Set<String> layouts = frontend.getSettings();
	            String[] keys = layouts.toArray( new String[ layouts.size() ] );
	            for( String key : keys ){
	                frontend.delete( key );
	            }
	            
				frontend.readXML(XIO.read(layoutXml));
			} catch (IOException e1) {
				// nothing worth doing
			}
		}
	}
}
