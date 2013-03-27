package com.aeiou.bigbang.services.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component("updatingBalanceJobProcessor")
public class UpdatingBalanceJobProcessor {

	private final Log log = LogFactory.getLog(getClass());

	// this will be activated by the Spring Quarz
	public synchronized void updateBalance() {
		if(log.isDebugEnabled()){
			log.debug("Quartz excuting.......................................");
		}
        //TODO:
		//1.get all user out.
		//	2.go through each user, and get all the user he's listening.
		//		3.for each listened user, fetch out his salary.
		//		4.give him his salary.(for now we don't allow user to change his salary.
	}
}
