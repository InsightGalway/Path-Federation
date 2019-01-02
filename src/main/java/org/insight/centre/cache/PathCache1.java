package org.insight.centre.cache;

import java.io.Serializable;

public class PathCache implements Serializable{


	private static final long serialVersionUID = 8298904626856815840L;
	public String src, target;
	public boolean passOrFail;
	
	public PathCache(String src, String target, boolean bool) {
		this.src=src; this.target=target;this.passOrFail=bool;
	}
	
}
