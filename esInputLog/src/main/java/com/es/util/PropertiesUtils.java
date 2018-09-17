package com.es.util;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class PropertiesUtils {




	
	  private static Logger log = Logger.getLogger(PropertiesUtils.class);
	  private String fileName;

	  public PropertiesUtils(String fileName)
	  {
	    this.fileName = fileName;
	  }

	  public Map<String, String> getResourceBundleByName() {
	    try {
	      ResourceBundle bundle = ResourceBundle.getBundle(this.fileName);
	      Enumeration enumkey = bundle.getKeys();
	      Map keysMap = new ConcurrentHashMap();
	      while (enumkey.hasMoreElements()) {
	        String key = (String)enumkey.nextElement();
	        keysMap.put(key, bundle.getString(key));
	      }
	      return keysMap;
	    } catch (Exception e) {
	      log.error("init CommonkeyWords exception: " + e.getMessage());
	    }
	    return null;
	  }

	  public String getKeyValue(String key) {
	    String keyValue = "";
	    try {
	      ResourceBundle bundle = ResourceBundle.getBundle(this.fileName);
	      Enumeration enumkey = bundle.getKeys();
	      while (enumkey.hasMoreElements()) {
	        String pkey = (String)enumkey.nextElement();
	        if (pkey.equals(key)) {
	          keyValue = bundle.getString(pkey);
	          break;
	        }
	      }
	    } catch (Exception e) {
	      log.error("init CommonkeyWords exception: " + e.getMessage());
	    }
	    return keyValue;
	  }

	  public static synchronized String getKeyValue(String fileName, String key) {
	    String keyValue = "";
	    try {
	      ResourceBundle bundle = ResourceBundle.getBundle(fileName);
	      Enumeration enumkey = bundle.getKeys();
	      while (enumkey.hasMoreElements()) {
	        String pkey = (String)enumkey.nextElement();
	        if (pkey.equals(key)) {
	          keyValue = bundle.getString(pkey);
	          break;
	        }
	      }
	      ResourceBundle.clearCache();
	    } catch (Exception e) {
	      log.error("init CommonkeyWords exception: " + e.getMessage());
	    }
	    return keyValue;
	  }
	
	
	
}
