package com.renx.mg.request.model;

public enum RequestStatusType {
	PENDING_APPROVAL("Pending_Approval"),CREATED("Created"),ASSIGNED("Assigned"),IN_TRANSIT("In Transit"),DONE("Done"),RATED("Rated"),REJECTED("Rejected");
	
	 private final String name;

	 RequestStatusType(String name) {
	    this.name = name;
	  }

	  public String getName() {
	    return name;
	  }
	
	
}
