package com.timestored.sqldash;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Formats command line options as a table with two columns (option,description).
 * Commands that perform the same function are grouped onto a single row
 * and required parameters are marked with a star. 
 */
class HtmlHelpFormatter implements HelpFormatter {

	@Override public String format(Map<String, ? extends OptionDescriptor> options) {

		StringBuilder sb = new StringBuilder();
		
        if(!options.isEmpty()) {
        	// sort
	        Comparator<OptionDescriptor> comparator =
	            new Comparator<OptionDescriptor>() {
	                public int compare( OptionDescriptor first, OptionDescriptor second ) {
	                    return first.options().iterator().next().compareTo( second.options().iterator().next() );
	                }
	            };
	        TreeSet<OptionDescriptor> sorted = new TreeSet<OptionDescriptor>( comparator );
	        
	        Set<String> seenCommands = Sets.newHashSet();

	        // remove arguments that are for the same thing,
	        // print them only once
	        for(OptionDescriptor od : options.values()) {
	        	String k = getOptionDisplay(od);
	        	if(!seenCommands.contains(k)) {
	        		seenCommands.add(k);
	        		sorted.add(od);
	        	}
	        }
        	
	        // create list
			sb.append("<table>\r\n<tr><th>Option");
			if(hasRequiredOption(sorted)) {
				sb.append(" (<span class='required'>*</span> = required)");
			}
			sb.append("</th><th>Description</th></tr>");
			for(OptionDescriptor od : sorted) {
				sb.append("\r\n\t<tr>");
				sb.append("<td style='white-space:nowrap;'><code>");
				sb.append(getOptionDisplay(od));
				sb.append("</code></td><td>");
				
		        List<?> defVals = od.defaultValues();
				if (defVals.isEmpty()) {
		            sb.append(od.description());
		        } else {
					String def = defVals.size() == 1 ? 
							defVals.get(0).toString() : defVals.toString();
					sb.append(od.description() + " (default: " + def + ")");
		        }
				sb.append("</td></tr>");
			}
			sb.append("\r\n</table>");
        }
        
		return sb.toString();
	}
	
	private boolean hasRequiredOption(Collection<? extends OptionDescriptor> options) {
		for (OptionDescriptor each : options) {
			if (each.isRequired()) {
				return true;
			}
		}
		return false;
	}
    
    private String getOptionDisplay(OptionDescriptor descriptor ) {
    	StringBuilder buffer = new StringBuilder();
    	buffer.append( descriptor.isRequired() ? "<span class='required'>*</span>" : "" );

        for ( Iterator<String> i = descriptor.options().iterator(); i.hasNext(); ) {
            String option = i.next();
            buffer.append( option.length() > 1 ? "--" : "-" );
            buffer.append( option );

            if ( i.hasNext() ) {
                buffer.append( ", " );
            }
        }

        maybeAppendOptionInfo( buffer, descriptor );
        return buffer.toString();
    }
    


    private void maybeAppendOptionInfo( StringBuilder buffer, OptionDescriptor descriptor ) {
        String indicator = extractTypeIndicator( descriptor );
        String description = descriptor.argumentDescription();
        if ( indicator != null || !Strings.isNullOrEmpty( description ) )
            appendOptionHelp( buffer, indicator, description, descriptor.requiresArgument() );
    }

	private String extractTypeIndicator( OptionDescriptor descriptor ) {
        String indicator = descriptor.argumentTypeIndicator();

        if ( !Strings.isNullOrEmpty( indicator ) && !String.class.getName().equals( indicator ) ) {
            return indicator;
        }

        return null;
    }

	    private void appendOptionHelp( StringBuilder buffer, String typeIndicator, String description, boolean required ) {
	        if ( required )
	            appendTypeIndicator( buffer, typeIndicator, description, "&lt;", "&gt;" );
	        else
	            appendTypeIndicator( buffer, typeIndicator, description, "[", "]" );
	    }

	    private void appendTypeIndicator( StringBuilder buffer, String typeIndicator, String description,
	                                      String start, String end ) {
	        buffer.append( ' ' ).append( start );
	        if ( typeIndicator != null )
	            buffer.append( typeIndicator );

	        if ( !Strings.isNullOrEmpty( description ) ) {
	            if ( typeIndicator != null )
	                buffer.append( ": " );

	            buffer.append( description );
	        }

	        buffer.append( end );
	    }

}