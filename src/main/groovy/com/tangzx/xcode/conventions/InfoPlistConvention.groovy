package com.tangzx.xcode.conventions

import org.gradle.util.ConfigureUtil

/**
*  Created by Tangzx on 15/4/12.
* @qq 272669294
*/
class InfoPlistConvention {

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