// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.CustomizeDataOnDemand;
import com.aeiou.bigbang.domain.CustomizeIntegrationTest;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect CustomizeIntegrationTest_Roo_IntegrationTest {
    
    declare @type: CustomizeIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: CustomizeIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml");
    
    declare @type: CustomizeIntegrationTest: @Transactional;
    
    @Autowired
    private CustomizeDataOnDemand CustomizeIntegrationTest.dod;
    
    @Test
    public void CustomizeIntegrationTest.testCountCustomizes() {
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", dod.getRandomCustomize());
        long count = Customize.countCustomizes();
        Assert.assertTrue("Counter for 'Customize' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void CustomizeIntegrationTest.testFindCustomize() {
        Customize obj = dod.getRandomCustomize();
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Customize' failed to provide an identifier", id);
        obj = Customize.findCustomize(id);
        Assert.assertNotNull("Find method for 'Customize' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'Customize' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void CustomizeIntegrationTest.testFindAllCustomizes() {
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", dod.getRandomCustomize());
        long count = Customize.countCustomizes();
        Assert.assertTrue("Too expensive to perform a find all test for 'Customize', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<Customize> result = Customize.findAllCustomizes();
        Assert.assertNotNull("Find all method for 'Customize' illegally returned null", result);
        Assert.assertTrue("Find all method for 'Customize' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void CustomizeIntegrationTest.testFindCustomizeEntries() {
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", dod.getRandomCustomize());
        long count = Customize.countCustomizes();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Customize> result = Customize.findCustomizeEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'Customize' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'Customize' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void CustomizeIntegrationTest.testFlush() {
        Customize obj = dod.getRandomCustomize();
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Customize' failed to provide an identifier", id);
        obj = Customize.findCustomize(id);
        Assert.assertNotNull("Find method for 'Customize' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyCustomize(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'Customize' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void CustomizeIntegrationTest.testMergeUpdate() {
        Customize obj = dod.getRandomCustomize();
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Customize' failed to provide an identifier", id);
        obj = Customize.findCustomize(id);
        boolean modified =  dod.modifyCustomize(obj);
        Integer currentVersion = obj.getVersion();
        Customize merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'Customize' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void CustomizeIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", dod.getRandomCustomize());
        Customize obj = dod.getNewTransientCustomize(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'Customize' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'Customize' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        Assert.assertNotNull("Expected 'Customize' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void CustomizeIntegrationTest.testRemove() {
        Customize obj = dod.getRandomCustomize();
        Assert.assertNotNull("Data on demand for 'Customize' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Customize' failed to provide an identifier", id);
        obj = Customize.findCustomize(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'Customize' with identifier '" + id + "'", Customize.findCustomize(id));
    }
    
}
