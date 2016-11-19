package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.MessageSource;

import com.aeiou.bigbang.domain.UserAccount;

public class BigAuthority {
    public static final int SHOW_TO_EVERY_ONE = 0;
    public static final int ONLY_MYSELF_CAN_SEE = 1;
    public static final int ALL_MY_TEAM_CAN_SEE = 2;
    public static final int ONLY_FOR_SELECTED_PERSON = 3;
    public static final int ONLY_FOR_RECEIVER = 11;

    private Short id;
    private MessageSource messageSource;
    private Locale locale;

    public BigAuthority(MessageSource pMessageSource, Locale pLocale) {
        messageSource = pMessageSource;
        locale = pLocale;
    }

    // TODO: change to support customization.
    public String toString() {
        if (id == SHOW_TO_EVERY_ONE) {
            return messageSource.getMessage("SHOW_TO_EVERY_ONE", null, locale);
        } else if (id == ONLY_MYSELF_CAN_SEE) {
            return messageSource.getMessage("ONLY_MYSELF_CAN_SEE", null, locale);
        } else if (id == ALL_MY_TEAM_CAN_SEE) {
            return messageSource.getMessage("ALL_MY_TEAM_CAN_SEE", null, locale);
        } else if (id == ONLY_FOR_SELECTED_PERSON) {
            return messageSource.getMessage("ONLY_FOR_SELECTED_PERSON", null, locale);
        } else if (id == ONLY_FOR_RECEIVER) {
            return messageSource.getMessage("ONLY_FOR_RECEIVER", null, locale);
        } else {
            return null;
        }
    }

    public static List<BigAuthority> getAllOptions(
            MessageSource messageSource,
            Locale local) {
        List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();
        for (Short i = 0; i < 3; i++) {
            BigAuthority tBigAuthority = new BigAuthority(messageSource, local);
            tBigAuthority.setId(i);
            tArrayFR.add(tBigAuthority);
        }
        return tArrayFR;
    }

    public static List<BigAuthority> getRemarkOptions(
            MessageSource messageSource,
            Locale locale) {
        List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();

        BigAuthority tBigAuthority = new BigAuthority(messageSource, locale);
        tBigAuthority.setId((short) 0);
        tArrayFR.add(tBigAuthority);
        BigAuthority tBigAuthority2 = new BigAuthority(messageSource, locale);
        tBigAuthority2.setId((short) 11);
        tArrayFR.add(tBigAuthority2);

        return tArrayFR;
    }

    public static Set<Integer> getAuthSet(
            UserAccount curUser,
            UserAccount owner) {
        Set<Integer> tAuthSetFR = new HashSet<Integer>();
        tAuthSetFR.add(Integer.valueOf(SHOW_TO_EVERY_ONE));
        if (owner.equals(curUser)) {
            tAuthSetFR.add(Integer.valueOf(ONLY_MYSELF_CAN_SEE));
            tAuthSetFR.add(Integer.valueOf(ALL_MY_TEAM_CAN_SEE));
            tAuthSetFR.add(Integer.valueOf(ONLY_FOR_SELECTED_PERSON));
            tAuthSetFR.add(Integer.valueOf(ONLY_FOR_RECEIVER));
        } else if (owner.getListento().contains(curUser)) {
            tAuthSetFR.add(Integer.valueOf(ALL_MY_TEAM_CAN_SEE));
        } else {// TODO: consider the case that visible to specific person.

        }
        return tAuthSetFR;
    }

    public static Set<Integer> getAuthSetForFans() {
        Set<Integer> tAuthSetFR = new HashSet<Integer>();
        tAuthSetFR.add(Integer.valueOf(SHOW_TO_EVERY_ONE));
        tAuthSetFR.add(Integer.valueOf(ALL_MY_TEAM_CAN_SEE));
        return tAuthSetFR;
    }

    public static Set<Integer> getAuthSetForNonFans() {
        Set<Integer> tAuthSetFR = new HashSet<Integer>();
        tAuthSetFR.add(Integer.valueOf(SHOW_TO_EVERY_ONE));
        return tAuthSetFR;
    }

    public static Set<Integer> getAuthSetForTwitterOfFriends(
            UserAccount pCurUser,
            UserAccount pOwner) {
        Set<Integer> tAuthSetFR = new HashSet<Integer>();
        tAuthSetFR.add(Integer.valueOf(SHOW_TO_EVERY_ONE));
        if (pOwner.equals(pCurUser))
            tAuthSetFR.add(Integer.valueOf(ALL_MY_TEAM_CAN_SEE));
        return tAuthSetFR;
    }

    public Short getId() {
        return id;
    }

    public void setId(
            Short id) {
        this.id = id;
    }
}
