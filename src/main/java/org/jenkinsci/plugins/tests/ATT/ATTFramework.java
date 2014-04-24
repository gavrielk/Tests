/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.tests.ATT;

import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.jenkinsci.plugins.tests.ATT.ATTRepository.TestSuite.Test;
import org.jenkinsci.plugins.tests.Group;
import org.jenkinsci.plugins.tests.ITest;
import org.jenkinsci.plugins.tests.ITestTool;

/**
 *
 * @author gavrielk
 */
public class ATTFramework implements ITestTool
{
    String testsRepositoryXMLPath = "";
    String name;
    ArrayList<ITest> tests = new ArrayList<>();
    ArrayList<Group> groups = new ArrayList<>();
    JAXBContext jaxbContext;
    
    public ATTFramework() throws JAXBException
    {
        String username = System.getProperty("user.name");
        this.testsRepositoryXMLPath = "/home/" + username + "/BuildSystem/cc-views/" + username + "_ATT_1.1_int/vobs/HostTool_AT/PyATT/Test_Suite/tests_repository.xml";
        
        ClassLoader cl = org.jenkinsci.plugins.tests.ATT.ObjectFactory.class.getClassLoader();
        this.jaxbContext = JAXBContext.newInstance("org.jenkinsci.plugins.tests.ATT", cl);
    }
    
    public String getName()
    {
        return "ATT";
    }

    @Override
    public ArrayList<ITest> getTests() throws JAXBException
    {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ATTRepositoryValidationEventHandler());
        File testsRepositoryFile = new File(testsRepositoryXMLPath);
        Source source = new StreamSource(testsRepositoryFile);
        JAXBElement<ATTRepository> AttRepositoryElement = (JAXBElement<ATTRepository>) unmarshaller.unmarshal(source, ATTRepository.class);
        
        for (Test test : AttRepositoryElement.getValue().testSuite.getTest())
        {
            ATTTest attTest = new ATTTest(test.getName(), test.getPackage(), test.getModule(), test.getDescription());
            this.tests.add(attTest);
        }
        
        return this.tests;
    }
    
    public ArrayList<Group> getTestsByGroups() throws JAXBException
    {
        if (this.tests.isEmpty() == true)
        {
            getTests();
        }
        sortTestsToGroups();
        
        return this.groups;
    }

    private void sortTestsToGroups() 
    {
        
        for(ITest test : this.tests)
        {
            boolean addedCurrTest = false;
            for (Group group : this.groups)
            {
                if (group.getName().equals(test.getGroup()) == true)
                {
                    addedCurrTest = group.add(test);
                }
            }
            if (addedCurrTest == false)
            {
                Group group = new Group(test.getGroup());
                group.add(test);
                this.groups.add(group);
            }
        }
    }
    
    
    
    
    
    
    
    public class ATTRepositoryValidationEventHandler implements ValidationEventHandler
    {
        @Override
        public boolean handleEvent(ValidationEvent ve) {
            if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
                    || ve.getSeverity() == ValidationEvent.ERROR) {
                ValidationEventLocator locator = ve.getLocator();
                //Print message from valdation event
                System.out.println("Invalid booking document: "
                        + locator.getURL());
                System.out.println("Error: " + ve.getMessage());
                //Output line and column number
                System.out.println("Error at column "
                        + locator.getColumnNumber()
                        + ", line "
                        + locator.getLineNumber());
            }
            return true;
        }
    }
}
