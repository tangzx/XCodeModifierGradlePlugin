package com.tangzx.xcode

import com.tangzx.xcode.conventions.XCodeProjectConvention
import com.tangzx.xcode.tasks.InfoPlistModifyTask
import com.tangzx.xcode.tasks.XCodeProjectModifyTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
*  Created by Tangzx on 2015/4/9.
* @qq 272669294
*/
class XCodeModifierPlugin implements Plugin<Project> {

    Project _project

    void apply (Project project) {
        _project = project
        _project.extensions.create("xcode-modifier", XCodeProjectConvention)

        this.initTasks()
    }

    void initTasks() {
        _project.task([type:InfoPlistModifyTask], "ModInfoPlist") {
            group = "XCode-Modifier"
            description = "Mod a plist file."
        }

        _project.task([type:XCodeProjectModifyTask], "ModXCodeProject") {
            group = "XCode-Modifier"
            description = "Mod a XCodeProject file."
            dependsOn "ModInfoPlist"
        }
    }
}