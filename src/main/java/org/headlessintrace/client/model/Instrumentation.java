package org.headlessintrace.client.model;

import java.util.ArrayList;
import java.util.List;

public class Instrumentation {
	private List<String> m_includedClasses = null;
	private List<String> m_excludedClasses = null;
	  public static List<String> getListFromString(String pattern)
	  {
	    List<String> items = new ArrayList<String>();
	    String[] patternParts = pattern.split("\\|");
	    for (String part : patternParts)
	    {
	      items.add(part);
	    }
	    return items;
	  }

	  public static String getStringFromList(List<String> list)
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

	public List<String> getIncludedClasses() {
		return m_includedClasses;
	}

	public void setIncludedClasses(List<String> includedClasses) {
		this.m_includedClasses = includedClasses;
	}

	public List<String> getExcludedClasses() {
		return m_excludedClasses;
	}

	public void setExcludedClasses(List<String> excludedClasses) {
		this.m_excludedClasses = excludedClasses;
	}

}
