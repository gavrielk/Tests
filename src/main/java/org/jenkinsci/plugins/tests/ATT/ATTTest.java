//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.22 at 05:15:13 PM IDT 
//


package org.jenkinsci.plugins.tests.ATT;

import org.jenkinsci.plugins.tests.ITest;

public class ATTTest implements ITest{

    String name;
    String group;
    String module;
    String description;

    public ATTTest(String name, String group, String module, String description)
    {
        this.name = name;
        this.group = group;
        this.module = module;
        this.description = description;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getGroup() {
        return this.group;
    }
        
}
