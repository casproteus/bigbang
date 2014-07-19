package com.aeiou.bigbang.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.servlet.theme.CookieThemeResolver;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.quartz.UpdatingBalanceJobProcessor;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.services.synchronization.ClientSyncTool;

public class BigUtil {

	public static String DEFAULT_IMAGE_TYPE = ".jpg";
	
	//Can not use strnge characters, because when the coding formmat of the IDE changes or are not same with database, will cause mismatch.
	//Can neiter use "[","(","+".... because the string will be considered as an expression in split method, those character have special
	//meaning and will cause splite
	public static final String SEP_TAG_NUMBER = "zSTNz";//"¿";
	public static final String SEP_LEFT_RIGHT = "zSLRz";// "¬";
	public static final String SEP_ITEM = "zSISz";//"¯";
	public static final String MARK_PUBLIC_TAG = "zMUTz";// "¶";
	public static final String MARK_PRIVATE_TAG = "zMITz";// "";
	public static final String MARK_MEMBERONLY_TAG = "zMMTz";// "®";
	public static final String MARK_TBD_TAG = "zMTTz";// "©";
	public static final int MARK_SEP_LENGTH = 5;
	
	public static String getUTFString(String pString){
		byte tByteAry[];
		try{
			tByteAry = pString.getBytes("ISO-8859-1");
			pString = new String(tByteAry, "UTF-8");
		}catch(Exception e){
			//so it's not "ISO-8859-1" encoded.
		}
		return pString;
	}

	public static boolean isSystemCommand(String pCommand, UserAccount pCurUser){
		if("5203_setDefaultValueForContents".equals(pCommand)){
			setDefaultValueForContents();
			return true;
		} else if("2745_setDefaultValueForTags".equals(pCommand)){
			setDefaultValueForTags();
			return true;
		} else if("1214_updateUserBalances".equals(pCommand)){
			SpringApplicationContext.getApplicationContext().getBean("updatingBalanceJobProcessor", UpdatingBalanceJobProcessor.class).updateBalance();
			return true;
		} else if(("1210_syncdb".equals(pCommand) || "1210_syncdb_ua".equals(pCommand) || "1210_syncdb_tg".equals(pCommand)
				 || "1210_syncdb_ms".equals(pCommand)  || "1210_syncdb_bg".equals(pCommand) || "1210_syncdb_rm".equals(pCommand)  || "1210_syncdb_bm".equals(pCommand))
				 && pCurUser != null){	//Looks like first run will see exception of line 137, SynchizationManager(persistant rool back). will be OK when run it the second time or the third time.
			new ClientSyncTool().startToSynch(pCurUser, pCommand);
			return true;
		} else if(pCommand != null && pCommand.startsWith("20130818_shufful") && pCurUser != null){
			String tName = pCommand.substring("20130818_shufful".length()).trim();
			String tTagName = null;				//if there's a space in param, means, user specify not only a username but also a tag.
			int tPos = tName.indexOf(' ');		//that also means if the username contains a space, then his bookmark can not be shuttled.
			if(tPos > 0){
				tName = tName.substring(0, tPos);
				tTagName = tName.substring(tPos + 1);
			}
			UserAccount tUA = UserAccount.findUserAccountByName(tName);
			if(tUA != null){
				List<Content> tList = Content.findContentsByPublisher(tUA, BigAuthority.getAuthSet(tUA,tUA), 0, 0, null);
				if(tList != null){
					for (int i = 0; i < tList.size(); i++){
						Content tBM = tList.get(i);
						if(tTagName != null && !tTagName.equals(tBM.getCommonBigTag().getTagName()))
							continue;	//if the content is not in the categray defined in parameter, then don't change.
						
						UserAccount tGhostUA = getGhostUA();
						tBM.setPublisher(tGhostUA);
						if(tBM.getUncommonBigTag() != null){
							BigTag tTag = BigTag.findBMTagByNameAndOwner(tBM.getUncommonBigTag().getTagName(), tGhostUA.getName());
							if(tTag != null)
								tBM.setUncommonBigTag(tTag);
						}
						tBM.persist();
					}
				}
			}
		}
		
		return false;
	}
	
