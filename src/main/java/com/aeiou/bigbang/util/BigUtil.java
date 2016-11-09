package com.aeiou.bigbang.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.Model;
import org.springframework.web.servlet.theme.CookieThemeResolver;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.model.MediaUpload;
import com.aeiou.bigbang.services.quartz.UpdatingBalanceJobProcessor;
import com.aeiou.bigbang.services.synchronization.ClientSyncTool;

public class BigUtil {

    private static final Logger log = LoggerFactory.getLogger(BigUtil.class);

    public static String DEFAULT_IMAGE_TYPE = ".jpg";

    // Can not use strange characters, because when the coding formmat of the IDE changes or are not same with database,
    // will cause mismatch.
    // Can neiter use "[","(","+".... because the string will be considered as an expression in split method, those
    // character have special
    // meaning and will cause splite
    public static final String SEP_TAG_NUMBER = "zSTNz";// "�";
    public static final String SEP_LEFT_RIGHT = "zSLRz";// "�";
    public static final String SEP_ITEM = "zSISz";// "�";
    public static final String MARK_PUBLIC_TAG = "zMUTz";// "�";
    public static final String MARK_PRIVATE_TAG = "zMITz";// "";
    public static final String MARK_MEMBERONLY_TAG = "zMMTz";// "�";
    public static final String MARK_TBD_TAG = "zMTTz";// "�";
    public static final int MARK_SEP_LENGTH = 5;

    public static String getUTFString(
            String pString) {
        byte tByteAry[];
        try {
            tByteAry = pString.getBytes("ISO-8859-1");
            pString = new String(tByteAry, "UTF-8");
        } catch (Exception e) {
            // so it's not "ISO-8859-1" encoded.
        }
        return pString;
    }

    public static boolean isSystemCommand(
            String pCommand,
            UserAccount pCurUser) {
        if ("5203_setDefaultValueForContents".equals(pCommand)) {
            setDefaultValueForContents();
            return true;
        } else if ("2745_setDefaultValueForTags".equals(pCommand)) {
            setDefaultValueForTags();
            return true;
        } else if ("1214_updateUserBalances".equals(pCommand)) {
            SpringApplicationContext.getApplicationContext()
                    .getBean("updatingBalanceJobProcessor", UpdatingBalanceJobProcessor.class).updateBalance();
            return true;
        } else if (("1210_syncdb".equals(pCommand) || "1210_syncdb_ua".equals(pCommand)
                || "1210_syncdb_tg".equals(pCommand) || "1210_syncdb_ms".equals(pCommand)
                || "1210_syncdb_bg".equals(pCommand) || "1210_syncdb_rm".equals(pCommand) || "1210_syncdb_bm"
                    .equals(pCommand)) && pCurUser != null) { // Looks like first run will see exception of line 137,
                                                              // SynchizationManager(persistant rool back). will be OK
                                                              // when run it the second time or the third time.
            new ClientSyncTool().startToSynch(pCurUser, pCommand);
            return true;
        } else if (pCommand != null && pCommand.startsWith("20130818_shufful") && pCurUser != null) {
            String tName = pCommand.substring("20130818_shufful".length()).trim();
            String tTagName = null; // if there's a space in param, means, user specify not only a username but also a
                                    // tag.
            int tPos = tName.indexOf(' '); // that also means if the username contains a space, then his bookmark can
                                           // not be shuttled.
            if (tPos > 0) {
                tName = tName.substring(0, tPos);
                tTagName = tName.substring(tPos + 1);
            }
            UserAccount tUA = UserAccount.findUserAccountByName(tName);
            if (tUA != null) {
                List<Content> tList =
                        Content.findContentsByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
                if (tList != null) {
                    for (int i = 0; i < tList.size(); i++) {
                        Content tBM = tList.get(i);
                        if (tTagName != null && !tTagName.equals(tBM.getCommonBigTag().getTagName()))
                            continue; // if the content is not in the categray defined in parameter, then don't change.

                        UserAccount tGhostUA = getGhostUA();
                        tBM.setPublisher(tGhostUA);
                        if (tBM.getUncommonBigTag() != null) {
                            BigTag tTag =
                                    BigTag.findBMTagByNameAndOwner(tBM.getUncommonBigTag().getTagName(),
                                            tGhostUA.getName());
                            if (tTag != null)
                                tBM.setUncommonBigTag(tTag);
                        }
                        tBM.persist();
                    }
                }
            }
        } else if (pCommand != null && pCommand.startsWith("tianjiaceshishuju")) {
            String a = pCommand.substring("tianjiaceshishuju".length() + 1);
            int count = Integer.valueOf(a);
            for (int i = 1342; i < count; i++) {
                MediaUpload tMedia = new MediaUpload();
                tMedia.setContent(new byte[100]);
                tMedia.setFilepath("test_" + i);
                tMedia.setContentType("test");
                tMedia.setFilesize(12345);
                tMedia.persist();
            }
        }

        return false;
    }

