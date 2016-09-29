package com.tangzx.xcode

/**
*  Created by Tangzx on 15/5/19.
* @qq 272669294
*/
class XCodeFileTool {

    static boolean isPackageFolder(File file) {
        if (file == null) return false

        return file.name.endsWith(".framework") || file.name.endsWith(".bundle")
    }

    static boolean isInPackage(File file) {
        if (file == null) return false
        File parent = file.parentFile
        while (parent != null) {
            if (isPackageFolder(parent))
                return true
            parent = parent.parentFile
        }

        return false
    }
}
