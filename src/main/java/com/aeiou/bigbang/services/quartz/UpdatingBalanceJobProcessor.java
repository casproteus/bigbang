package com.aeiou.bigbang.services.quartz;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.aeiou.bigbang.domain.UserAccount;

@Component("updatingBalanceJobProcessor")
public class UpdatingBalanceJobProcessor {

	private final Log log = LogFactory.getLog(getClass());

	/** 
	 * this will be activated by the Spring Quarz
	 * 1.get all user out into list.
	 *	2.go through each user, and get all the user he's listening.
	 *		replace them with the element in the list
	 *		3.go through this list, for each listened user, fetch out his salary.
	 *		4.give him his salary.(for now we don't allow user to change his salary, later when we allow, we'll send email to his emaployers.
	 *		5.remove the money from the employee		
	 *	6.save every one back to db
	 */
	public synchronized void updateBalance() {
		if(log.isDebugEnabled()){
			log.debug("Quartz excuting" + new Date().toString());
		}
		
		List<UserAccount> tMainList = UserAccount.findAllUserAccounts();
		for(int i = tMainList.size() - 1; i >=0; i--){
			UserAccount tUser = tMainList.get(i);
			Set<UserAccount> tListenedusers = tUser.getListento();
			if(tListenedusers != null){
				Iterator<UserAccount> tLUIterator = tListenedusers.iterator();
				while(tLUIterator.hasNext()){
					UserAccount tLU = tLUIterator.next();
					for(int j = tMainList.size() - 1; j >=0; j--){
						UserAccount tLUinMainList = tMainList.get(j);
						if(tLUinMainList.getId() == tLU.getId()){
							tLUinMainList.setBalance(tLUinMainList.getBalance() + tLUinMainList.getPrice());
							tUser.setBalance(tUser.getBalance() - tLUinMainList.getPrice());
						}
					}
				}
			}
		}

		for(int i = tMainList.size() - 1; i >=0; i--){
			UserAccount tUserAccount = tMainList.get(i);
			tUserAccount.merge();
		}
	}
}