    public static List<BigTag> convertTagArrayToList(
            String[] tAryTagStrs,
            String pOwnerName) {
        List<BigTag> tBigTags = new ArrayList<BigTag>();
        for (String tagStr : tAryTagStrs) {
            // System.out.println("i:" + i);
            // System.out.println("tAryTagStrs[i]:" + tAryTagStrs[i]);
            if (tagStr.endsWith(MARK_PRIVATE_TAG) || tagStr.endsWith(MARK_MEMBERONLY_TAG)
                    || tagStr.endsWith(MARK_TBD_TAG))
                tagStr = tagStr.substring(0, tagStr.length() - MARK_SEP_LENGTH);

            if (tagStr.startsWith(MARK_PUBLIC_TAG)) {
                BigTag tTag = BigTag.findBMTagByNameAndOwner(tagStr.substring(MARK_SEP_LENGTH), "admin");
                if (tTag != null) {
                    tBigTags.add(tTag);
                } else {
                    tTag = BigTag.findBMTagByNameAndOwner(tagStr.substring(MARK_SEP_LENGTH), "administrator");
                    if (tTag != null)
                        tBigTags.add(tTag);
                }
            } else {
                BigTag tTag = BigTag.findBMTagByNameAndOwner(tagStr, pOwnerName);
                if (tTag != null)
                    tBigTags.add(tTag);
            }
        }
        return tBigTags;
    }

    /**
     * update the lastupdate field of twitter.
     */
    public static void refreshULastUpdateTimeOfTwitter(
            Remark remark) {
        remark = Remark.findRemark(remark.getId()); // this remark may got from webpage, and has no some field like
                                                    // "remarkto"
        Twitter tTwitter = remark.getRemarkto();
        tTwitter.setLastupdate(remark.getRemarkTime());
        tTwitter.merge();
    }

    // reset the columns to be displayed on personal space, by default, we display only user's own and his friends tags.
    // if user want to display more tags, he can customize it later easily.
    public static void resetLayoutString(
            UserAccount pUser) {
        if (pUser == null)
            return;

        List<BigTag> tBigTags = BigTag.findBMTagsByOwner(pUser.getName()); // fetch out all tags of admin's or owner's
                                                                           // and his team's,
        int tSize = tBigTags.size(); // Separate tags and IDs into 2 columns and prepare the Layout String.

        StringBuilder tStrB = new StringBuilder();
        StringBuilder tStrB_Num = new StringBuilder();
        for (int j = 0; j < tSize / 2; j++) {
            tStrB.append(getTagInLayoutString(tBigTags.get(j)));

            tStrB_Num.append("8");

            if (j + 1 < tSize / 2) {
                tStrB.append(SEP_ITEM);
                tStrB_Num.append(SEP_ITEM);
            }
        }

        tStrB.append(SEP_LEFT_RIGHT);
        tStrB_Num.append(SEP_LEFT_RIGHT);

        for (int j = tSize / 2; j < tSize; j++) {
            tStrB.append(getTagInLayoutString(tBigTags.get(j)));

            tStrB_Num.append("8");

            if (j + 1 < tSize) {
                tStrB.append(SEP_ITEM);
                tStrB_Num.append(SEP_ITEM);
            }
        }
        tStrB.append(SEP_TAG_NUMBER).append(tStrB_Num);

        pUser.setLayout(tStrB.toString()); // save to DB
        pUser.persist();
    }

