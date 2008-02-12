package ubisoa.activecloud.hal.filesystem;

import java.util.EventObject;

public class CapsuleEvent extends EventObject{
	private static final long serialVersionUID = -2258402404358872576L;
	
	private String[] addedJars;
	private String[] deletedJars;
	
	public CapsuleEvent(Object source, String[] addedJars, String[] deletedJars){
		super(source);
		this.addedJars = addedJars;
		this.deletedJars = deletedJars;
	}
 
	public String[] getAddedJars() {
		return addedJars;
	}

	public void setAddedJars(String[] addedJars) {
		this.addedJars = addedJars;
	}

	public String[] getDeletedJars() {
		return deletedJars;
	}

	public void setDeletedJars(String[] deletedJars) {
		this.deletedJars = deletedJars;
	}
}
