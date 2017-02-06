package com.tangzx.xcode.tasks

import ca.mestevens.ios.xcode.parser.models.PBXBuildFile
import com.tangzx.xcode.XCode
import com.tangzx.xcode.conventions.AddBuildSetting
import com.tangzx.xcode.conventions.BuildFileConvention
import com.tangzx.xcode.conventions.XCodeProjectConvention
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
*  Created by Tangzx on 15/4/12.
* @qq 272669294
*/
class XCodeProjectModifyTask extends DefaultTask{

    XCodeProjectConvention xcodeExt

    @TaskAction
    exec() {
        xcodeExt = project.extensions.modifier
        if (xcodeExt != null) {
            String src = xcodeExt.src
            if (src.endsWith("xcodeproj"))
                src = src + "/project.pbxproj"
            XCode xcode = new XCode(src)

            //Add Folder
            xcodeExt.addFolders.each { File folder->
                //this.addFolder(folder, xcode)
            }

            //Build Files
            xcodeExt.addFiles.each {
                it.files.each { f->
                    this.addFile(f, it, xcode)
                }
            }

            //Remove Files
            xcodeExt.removeFiles.each { File file->
                xcode.removeFileReference(file.path)
            }

            //Add EmbedFramework
            xcodeExt.embedFrameworks.each { it->
                it.files.each { File f->
                    xcode.addEmbedFramework(f.path)
                }
            }

            //Add Framework
            xcodeExt.frameworks.each {
                it.files.each { File f->
                    this.addFile(f, it, xcode)
                }
            }

            //Build Settings
            xcodeExt.addBuildSettings.each { AddBuildSetting setting->
                if (setting.override)
                    xcode.removeBuildSetting(setting.name)
                xcode.addBuildSetting(setting.name,  setting.values)
            }

            try {
                if (xcode.project.productRefGroup == null)
                    xcode.project.productRefGroup = xcode.getGroup("/Products", true).getReference()

                project.file(src).write(xcode.toString())
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    def addFile(File f, BuildFileConvention cfg, XCode xcode) {
        String group = cfg.getGroup(f)
        String name = f.name
        String phaseType = null
        int dotIndex = name.lastIndexOf('.')
        if (dotIndex != -1) {
            String type = name.substring(name.lastIndexOf('.'))
            phaseType = xcodeExt.fileType2Phase[type]

            if (type == ".h")
                xcode.addBuildSetting("HEADER_SEARCH_PATHS", "\"" + f.parentFile.path + "\"")
            else if (type == ".a")
                xcode.addBuildSetting("LIBRARY_SEARCH_PATHS", "\"" + f.parentFile.path + "\"")
        }

        switch (phaseType) {
            case "PBXFrameworksBuildPhase":
                xcode.addFramework(f.path, group)
                break
            case "PBXSourcesBuildPhase":
                PBXBuildFile pbxBuildFile = xcode.getBuildFile(f.path)
                if (pbxBuildFile == null)
                    pbxBuildFile = xcode.addBuildFile(f.path, group)

                xcode.addFileToPhases(pbxBuildFile.getReference(), xcode.getSourcesBuildPhases())
                break
            case "PBXResourcesBuildPhase":
                PBXBuildFile pbxBuildFile = xcode.getBuildFile(f.path)
                if (pbxBuildFile == null)
                    pbxBuildFile = xcode.addBuildFile(f.path, group)

                xcode.addFileToPhases(pbxBuildFile.getReference(), xcode.getResourcesBuildPhases())
                break
            default:
                PBXBuildFile pbxBuildFile = xcode.getBuildFile(f.path)
                if (pbxBuildFile == null)
                    xcode.addBuildFile(f.path, group)
                break
        }
    }

    def addFolder(File folder, BuildFileConvention cfg, XCode xcode) {
        if (folder.isDirectory()) {
            if (folder.name.endsWith(".bundle") || folder.name.endsWith(".framework")) {
                this.addFile(folder, cfg, xcode)
            } else {
                folder.listFiles().each {
                    if (it.isDirectory())
                        this.addFolder(it, cfg, xcode)
                    else
                        this.addFile(it, cfg, xcode)
                }
            }
        }
    }
}