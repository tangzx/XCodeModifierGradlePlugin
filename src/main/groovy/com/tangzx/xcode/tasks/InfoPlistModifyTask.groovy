package com.tangzx.xcode.tasks

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import com.tangzx.xcode.conventions.InfoPlistConvention
import com.tangzx.xcode.conventions.XCodeProjectConvention
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
*  Created by Tangzx on 15/4/12.
* @qq 272669294
*/
class InfoPlistModifyTask extends DefaultTask{

    InfoPlistConvention infoExt

    @TaskAction
    def exec() {
        XCodeProjectConvention xcodeExt = project.extensions.xcodeproject
        if (xcodeExt.info != null && xcodeExt.info.src != null) {
            infoExt = xcodeExt.info

            NSObject plistObject = PropertyListParser.parse(infoExt.src)
            NSDictionary plist = plistObject as NSDictionary

            infoExt.addKeyValue.each { key, value ->
                if (value instanceof GString)
                    plist.put(key, value.toString())
                else
                    plist.put(key, value)
            }

            infoExt.removeKey.each {
                if (plist.containsKey(it)) {
                    plist.remove(it)
                }
            }

            /**
             * <key>CFBundleURLTypes</key>
             <array>
                 <dict>
                 <key>CFBundleTypeRole</key>
                 <string>Editor</string>
                 <key>CFBundleURLSchemes</key>
                 <array>
                    <string>ipayalipay-com.powergame.BLCXHM</string>
                 </array>
                 </dict>
             </array>
             */
            if (infoExt.urlSchemes.size() > 0) {
                int size = infoExt.urlSchemes.size()
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

                infoExt.urlSchemes.each { com.tangzx.xcode.conventions.URLSchemes url ->
                    NSDictionary usDict = new NSDictionary()
                    usDict.put("CFBundleTypeRole", "Editor")

                    NSArray urls = new NSArray(1)
                    urls.setValue(0, url.url)
                    usDict.put("CFBundleURLSchemes", urls)

                    array.setValue(i++, usDict)
                }

                plist.put("CFBundleURLTypes", array)
            }

            infoExt.src.write(plist.toXMLPropertyList())
        }
    }
}