    public static String getTagInLayoutString(
            BigTag pTag) {

        StringBuilder tStrB = new StringBuilder();

        if ("admin".equals(pTag.getType()) || "administrator".equals(pTag.getType()))
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

    // transfer string form "in layout string" format to normal format (clean tag name format).
    public static String getTagNameFromLayoutStr(
            String pLayoutString) {
        StringBuilder tStrB = new StringBuilder(pLayoutString);
        if (tStrB.indexOf(MARK_PUBLIC_TAG) == 0 || tStrB.indexOf(MARK_PRIVATE_TAG) == 0) // remove the prefix.
            tStrB = tStrB.delete(0, MARK_SEP_LENGTH);

        if (tStrB.indexOf(MARK_PUBLIC_TAG) > -1 || tStrB.indexOf(MARK_PRIVATE_TAG) > -1
                || tStrB.indexOf(SEP_TAG_NUMBER) > -1) // remove the affix.
            tStrB = tStrB.delete(tStrB.length() - MARK_SEP_LENGTH, tStrB.length());

        return tStrB.toString();
    }

    public static boolean notCorrect(
            List<String[]> tTagsAndNums) {
        String[] tAryTagStrsLeft = tTagsAndNums.get(0);
        String[] tAryTagStrsRight = tTagsAndNums.get(1);
        String[] tAryNumStrsLeft = tTagsAndNums.get(2);
        String[] tAryNumStrsRight = tTagsAndNums.get(3);
        return notCorrect(tAryTagStrsLeft, tAryTagStrsRight, tAryNumStrsLeft, tAryNumStrsRight);
    }

    public static boolean notCorrect(
            String[] tAryTagStrsLeft,
            String[] tAryTagStrsRight,
            String[] tAryNumStrsLeft,
            String[] tAryNumStrsRight) {

        if ((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0)
                && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
            return true;
        if ((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0)
                && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
            return true;
        if (tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)
            return true;
        try {
            if (!(tAryNumStrsLeft.length == 1 && tAryNumStrsLeft[0].length() == 0)) // in case that when the left column
                                                                                    // or right column have no tag to
                                                                                    // show, the string will be ""
                for (int i = tAryNumStrsLeft.length - 1; i >= 0; i--) {
                    Integer.parseInt(tAryNumStrsLeft[i]);
                }
            if (!(tAryNumStrsRight.length == 1 && tAryNumStrsRight[0].length() == 0)) // and when a "" is splid, the
                                                                                      // array returned will have one
                                                                                      // element, and it's "".so we
                                                                                      // allow "".
                for (int i = tAryNumStrsRight.length - 1; i >= 0; i--) {
                    Integer.parseInt(tAryNumStrsRight[i]);
                }
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    private static void setDefaultValueForTags() {
        List<BigTag> tList = BigTag.findAllBigTags();
        for (int i = 0; i < tList.size(); i++) {
            BigTag tBigTag = tList.get(i);

            if (tBigTag.getAuthority() == null) {
                tBigTag.setAuthority(new Integer(0));
                tBigTag.persist();
            } else if (!(tBigTag.getAuthority() instanceof Integer)) {
                // System.out.println("N : " + tBigTag.getAuthority());
                tBigTag.setAuthority(new Integer(0));
                tBigTag.persist();
            }

            if (tBigTag.getOwner() == null) {
                tBigTag.setOwner(new Integer(0));
                tBigTag.persist();
            }
        }
    }

    private static void setDefaultValueForContents() {
        List<Content> tList = Content.findAllContents();
        for (int i = 0; i < tList.size(); i++) {
            Content tContent = tList.get(i);
            if (tContent.getAuthority() == null) {
                tContent.setAuthority(new Integer(0));
                tContent.persist();
            } else if (tContent.getAuthority() instanceof Integer) {
                // System.out.println("Y : " + tContent.getAuthority().intValue());
            } else {
                // System.out.println("N : " + tContent.getAuthority());
                tContent.setAuthority(new Integer(0));
                tContent.persist();
            }
        }
    }

    private static List<UserAccount> tGhostUAList;

    private static UserAccount getGhostUA() {
        if (tGhostUAList == null) {
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

    // SpringApplicationContext.getApplicationContext().getBean("themeResolver",
    // CookieThemeResolver.class).setDefaultThemeName("2");

    public static void sendMessage(
            String mailFrom,
            String subject,
            String mailTo,
            String message) {
        MailSender tMailSender =
                SpringApplicationContext.getApplicationContext().getBean("mailSender", MailSender.class);
        MimeMessage mimeMessage = ((JavaMailSender) tMailSender).createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessage.setContent(message, "text/html;charset=utf-8");
            helper.setTo(mailTo);
            helper.setSubject(subject);
            helper.setFrom(mailFrom);
        } catch (Exception e) {
            System.out.println("Sending email failed!" + mailTo + "|" + subject + "|" + message);
        }
        ((JavaMailSender) tMailSender).send(mimeMessage);
    }

    public static void main(
            String args[]) {
        String a = "abcdefiSTNijklmn";
        String b = "iSTNi";
        String[] c = a.split(b);

    }

    public static byte[] resizeImage(
            BufferedImage im,
            String tKeyString,
            String pFormat,
            boolean isSpecial) {
        int pToWidth = 100;
        int pToHeight = 100;

        if (tKeyString.startsWith("uc_")) {
            if (tKeyString.endsWith("_bg")) {
                pToWidth = im.getWidth();
                pToHeight = im.getHeight();
                if (pToWidth * pToHeight > 25000 && !isSpecial) { // so the thin lines texture are allowed.
                    if (pToWidth > 123) { // check if the width are too big?
                        pToWidth = 123;
                        pToHeight = im.getHeight() * 123 / im.getWidth();
                    }
                    if (pToHeight > 187) { // width is already under 1370, if the height are still too big, modify
                                           // again!
                        pToHeight = 187;
                        pToWidth = im.getWidth() * 187 / im.getHeight();
                    }
                }
            } else if (tKeyString.endsWith("_headimage")) {
                pToWidth = im.getWidth();
                pToHeight = im.getHeight();
                if (pToWidth > 800) { // check if the width are too big?
                    pToWidth = 800;
                    pToHeight = im.getHeight() * 800 / im.getWidth();
                }
                if (pToHeight > 200) { // width is already under 1370, if the height are still too big, modify again!
                    pToHeight = 200;
                    pToWidth = im.getWidth() * 200 / im.getHeight();
                }
            } else {
                return null;
            }
        } else {
            return null;
        }

        BufferedImage inputbig = new BufferedImage(pToWidth, pToHeight, BufferedImage.TYPE_INT_BGR);
        // inputbig.getGraphics().drawImage(im, 0, 0, pToWidth, pToHeight, null); //the created thum image is not clear
        // enough with this way.
        inputbig.getGraphics().drawImage(im.getScaledInstance(pToWidth, pToHeight, java.awt.Image.SCALE_SMOOTH), 0, 0,
                null); // this way is better.

        byte[] bFR = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (pFormat.startsWith("."))
            pFormat = pFormat.substring(1);

        try {
            ImageIO.write(inputbig, pFormat, out);
            bFR = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFR;
    }

    public static void checkTheme(
            UserAccount tOwner,
            HttpServletRequest httpServletRequest) {
        // check If Its New Created User;
        if (tOwner.getName() == null) {
            tOwner = UserAccount.findUserAccountByName("admin");
        }
        httpServletRequest.setAttribute("spaceOwner", tOwner.getName());
        // if the owner has theme already, then use the theme! (will effect only on this request)
        int tTheme = tOwner.getTheme();
        if (tTheme != 0)
            httpServletRequest.setAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME, String.valueOf(tTheme));
    }

    /**
     * @NOTE: can not use the string[] as parameter, it's not like list objects.
     * @param tOwner
     * @return
     */
    public static List<String[]> fetchBookMarkTagAndNumberInListOfArrayFormat(
            UserAccount tOwner) {

        String[] tBigTagStrsLeft = null;
        String[] tBigTagStrsRight = null;
        String[] tNumStrsLeft = null;
        String[] tNumStrsRight = null;

        String tLayout = tOwner.getLayout(); // get the layout info from DB.
        int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
        if (p > -1) {
            String tTagStr = tLayout.substring(0, p);
            String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);

            p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
            if (p >= 0) {
                tBigTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
                tBigTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
            }
            p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
            if (p >= 0) {
                tNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
                tNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
            }
        }

        List<String[]> listForReturn = new ArrayList<String[]>();
        listForReturn.add(tBigTagStrsLeft);
        listForReturn.add(tBigTagStrsRight);
        listForReturn.add(tNumStrsLeft);
        listForReturn.add(tNumStrsRight);
        return listForReturn;
    }

    /**
     * @NOTE: can not use the string[] as parameter, it's not like list objects.
     * @param tOwner
     * @return
     */
    public static List<String[]> fetchNoteTagAndNumberInListOfArrayFormat(
            UserAccount tOwner) {

        String[] tBigTagStrsLeft = null;
        String[] tBigTagStrsRight = null;
        String[] tNumStrsLeft = null;
        String[] tNumStrsRight = null;

        String tLayout = tOwner.getLayout(); // get the layout info from DB.
        int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
        if (p > -1) {
            String tTagStr = tLayout.substring(0, p);
            String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);

            p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
            if (p >= 0) {
                tBigTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
                tBigTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
            }
            p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
            if (p >= 0) {
                tNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
                tNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
            }
        }

        List<String[]> listForReturn = new ArrayList<String[]>();
        listForReturn.add(tBigTagStrsLeft);
        listForReturn.add(tBigTagStrsRight);
        listForReturn.add(tNumStrsLeft);
        listForReturn.add(tNumStrsRight);
        return listForReturn;
    }

    /**
     * @ Note the list works as reference.
     * 
     * @param tOwner
     * @param tBigTagsLeft
     * @param tBigTagsRight
     * @param tTagIdsLeft
     * @param tTagIdsRight
     */
    public static List<List> resetTagsForOwner(
            UserAccount tOwner,
            HttpServletRequest httpServletRequest) {
        List<BigTag> tBigTagsLeft = new ArrayList<BigTag>();
        List<BigTag> tBigTagsRight = new ArrayList<BigTag>();
        List<Long> tTagIdsLeft = new ArrayList<Long>();
        List<Long> tTagIdsRight = new ArrayList<Long>();

        List<BigTag> tBigTags = BigTag.findBMTagsByOwner(tOwner.getName()); // fetch out all tags owner's and his
                                                                            // team's,
        List<Long> tTagIds = new ArrayList<Long>(); // then adjust it. @note: don't know if we can use AthenSet to move
                                                    // this into JPQL, because
        for (int i = 0; i < tBigTags.size(); i++) { // here, we need to compare the tag names, to avoid duplication.
            tTagIds.add(tBigTags.get(i).getId());
        }

        int tSize = tBigTags.size(); // Separate tags and IDs into 2 columns and prepare the Layout String.
        String[] tNumStrsLeft = new String[tSize / 2];
        String[] tNumStrsRight = new String[tSize - tSize / 2];

        StringBuilder tStrB = new StringBuilder();
        StringBuilder tStrB_Num = new StringBuilder();
        for (int j = 0; j < tSize / 2; j++) {
            BigTag tTag = tBigTags.get(j);
            tBigTagsLeft.add(tBigTags.get(j));
            tTagIdsLeft.add(tTagIds.get(j));

            tStrB.append(BigUtil.getTagInLayoutString(tTag));

            tNumStrsLeft[j] = "8";
            tStrB_Num.append(tNumStrsLeft[j]);

            if (j + 1 < tSize / 2) {
                tStrB.append(BigUtil.SEP_ITEM);
                tStrB_Num.append(BigUtil.SEP_ITEM);
            }
        }

        tStrB.append(BigUtil.SEP_LEFT_RIGHT);
        tStrB_Num.append(BigUtil.SEP_LEFT_RIGHT);

        for (int j = tSize / 2; j < tSize; j++) {
            BigTag tTag = tBigTags.get(j);
            tBigTagsRight.add(tBigTags.get(j));
            tTagIdsRight.add(tTagIds.get(j));

            tStrB.append(BigUtil.getTagInLayoutString(tTag));

            tNumStrsRight[j - tSize / 2] = "8";
            tStrB_Num.append(tNumStrsRight[j - tSize / 2]);

            if (j + 1 < tSize) {
                tStrB.append(BigUtil.SEP_ITEM);
                tStrB_Num.append(BigUtil.SEP_ITEM);
            }
        }
        tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tStrB_Num);

        tOwner.setLayout(tStrB.toString()); // save the correct layout string back to DB
        tOwner.persist();

        List<List> listForReturn = new ArrayList<List>();
        listForReturn.add(tBigTagsLeft);
        listForReturn.add(tBigTagsRight);
        listForReturn.add(tTagIdsLeft);
        listForReturn.add(tTagIdsRight);
        return listForReturn;
    }

    /**
     * @param bigTagsAdmin
     *            must be an empty list, it is used to be filled in with default tags.
     * @param bigTagsAdministrator
     *            must be an empty list, it is used to be filled in with default tags.
     * @param uiModel
     * @param session
     */
    public static void prepareAdminTags(
            List<BigTag> bigTagsAdmin,
            List<BigTag> bigTagsAdministrator,
            Model uiModel,
            HttpSession session) {

        List<Long> tTagIdsAdmin = new ArrayList<Long>();
        List<Long> tTagIdsAdministrator = new ArrayList<Long>();
        fetchAllSuggestedTags(bigTagsAdmin, bigTagsAdministrator, session);

        for (BigTag bigTag : bigTagsAdmin) {
            tTagIdsAdmin.add(bigTag.getId());
        }
        for (BigTag bigTag : bigTagsAdministrator) {
            tTagIdsAdministrator.add(bigTag.getId());
        }

        // set to front end model.
        uiModel.addAttribute("bigTagsLeft", bigTagsAdmin);
        uiModel.addAttribute("bigTagsRight", bigTagsAdministrator);
        uiModel.addAttribute("tagIdsLeft", tTagIdsAdmin);
        uiModel.addAttribute("tagIdsRight", tTagIdsAdministrator);
    }

    /**
     * @param bigTagsAdmin
     * @param bigTagsAdministrator
     * @param session
     */
    private static void fetchAllSuggestedTags(
            List<BigTag> bigTagsAdmin,
            List<BigTag> bigTagsAdministrator,
            HttpSession session) {
        // get out admin and administrator's tags in string.
        List<String> tBigTagStrsOfAdmin = new ArrayList<String>();
        List<String> tBigTagStrsOfAdministrator = new ArrayList<String>();

        for (int i = 1; i < 100; i++) {
            Object tTagStr = session.getAttribute("suggested_tag" + i);
            if (tTagStr == null)
                break;
            tBigTagStrsOfAdmin.add(tTagStr.toString());
        }
        for (int i = 1; i < 100; i++) {
            Object tTagStr = session.getAttribute("selectable_tag" + i);
            if (tTagStr == null)
                break;
            tBigTagStrsOfAdministrator.add(tTagStr.toString());
        }

        String[] tags = new String[tBigTagStrsOfAdmin.size()];
        bigTagsAdmin.addAll(BigUtil.convertTagArrayToList(tBigTagStrsOfAdmin.toArray(tags), "admin"));
        tags = new String[tBigTagStrsOfAdministrator.size()];
        bigTagsAdministrator.addAll(BigUtil.convertTagArrayToList(tBigTagStrsOfAdministrator.toArray(tags),
                "administrator"));
    }

    public static void prepareAdminContents(
            List<BigTag> bigTagsAdmin,
            List<BigTag> bigTagsAdministrator,
            Model uiModel,
            HttpSession session) {

        // get out relevant content (urls)
        Object cus_items_per_page = session.getAttribute("items_per_page");
        int items_per_page = cus_items_per_page == null ? 8 : Integer.valueOf(cus_items_per_page.toString());
        List<List> tContentListsLeft = new ArrayList<List>(); // prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>(); // prepare the contentList for each tag.
        for (int i = 0; i < bigTagsAdmin.size(); i++) {
            tContentListsLeft.add(Content.findContentsByTag(bigTagsAdmin.get(i), 0, items_per_page, null));
        }
        for (int i = 0; i < bigTagsAdministrator.size(); i++) {
            tContentListsRight.add(Content.findContentsByTag(bigTagsAdministrator.get(i), 0, items_per_page, null));
        }

        // set to front end model.
        uiModel.addAttribute("contentsLeft", tContentListsLeft);
        uiModel.addAttribute("contentsRight", tContentListsRight);
    }

    public static void changeUserTheme(
            Long ownerID,
            int themeNumber) {
        UserAccount tUser = UserAccount.findUserAccount(ownerID);
        tUser.setTheme(themeNumber);
        tUser.persist();
    }
}
