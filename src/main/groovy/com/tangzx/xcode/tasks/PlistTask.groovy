package com.tangzx.xcode.tasks

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil

/**
*  Created by Tangzx on 15/5/21.
* @qq 272669294
*/
class PlistTask extends DefaultTask{

    @TaskAction
    def exec() {
        if (src != null) {

            NSObject plistObject = PropertyListParser.parse(src)
            NSDictionary plist = plistObject as NSDictionary

            addKeyValue.each { key, value ->
                if (value instanceof GString)
                    plist.put(key, value.toString())
                else
                    plist.put(key, value)
            }

            removeKey.each {
                if (plist.containsKey(it)) {
                    plist.remove(it)
                }
            }

            if (urlSchemes.size() > 0) {
                int size = urlSchemes.size()
                int i = 0;
                NSArray old = plist.get("CFBundleURLTypes") as NSArray
                if (old != null) {
                    size += old.array.size()
                }
                NSArray array = new NSArray(size)
                if (old != null) {
                    old.array.each {
                        array.setValue(i++, it)
                    }
                }

                urlSchemes.each { URLSchemes url ->
                    NSDictionary usDict = new NSDictionary()
                    usDict.put("CFBundleTypeRole", "Editor")

                    NSArray urls = new NSArray(1)
                    urls.setValue(0, url.url)
                    usDict.put("CFBundleURLSchemes", urls)

                    array.setValue(i++, usDict)
                }

                plist.put("CFBundleURLTypes", array)
            }

            src.write(plist.toXMLPropertyList())
        }
    }

    File src;

    String version

    TreeMap<String, Object> addKeyValue = new TreeMap<String, Object>()

    List<String> removeKey = new ArrayList<String>()

    List<URLSchemes> urlSchemes = new ArrayList<URLSchemes>()

    void src(Object o) {
        if (o instanceof String) {
            src = new File(o)
        } else if (o instanceof File) {
            src = o as File
        }
    }

    void version(String v) {
        this.version = v
    }

    void addKeyValue(String key, Object value) {
        addKeyValue.put(key, value)
    }

    void addKeyValue(Map<String, ?> kv) {
        kv.each {
            addKeyValue.put(it.key, it.value)
        }
    }

    void removeKey(String key) {
        removeKey << key
    }

    void removeKey(List<String> keys) {
        removeKey.addAll(keys)
    }

    void addURLSchemes(Object data) {
        if (data instanceof String) {
            URLSchemes us = new URLSchemes()
            us.url = (String)data
            urlSchemes << us
        } else if (data instanceof Closure) {
            URLSchemes us = new URLSchemes()
            ConfigureUtil.configure(data, us)
            urlSchemes << us
        }
    }
}

class URLSchemes {
    String url
}