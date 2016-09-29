package com.tangzx.xcode

import com.tangzx.xcode.conventions.XCodeProjectConvention
import com.tangzx.xcode.tasks.InfoPlistModTask
import com.tangzx.xcode.tasks.XCodeProjectModTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
*  Created by Tangzx on 2015/4/9.
* @qq 272669294
*/
class XCodeProjectPlugin implements Plugin<Project> {

    Project _project

    void apply (Project project) {
        _project = project
        _project.extensions.create("xcodeproject", XCodeProjectConvention)

        this.initTasks()
    }

    void initTasks() {
        _project.task([type:InfoPlistModTask], "ModInfoPlist") {
            group = "XCode"
            description = "Mod a plist file."
        }

        _project.task([type:XCodeProjectModTask], "ModXCodeProject") {
            group = "XCode"
            description = "Mod a XCodeProject file."
            dependsOn "ModInfoPlist"
        }
    }
}