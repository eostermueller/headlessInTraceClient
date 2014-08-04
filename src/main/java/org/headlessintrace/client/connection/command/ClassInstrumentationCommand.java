package org.headlessintrace.client.connection.command;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.headlessintrace.shared.AgentConfigConstants;


public class ClassInstrumentationCommand extends AbstractDefaultCommand {

	private String m_includeClassRegEx = null;
	private String m_excludeClassRegEx = null;
	private static final String NEW_LINE_DELIMS = "\n\r";
	
	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return 
			AgentConfigConstants.EXCLUDE_CLASS_REGEX+getExcludeClassRegEx()
			+AgentConfigConstants.CLASS_REGEX+this.getIncludeClassRegEx();
	}

	public String getIncludeClassRegEx() {
		return m_includeClassRegEx;
	}

	public void setIncludeClassRegEx(String includeClassRegEx) {
		this.m_includeClassRegEx = includeClassRegEx;
	}

	public String getExcludeClassRegEx() {
		return m_excludeClassRegEx;
	}

	public void setExcludeClassRegEx(String excludeClassRegEx) {
		this.m_excludeClassRegEx = excludeClassRegEx;
	}
	private static List<String> rawTextToList(String rawMultiLineText) {
		List<String> al = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(rawMultiLineText,NEW_LINE_DELIMS);
		String currentLine = null;
		while(st.hasMoreTokens()) {
			currentLine = (String)st.nextToken();
			al.add(currentLine);
		}
		return al;
	}
	
	/**
	 * Designed to take the text straight from the list box.
	 * @param rawMultiLineText
	 * @return
	 */
	public static ClassInstrumentationCommand create(String rawMultiLineText) {
		
		List<String> list = rawTextToList(rawMultiLineText);
		String classesIncludeRegEx = getStringFromList(list);
		
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx(classesIncludeRegEx);
		return cic;
	}
	
	  private static String getStringFromList(List<String> list)
	  {
	    StringBuilder str = new StringBuilder();
	
	    for (int ii = 0; ii < list.size(); ii++)
	    {
	      String item = list.get(ii);
	      str.append(item);
	      if (ii < (list.size() - 1))
	      {
	        str.append("|");
	      }
	    }
	    return str.toString();
	  }

}
