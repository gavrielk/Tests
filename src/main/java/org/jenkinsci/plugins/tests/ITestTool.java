/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.tests;

import java.util.ArrayList;
import javax.xml.bind.JAXBException;

/**
 *
 * @author gavrielk
 */
public interface ITestTool {
//    ArrayList<Test> testList = null;
    
    ArrayList<ITest> getTests() throws JAXBException;
    
    
}
