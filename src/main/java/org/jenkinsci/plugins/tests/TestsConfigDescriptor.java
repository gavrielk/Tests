package org.jenkinsci.plugins.tests;
import hudson.model.Descriptor;


public abstract class TestsConfigDescriptor extends Descriptor<TestsManager> {

        @Override
        public String getDisplayName() {
            System.out.println("in TestsConfigDescriptor getDisplayName()");
            return clazz.getSimpleName();
        }

}