	public static List<BigTag> transferToTags(String[] tAryTagStrs, String pOwnerName){
    	List<BigTag> tBigTags = new ArrayList<BigTag>();
    	for(int i = 0; i < tAryTagStrs.length; i++){
    		//System.out.println("i:" + i);
    		//System.out.println("tAryTagStrs[i]:" + tAryTagStrs[i]);
    		if(tAryTagStrs[i].endsWith(MARK_PRIVATE_TAG) ||tAryTagStrs[i].endsWith(MARK_MEMBERONLY_TAG) ||tAryTagStrs[i].endsWith(MARK_TBD_TAG))
    			tAryTagStrs[i] = tAryTagStrs[i].substring(0, tAryTagStrs[i].length() - MARK_SEP_LENGTH);
    		
    		if(tAryTagStrs[i].startsWith(MARK_PUBLIC_TAG)){
    			BigTag tTag = BigTag.findBMTagByNameAndOwner(tAryTagStrs[i].substring(MARK_SEP_LENGTH), "admin");
    			if(tTag != null)
    				tBigTags.add(tTag);
    			else{
    				tTag = BigTag.findBMTagByNameAndOwner(tAryTagStrs[i].substring(MARK_SEP_LENGTH), "administrator");
    				if(tTag != null)
    					tBigTags.add(tTag);
    			}
    		}else{
    			BigTag tTag = BigTag.findBMTagByNameAndOwner(tAryTagStrs[i], pOwnerName);
    			if(tTag != null)
    				tBigTags.add(tTag);
    		}
    	}
    	return tBigTags;
    }
	
	/**
	 * update the lastupdate field of twitter.
	 */
	public static void refreshULastUpdateTimeOfTwitter(Remark remark){
		remark = Remark.findRemark(remark.getId());	//this remark may got from webpage, and has no some field like "remarkto"
        Twitter tTwitter = remark.getRemarkto();
        tTwitter.setLastupdate(remark.getRemarkTime());
        tTwitter.merge();
	}
	
	//reset the columns to be displayed on personal space, by default, we display only user's own and his friends tags.
	//if user want to display more tags, he can customize it later easily.
	public static void resetLayoutString(UserAccount pUser){
    	if(pUser == null) return;
    	
		List<BigTag> tBigTags = BigTag.findBMTagsByOwner(pUser.getName()); 	//fetch out all tags of admin's or owner's and his team's, 
    	int tSize = tBigTags.size();									//Separate tags and IDs into 2 columns and prepare the Layout String.
    	
    	StringBuilder tStrB = new StringBuilder();
    	StringBuilder tStrB_Num = new StringBuilder();
		for(int j = 0; j < tSize/2; j++){
	    	tStrB.append(getTagInLayoutString(tBigTags.get(j)));
	    	
	    	tStrB_Num.append("8");
	    	
	    	if(j + 1 < tSize/2){
	    		tStrB.append(SEP_ITEM);
    	    	tStrB_Num.append(SEP_ITEM);
	    	}
    	}

		tStrB.append(SEP_LEFT_RIGHT);
		tStrB_Num.append(SEP_LEFT_RIGHT);
		
		for(int j = tSize/2; j < tSize; j++){
	    	tStrB.append(getTagInLayoutString(tBigTags.get(j)));
	    	
	    	tStrB_Num.append("8");
	    	
	    	if(j + 1 < tSize){
	    		tStrB.append(SEP_ITEM);
    	    	tStrB_Num.append(SEP_ITEM);
	    	}
    	}
		tStrB.append(SEP_TAG_NUMBER).append(tStrB_Num);

		pUser.setLayout(tStrB.toString());	    						//save to DB
		pUser.persist();
	}
	
	public static String getTagInLayoutString(BigTag pTag){
		
		StringBuilder tStrB = new StringBuilder();
		
		if("admin".equals(pTag.getType()) || "administrator".equals(pTag.getType()))
    		tStrB.append(MARK_PUBLIC_TAG);
    	
    	tStrB.append(pTag.getTagName());
    	
    	switch (pTag.getAuthority()) {
		case BigAuthority.ONLY_MYSELF_CAN_SEE:
			tStrB.append(MARK_PRIVATE_TAG);
			break;
		case BigAuthority.ALL_MY_TEAM_CAN_SEE:
			tStrB.append(MARK_MEMBERONLY_TAG);
			break;
		case BigAuthority.ONLY_FOR_SELECTED_PERSON:
			tStrB.append(MARK_TBD_TAG);
			break;
		default:
			break;
		}
    	
    	return tStrB.toString();
	}
	
