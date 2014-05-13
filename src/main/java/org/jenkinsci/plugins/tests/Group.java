/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.tests;

import java.util.ArrayList;

/**
 *
 * @author gavrielk
 */
public class Group 
{
    String groupName = "";
    ArrayList<ITest> tests = new ArrayList<>();
    
    public Group(String name)
    {
        this.groupName = name;
    }
    
    public boolean add(ITest test)
    {
        return tests.add(test);
    }
    
    public ArrayList<ITest> getTests()
    {
        return tests;
    }
    
    public String getName()
    {
        return groupName;
    }
}