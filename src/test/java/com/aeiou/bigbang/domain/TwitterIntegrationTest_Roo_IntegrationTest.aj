// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.TwitterDataOnDemand;
import com.aeiou.bigbang.domain.TwitterIntegrationTest;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect TwitterIntegrationTest_Roo_IntegrationTest {
    
    declare @type: TwitterIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: TwitterIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml");
    
    declare @type: TwitterIntegrationTest: @Transactional;
    
    @Autowired
    private TwitterDataOnDemand TwitterIntegrationTest.dod;
    
    @Test
    public void TwitterIntegrationTest.testCountTwitters() {
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", dod.getRandomTwitter());
        long count = Twitter.countTwitters();
        Assert.assertTrue("Counter for 'Twitter' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void TwitterIntegrationTest.testFindTwitter() {
        Twitter obj = dod.getRandomTwitter();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to provide an identifier", id);
        obj = Twitter.findTwitter(id);
        Assert.assertNotNull("Find method for 'Twitter' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'Twitter' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void TwitterIntegrationTest.testFindAllTwitters() {
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", dod.getRandomTwitter());
        long count = Twitter.countTwitters();
        Assert.assertTrue("Too expensive to perform a find all test for 'Twitter', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<Twitter> result = Twitter.findAllTwitters();
        Assert.assertNotNull("Find all method for 'Twitter' illegally returned null", result);
        Assert.assertTrue("Find all method for 'Twitter' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void TwitterIntegrationTest.testFindTwitterEntries() {
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", dod.getRandomTwitter());
        long count = Twitter.countTwitters();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Twitter> result = Twitter.findTwitterEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'Twitter' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'Twitter' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void TwitterIntegrationTest.testFlush() {
        Twitter obj = dod.getRandomTwitter();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to provide an identifier", id);
        obj = Twitter.findTwitter(id);
        Assert.assertNotNull("Find method for 'Twitter' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyTwitter(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'Twitter' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void TwitterIntegrationTest.testMergeUpdate() {
        Twitter obj = dod.getRandomTwitter();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to provide an identifier", id);
        obj = Twitter.findTwitter(id);
        boolean modified =  dod.modifyTwitter(obj);
        Integer currentVersion = obj.getVersion();
        Twitter merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'Twitter' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void TwitterIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", dod.getRandomTwitter());
        Twitter obj = dod.getNewTransientTwitter(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'Twitter' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'Twitter' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        Assert.assertNotNull("Expected 'Twitter' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void TwitterIntegrationTest.testRemove() {
        Twitter obj = dod.getRandomTwitter();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Twitter' failed to provide an identifier", id);
        obj = Twitter.findTwitter(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'Twitter' with identifier '" + id + "'", Twitter.findTwitter(id));
    }
    
}
