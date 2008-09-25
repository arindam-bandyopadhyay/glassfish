package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.NameValue;
import com.sun.enterprise.universal.collections.ManifestUtils;
import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * Wraps the Manifest object returned by the Server.  The Manifest object has
 * an internally defined format over and above the Manifest itself.  This is a
 * central place where we are aware of all the details so that callers don't
 * have to be.  If the format changes or the returned Object type changes then
 * this class will be the thing to change.
 * 
 * @author bnevins
 */
public class AdminCommandResponse {
    public static final String GENERATED_HELP = "GeneratedHelp";
    public static final String MANPAGE = "MANPAGE";
    public static final String SYNOPSIS = "SYNOPSIS";
    public static final String MESSAGE = "message";
    public static final String CHILDREN_TYPE = "children-type";
    public static final String EXITCODE = "exit-code";
    public static final String SUCCESS = "Success";
    
    public AdminCommandResponse(InputStream inStream) throws IOException {
        Manifest m = new Manifest(inStream);
        m.read(inStream);
        allRaw = ManifestUtils.normalize(m);
        mainRaw = ManifestUtils.getMain(allRaw);
        makeMain();
    }

    public boolean isGeneratedHelp() {
        return isGeneratedHelp;
    }
    
    public String getMainMessage() {
        return mainMessage;
    }
    
    public boolean wasSuccess() {
        return exitCode == 0;
    }

    public String getCause() {
        return cause;
    }
    public Map<String,String> getMainAtts() {
        return mainRaw;
    }
    public List<NameValue<String,String>> getMainKeys() {
        return mainKeys;
    }
    
    public String getValue(String key) {
        for(NameValue<String,String> nv : mainKeys) {
            if(nv.getName().equals(key))
                return nv.getValue();
        }
        return null;
    }
    
    public List<NameValue<String,String>> getKeys(Map<String,String> map) {
        List<NameValue<String,String>> list = new LinkedList<NameValue<String,String>>();
        
        String keysString = map.get("keys");
        
        if(ok(keysString)) {
            String[] keys = keysString.split(";");

            for(String key : keys) {
                String name = map.get(key + "_name");
                String value = null;
                try {
                    value = map.get(key + java.net.URLDecoder.decode("_value", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    value = map.get(key + "_value");
                }

                if(!ok(name))
                    continue;
                list.add(new NameValue<String,String>(name, value));
            }
        }
        
        return list;
    }

    public Map<String,Map<String,String>> getChildren(Map<String,String> map) {
        Map<String,Map<String,String>> children = new HashMap<String,Map<String,String>>();
        String kidsString = map.get("children");
        
        if(ok(kidsString)) {
            String[] kids = kidsString.split(";");

            for(String kid : kids) {
                // kid is the name of the Attributes
                Map<String,String> kidMap = allRaw.get(kid);
                if(kidMap != null)
                    children.put(kid, kidMap);
            }
        }
        if(children.isEmpty())
            return null;
        else
            return children;
    }

    private void makeMain() {
        mainMessage = mainRaw.get(MESSAGE);
        mainChildrenType = mainRaw.get(CHILDREN_TYPE);
        
        if(SUCCESS.equalsIgnoreCase(mainRaw.get(EXITCODE)))
            exitCode = 0;
        else
            exitCode = 1;
        signature = mainRaw.get("Signature-Version");
        cause = mainRaw.get("cause");
        makeMainKeys();
    }
    
    /**
     *  Format:
     * (1) Main Attributes usually have the bulk of the data.  Say you have 3 items
     * in there: a, b and c.  The Manifest main attributes will end up with this:
     * keys=a;b;c
     * a_name=xxx
     * a_value=xxx
     * b_name=xxx
     * b_value=xxx
     * c_name=xxx
     * c_value=xxx
     */
    private void makeMainKeys() {
        mainKeys = getKeys(mainRaw);
        
        for(NameValue<String,String> nv : mainKeys) {
            if(nv.getName().equals(GENERATED_HELP)) {
                isGeneratedHelp = Boolean.parseBoolean(nv.getValue());
                mainKeys.remove(nv);
                break;
            }
        }
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null"); 
    }

    private Map<String, Map<String, String>> allRaw;
    private Map<String, String> mainRaw;
    private List<NameValue<String,String>> mainKeys;
    private String  mainMessage;
    private String  mainChildrenType;
    private String  signature;
    private String  cause;
    private int exitCode = 0;   // 0=success, 1=failure
    private boolean isGeneratedHelp;
}
