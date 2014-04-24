package org.jenkinsci.plugins.tests;

import org.jenkinsci.plugins.tests.ATT.ATTFramework;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.RootAction;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

@Extension
public class TestsManager implements RootAction, Describable<TestsManager> {
    
    public static String ProfilesDBPath;
    public ArrayList<ITest> testRepository;
    public ArrayList<Profile> profiles;
    
    public TestsManager()
    {
        String username = System.getProperty("user.name");
        TestsManager.ProfilesDBPath = "/home/" + username + "/profilesDB";
        this.profiles = new ArrayList<Profile>();
    }

    @Override
    public String getIconFileName() {
        if (CheckBuildPermissions() == true){
            return "/plugin/Tests/tests-icon.png";
        }
        else{
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if (CheckBuildPermissions() == true){
            return "Tests";
        }
        else{
            return null;
        }
    }

    @Override
    public String getUrlName() {
        if (CheckBuildPermissions() == true){
            return "Tests";
        }
        else{
            return null;
        }
    }
    
    private boolean CheckBuildPermissions()
    {
        for ( Permission permission : Permission.getAll())
        {
            if (permission.name.equals("Build") == true)
            {
                if (Jenkins.getInstance().hasPermission(permission) == true)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Descriptor getDescriptor() 
    {
        System.out.println("In TestsConfig getDescriptor()");
        return (TestsDescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }
    
    // Set a new Corntab entry using TimerTrigger class
    public void doSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException
    {
        String addProfileSubmit = req.getParameter("profile-add-submit");
        String removeProfileSubmit = req.getParameter("profile-remove-submit");
        String saveProfileSubmit = req.getParameter("profile-save-submit");
        
//        Iterator keySetIt = req.getParameterMap().keySet().iterator();
//        Object key;
//        while (keySetIt.hasNext())
//        {
//            key = keySetIt.next();
//            System.out.println("key: " + key.toString() + ", value: " + req.getParameter(key.toString()));
//        }
        
        if (isBlank(new String[]{addProfileSubmit}) == false)
        {
            String newProfileName = req.getParameter("profile-add-name");
            if (isBlank(new String[]{newProfileName}) == false)
            {
                String pathToNewProfileFile = TestsManager.ProfilesDBPath + "/" + newProfileName + ".profile";
                try 
                {
                    Profile profile = new Profile(new File(pathToNewProfileFile));
                    this.profiles.add(profile);
                } catch (IOException ex) {
                    System.out.println("[ERROR] Failed to create profile " + newProfileName + ", profile's file path is " + pathToNewProfileFile);
                    Logger.getLogger(TestsManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else if (isBlank(new String[]{removeProfileSubmit}) == false)
        {
            String profileName = req.getParameter("profile");
            if (isBlank(new String[]{profileName}) == false)
            {
                Profile profile = getProfileByName(profileName);
                if (profile != null)
                {
                    profile.removeFromDB();;
                    this.profiles.remove(profile);
                }
            }
        }
        else if (isBlank(new String[]{saveProfileSubmit}) == false)
        {
            String profileName = req.getParameter("profile");
            if (isBlank(new String[]{profileName}) == false)
            {
                Profile profile = getProfileByName(profileName);
                if (profile != null)
                {
                    profile.removeAllTests();
                    Object key;
                    Iterator keySetIt = req.getParameterMap().keySet().iterator();
                    while (keySetIt.hasNext())
                    {
                        key = keySetIt.next();
                        if (key.toString().startsWith("test_"))
                        {
                            String testName = req.getParameter(key.toString()).replaceFirst("test_", "");
                            System.out.println("testName=" + testName);
                            if (isBlank(new String[]{testName}) == false)
                            {
                                try{
                                    profile.add(getTestByName(testName));
                                }catch(IOException ex){
                                    System.out.println("[ERROR] Failed to add test: " + testName + ", to "+ profileName + " profile");
                                    Logger.getLogger(TestsManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
    //                    System.out.println("key: " + key.toString() + ", value: " + req.getParameter(key.toString()));
                    }
                }
                else
                {
                    System.out.println("[ERROR] profile: " +  profileName + " not found");
                }
            }
        }
        
        rsp.sendRedirect2(req.getReferer());
    }
    
    public ArrayList<Profile> getProfiles()
    {
        File profileDBDir = new File(TestsManager.ProfilesDBPath);
        if (profileDBDir.isDirectory() == true)
        {
            this.profiles =new ArrayList<Profile>();
            File[] profileFiles = profileDBDir.listFiles(new ProfilesFileFilter());
            for (File profileFile : profileFiles)
            {
                try {
                    Profile profile = new Profile(profileFile);
                    ArrayList<String> testsNameList = profile.getTestsNameList();
                    for (String testName : testsNameList)
                    {
                        ITest test = null;
                        if (this.testRepository == null)
                        {
                            getTests();
                        }
                        for (ITest testIt : this.testRepository)
                        {
                            if (testIt.getName().equals(testName) == true)
                            {
                                test = testIt;
                                break;
                            }
                        }
                        if (test != null)
                        {
                            profile.add(test);
                        }
                        else
                        {
                            System.out.println("[ERROR] Test " + testName + " is listed in the profile " + profile.getName() + " but doesn't exist in the tests repository");
                        }
                    }
                    
                    this.profiles.add(profile);
                } catch (IOException ex) {
                    System.out.println("[ERROR] Creating new profile for file: " + profileFile + " failed");
                    Logger.getLogger(TestsManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else
        {
            System.out.println("[ERROR] Profiles DB doesn't exist under " + profileDBDir.getAbsolutePath());
        }
        
        return this.profiles;
    }
    
    public ArrayList<Group> getTests()
    {
        ArrayList<Group> groupsList = null;
        try {
            ATTFramework attFramework = new ATTFramework();
            this.testRepository = attFramework.getTests();
            groupsList = attFramework.getTestsByGroups();
        } catch (JAXBException ex) {
            Logger.getLogger(TestsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return groupsList;
    }

        
    @JavaScriptMethod
    public ArrayList<String> doGetTestsInProfile(String profileName)
    {
        Profile profile = null;
        System.out.println("in doGetTestsInProfile");
        if (profileName != null)
        {
            for (Profile profileIt : this.profiles)
            {
                if (profileName.equals(profileIt.getName()))
                {
                    profile = profileIt;
                }
            }
            if (profile != null)
            {
//                Gson gson;
                for (String test : profile.getTestsNameList())
                {
                    System.out.println(test);
                }
                return profile.getTestsNameList();
            }
            else
            {
                System.out.println("Profile " + profileName + " not found");
            }
        }
        else
        {
            System.out.println("Profile name is empty");
        }
        return null;
    }
        
    @Extension
    public static final class TestsDescriptorImpl extends TestsConfigDescriptor
    {
//        ArrayList<Profile> profiles = null;
//        
//        
//        public ListBoxModel doFillProfileItems()
//        {
//            ListBoxModel m = new ListBoxModel();
//            File profileDBDir = new File(TestsManager.ProfilesDBPath);
//            if (profileDBDir.isDirectory() == true)
//            {
//                File[] profileFiles = profileDBDir.listFiles(new ProfilesFileFilter());
//                for (File profileFile : profileFiles)
//                {
//                    Profile profile = null;
//                    
//                    // Get profile name
//                    String fileName = profileFile.getName();
//                    int indexOfSuffix = fileName.indexOf('.');
//                    String profileName = fileName.substring(0, indexOfSuffix);
//                    
//                    profile = new Profile(profileName);
//                    
//                }
//                
//            }
//            return m;
//        }
//        
//        private
    } 

    private class ProfilesFileFilter implements FileFilter 
    {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".profile");
        }
    }
    
    private boolean isBlank(String[] variables)
    {
        for (String variable : variables) 
        {
            if (variable == null || variable.isEmpty() == true) {
                return true;
            }
        }
        return false;
    }

    private Profile getProfileByName(String profileName) 
    {
        for (Profile p : this.profiles)
        {
            if (p.getName().equals((profileName)) == true)
            {
                return p;
            }
        }
        return null;
    }

    private ITest getTestByName(String testName) 
    {
        for (ITest t : this.testRepository)
        {
            if (t.getName().equals((testName)) == true)
            {
                return t;
            }
        }
        return null;
    }
}