	//transfer string form "in layout string" format to normal format (clean tag name format).
	public static String getTagNameFromLayoutStr(String pLayoutString){
		StringBuilder tStrB = new StringBuilder(pLayoutString);
		if(tStrB.indexOf(MARK_PUBLIC_TAG) == 0 || tStrB.indexOf(MARK_PRIVATE_TAG) == 0)  //remove the prefix.
			tStrB = tStrB.delete(0, MARK_SEP_LENGTH);
		
		if(tStrB.indexOf(MARK_PUBLIC_TAG) > -1 || tStrB.indexOf(MARK_PRIVATE_TAG) > -1 || tStrB.indexOf(SEP_TAG_NUMBER) > -1 )  //remove the affix.
			tStrB = tStrB.delete(tStrB.length() - MARK_SEP_LENGTH, tStrB.length());
		
		return tStrB.toString();
	}
	
    public static boolean notCorrect(String[] tAryTagStrsLeft, String[] tAryTagStrsRight, String[] tAryNumStrsLeft, String[] tAryNumStrsRight){
    	if((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0) && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
    		return true;
		if((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0) && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
			return true;
		if(tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)
			return true;
		try{
			if(!(tAryNumStrsLeft.length ==1 && tAryNumStrsLeft[0].length() == 0))		//in case that when the left column or right column have no tag to show, the string will be ""
				for(int i = tAryNumStrsLeft.length - 1; i >= 0; i--){
					Integer.parseInt(tAryNumStrsLeft[i]);
				}
			if(!(tAryNumStrsRight.length == 1 && tAryNumStrsRight[0].length() == 0))	//and when a "" is splid, the array returned will have one element, and it's "".so we allow "".
				for(int i = tAryNumStrsRight.length - 1; i >= 0; i--){
					Integer.parseInt(tAryNumStrsRight[i]);	
				}
		}catch(Exception e){
			return true;
		}
		
		return false;
    }
    
	private static void setDefaultValueForTags(){
		List<BigTag> tList = BigTag.findAllBigTags();
		for(int i = 0; i < tList.size(); i++){
			BigTag tBigTag = tList.get(i);
			
			if(tBigTag.getAuthority() == null){
				tBigTag.setAuthority(new Integer(0));
				tBigTag.persist();
			}else if(!(tBigTag.getAuthority() instanceof Integer)){
				//System.out.println("N : " + tBigTag.getAuthority());
				tBigTag.setAuthority(new Integer(0));
				tBigTag.persist();
			}
			
			if(tBigTag.getOwner() == null){
				tBigTag.setOwner(new Integer(0));
				tBigTag.persist();
			}
		}
	}
	
	private static void setDefaultValueForContents(){
		List<Content> tList = Content.findAllContents();
		for(int i = 0; i < tList.size(); i++){
			Content tContent = tList.get(i);
			if(tContent.getAuthority() == null){
				tContent.setAuthority(new Integer(0));
				tContent.persist();
			}else if(tContent.getAuthority() instanceof Integer){
				//System.out.println("Y : " + tContent.getAuthority().intValue());
			}else{
				//System.out.println("N : " + tContent.getAuthority());
				tContent.setAuthority(new Integer(0));
				tContent.persist();
			}
		}
	}
	
	private static List<UserAccount> tGhostUAList;
	private static UserAccount getGhostUA(){
		if(tGhostUAList == null){
			tGhostUAList = new ArrayList<UserAccount>();
			UserAccount tU0 = UserAccount.findUserAccountByName("mwang");
			UserAccount tU1 = UserAccount.findUserAccountByName("gchen");
			UserAccount tU2 = UserAccount.findUserAccountByName("sha");
			UserAccount tU3 = UserAccount.findUserAccountByName("xJin");
			UserAccount tU4 = UserAccount.findUserAccountByName("gZhou");
			UserAccount tU5 = UserAccount.findUserAccountByName("AustinL");
			UserAccount tU6 = UserAccount.findUserAccountByName("James");
			UserAccount tU7 = UserAccount.findUserAccountByName("Gustsao");
			UserAccount tU8 = UserAccount.findUserAccountByName("SYLi");
			UserAccount tU9 = UserAccount.findUserAccountByName("Bobchu");
			UserAccount tU10 = UserAccount.findUserAccountByName("JackM");
			UserAccount tU11 = UserAccount.findUserAccountByName("HQ.J");
			UserAccount tU12 = UserAccount.findUserAccountByName("NancyS");
			UserAccount tU13 = UserAccount.findUserAccountByName("JaneH");
			UserAccount tU14 = UserAccount.findUserAccountByName("HerryY");
			UserAccount tU15 = UserAccount.findUserAccountByName("MarryLi");
			UserAccount tU16 = UserAccount.findUserAccountByName("MichaelM");
			UserAccount tU17 = UserAccount.findUserAccountByName("Jack99");
			UserAccount tU18 = UserAccount.findUserAccountByName("Sam2013");
			UserAccount tU19 = UserAccount.findUserAccountByName("David8");
			tGhostUAList.add(tU0);
			tGhostUAList.add(tU1);
			tGhostUAList.add(tU2);
			tGhostUAList.add(tU3);
			tGhostUAList.add(tU4);
			tGhostUAList.add(tU5);
			tGhostUAList.add(tU6);
			tGhostUAList.add(tU7);
			tGhostUAList.add(tU8);
			tGhostUAList.add(tU9);
			tGhostUAList.add(tU10);
			tGhostUAList.add(tU11);
			tGhostUAList.add(tU12);
			tGhostUAList.add(tU13);
			tGhostUAList.add(tU14);
			tGhostUAList.add(tU15);
			tGhostUAList.add(tU16);
			tGhostUAList.add(tU17);
			tGhostUAList.add(tU18);
			tGhostUAList.add(tU19);
		}
		int randomIdx = (int) ((19 - 0) * Math.random() + 0);
		return tGhostUAList.get(randomIdx);
	}
	//SpringApplicationContext.getApplicationContext().getBean("themeResolver", CookieThemeResolver.class).setDefaultThemeName("2");
	
    public static void sendMessage(String mailFrom, String subject, String mailTo, String message) {
    	MailSender tMailSender = SpringApplicationContext.getApplicationContext().getBean("mailSender", MailSender.class);
        MimeMessage mimeMessage = ((JavaMailSender)tMailSender).createMimeMessage();
        MimeMessageHelper helper = null;
        try{
        	helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
	        mimeMessage.setContent(message, "text/html;charset=utf-8");
	        helper.setTo(mailTo);
	        helper.setSubject(subject);
	        helper.setFrom(mailFrom);
        }catch(Exception e){
        	System.out.println("Sending email failed!" + mailTo + "|" + subject + "|" + message);
        }
        ((JavaMailSender)tMailSender).send(mimeMessage);
    }
    
    public static void main(String args[]){
    	String a = "abcdefiSTNijklmn";
    	String b = "iSTNi";
    	String[] c = a.split(b);
    
    }
    
    public static byte[] resizeImage(BufferedImage im, String tKeyString, String pFormat) {
    	int pToWidth = 100;
    	int pToHeight = 100;
    	
    	if(tKeyString.startsWith("uc_")){
    		if(tKeyString.endsWith("_bg")){
    			pToWidth = im.getWidth();
        		pToHeight = im.getHeight();
        		if(pToWidth * pToHeight > 25000){  //so the thin lines texture are allowed.
	        		if(pToWidth > 123){	//check if the width are too big?
	        			pToWidth = 123;
	        			pToHeight = im.getHeight() * 123 / im.getWidth();
	        		}
	        		if(pToHeight > 187){	//width is already under 1370, if the height are still too big, modify again!
	        			pToHeight = 187;
	        			pToWidth = im.getWidth() * 187 / im.getHeight();
	        		}
        		}
    		}else if(tKeyString.endsWith("_headimage")){
    			pToWidth = im.getWidth();
        		pToHeight = im.getHeight();
        		if(pToWidth > 800){	//check if the width are too big?
        			pToWidth = 800;
        			pToHeight = im.getHeight() * 800 / im.getWidth();
        		}
        		if(pToHeight > 200){	//width is already under 1370, if the height are still too big, modify again!
        			pToHeight = 200;
        			pToWidth = im.getWidth() * 200 / im.getHeight();
        		}
    		}else{
        		return null;
    		}
    	}else{
    		return null;
    	}
    	
    	BufferedImage inputbig = new BufferedImage(pToWidth, pToHeight, BufferedImage.TYPE_INT_BGR);
		//inputbig.getGraphics().drawImage(im, 0, 0, pToWidth, pToHeight, null);	//the created thum image is not clear enough with this way.
        inputbig.getGraphics().drawImage(im.getScaledInstance(pToWidth, pToHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);	//this way is better.
		
		byte[] bFR = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        if(pFormat.startsWith("."))
        	pFormat = pFormat.substring(1);
        
        try{
	        ImageIO.write(inputbig, pFormat, out);  
	        bFR = out.toByteArray(); 
        }catch(Exception e){
        	e.printStackTrace();
        }
        return bFR;
    }

    public static void checkTheme(UserAccount tOwner, HttpServletRequest httpServletRequest){
        //if the owner has setted theme, then use the theme! (will effect only on this request)
    	int tTheme = tOwner.getTheme();
    	if(tTheme != 0)
    		httpServletRequest.setAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME, String.valueOf(tTheme));
    }
}