package com.tangzx.xcode.conventions

import com.tangzx.xcode.XCodeFileTool
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.util.ConfigureUtil

/**
*  Created by Tangzx on 15/4/14.
* @qq 272669294
*/
class XCodeProjectConvention {

    /**
     * Xcode 工程文件
     */
    String src

    /**
     * Info.plist 文件修改配置
     */
    InfoPlistConvention info

    List<BuildFileConvention> embedFrameworks = new ArrayList<>()

    List<BuildFileConvention> frameworks = new ArrayList<>()

    List<BuildFileConvention> addFiles = new ArrayList<>()

    List<File> addFolders = new ArrayList<>()

    List<File> removeFiles = new ArrayList<File>()

    List<AddBuildSetting> addBuildSettings = new ArrayList<>()

    Map<String, String> fileType2Phase = [ ".a":"PBXFrameworksBuildPhase",
                                           ".app":null,
                                           ".s":"PBXSourcesBuildPhase",
                                           ".c":"PBXSourcesBuildPhase",
                                           ".cpp":"PBXSourcesBuildPhase",
                                           ".framework":"PBXFrameworksBuildPhase",
                                           ".h":null,
                                           ".pch":null,
                                           ".icns":"PBXResourcesBuildPhase",
                                           ".m":"PBXSourcesBuildPhase",
                                           ".mm":"PBXSourcesBuildPhase",
                                           ".nib":"PBXResourcesBuildPhase",
                                           ".plist":"PBXResourcesBuildPhase",
                                           ".png":"PBXResourcesBuildPhase",
                                           ".rtf":"PBXResourcesBuildPhase",
                                           ".tiff":"PBXResourcesBuildPhase",
                                           ".txt":"PBXResourcesBuildPhase",
                                           ".xcodeproj":null,
                                           ".xib":"PBXResourcesBuildPhase",
                                           ".strings":"PBXResourcesBuildPhase",
                                           ".bundle":"PBXResourcesBuildPhase",
                                           ".dylib":"PBXFrameworksBuildPhase" ]
    Map<String, String> fileType = [ ".a":"archive.ar",
                                     ".app":"wrapper.application",
                                     ".s":"sourcecode.asm",
                                     ".c":"sourcecode.c.c",
                                     ".cpp":"sourcecode.cpp.cpp",
                                     ".framework":"wrapper.framework",
                                     ".h":"sourcecode.c.h",
                                     ".pch":"sourcecode.c.h",
                                     ".icns":"image.icns",
                                     ".m":"sourcecode.c.objc",
                                     ".mm":"sourcecode.cpp.objcpp",
                                     ".nib":"wrapper.nib",
                                     ".plist":"text.plist.xml",
                                     ".png":"image.png",
                                     ".rtf":"text.rtf",
                                     ".tiff":"image.tiff",
                                     ".txt":"text",
                                     ".xcodeproj":"wrapper.pb-project",
                                     ".xib":"file.xib",
                                     ".strings":"text.plist.strings",
                                     ".bundle":"wrapper.plug-in",
                                     ".dylib":"compiled.mach-o.dylib"]
    
    void addBuildSetting(Object o) {
        if (o instanceof Closure) {
            AddBuildSetting setting = new AddBuildSetting()
            ConfigureUtil.configure(o, setting)
            addBuildSettings << setting
        } else if (o instanceof Map) {
            Map map = o as Map
            map.each {
                AddBuildSetting setting = new AddBuildSetting()
                setting.name = it.key
                if (it.value instanceof String)
                    setting.values << (it.value as String)
                else if (it.value instanceof Map)
                    setting.values += it.value
            }
        }
    }

    void addFile(Object o) {
        if (o instanceof String) {
            def f = new BuildFileConvention()
            f.files(o)
            addFiles << f
        } else if (o instanceof Closure) {
            def f = new BuildFileConvention()
            ConfigureUtil.configure(o, f)
            addFiles << f
        }
    }

    void addFolder(Object o) {
        if (o instanceof String) {
            BuildFileConvention fc = new BuildFileConvention()
            fc.groupFileHeader(o)
            fc.files(o)
            addFiles << fc
        } else if (o instanceof File) {
            BuildFileConvention fc = new BuildFileConvention()
            fc.groupFileHeader(o.path)
            fc.files(o)
            addFiles << fc
        }
    }

    void removeFile(Object o) {
        if (o instanceof String) {
            def f = new File(o)
            removeFiles << f
        } else if (o instanceof FileTree) {
            FileTree tree = o as FileTree
            removeFiles += tree.files
        }
    }

    void addEmbedFramework(Object o) {
        if (o instanceof String) {//FilePath
            def bf = new FrameworkFileConvention()
            bf.files(o)
            embedFrameworks << bf
        } else if (o instanceof Closure) {
            def bf = new FrameworkFileConvention()
            ConfigureUtil.configure(o, bf)
            embedFrameworks << bf
        }
    }

    void addFramework(Object o) {
        if (o instanceof String) {
            def bf = new FrameworkFileConvention()
            bf.files(o)
            frameworks << bf
        } else if (o instanceof Closure) {
            def bf = new FrameworkFileConvention()
            ConfigureUtil.configure(o, bf)
            frameworks << bf
        }
    }

    void addLib(Object o) {
        if (o instanceof String) {
            def bf = new LibraryFileConvention()
            bf.files(o)
            frameworks << bf
        } else if (o instanceof Closure) {
            def bf = new LibraryFileConvention()
            ConfigureUtil.configure(o, bf)
            frameworks << bf
        }
    }

    void regFileType(Map<String, String> data) {
        fileType2Phase.putAll(data)
    }

    void info(Closure c) {
        if (info == null)
            info = new InfoPlistConvention()
        ConfigureUtil.configure(c, info)
    }
}

class BuildFileConvention {

    String group = "/"

    File groupFileHeader

    ArrayList<File> files = new ArrayList<File>()

    void files(Object c) {
        if (c instanceof File) {
            this.filter(c)
        } else if (c instanceof FileTree) {
            FileTree ft = c
            ft.visit(new FileVisitor() {
                @Override
                void visitDir(FileVisitDetails fileVisitDetails) {
                    if (XCodeFileTool.isPackageFolder(fileVisitDetails.file))
                        files << fileVisitDetails.file
                }

                @Override
                void visitFile(FileVisitDetails fileVisitDetails) {
                    if (!XCodeFileTool.isInPackage(fileVisitDetails.file))
                        files << fileVisitDetails.file
                }
            })
        } else if (c instanceof String) {
            File f = new File((String)c)
            this.filter(f)
        }
    }

    void filter(File file) {
        if (file.isDirectory()) {
            if (XCodeFileTool.isPackageFolder(file))
                files << file
            else {
                file.listFiles().each {
                    filter(it)
                }
            }
        } else if (!XCodeFileTool.isInPackage(file)){
            files << file
        }
    }

    void group(String name) {
        this.group = name
    }

    void groupFileHeader(String path) {
        groupFileHeader = new File(path)
    }

    String getGroup(File file) {
        if (groupFileHeader != null && groupFileHeader.exists()) {
            String groupFile = groupFileHeader.getCanonicalPath()
            String filePath = file.parentFile.getCanonicalPath()
            String groupName = filePath.replace(groupFile, "")
            return groupName
        }
        return this.group
    }
}

class FrameworkFileConvention extends BuildFileConvention {

    void sys(String fileName) {
        def f = new File("System/Library/Frameworks", fileName)
        files << f
    }

}

class LibraryFileConvention extends FrameworkFileConvention {

    void sys(String fileName) {
        def f = new File("usr/lib/", fileName)
        files << f
    }

}

class AddBuildSetting {
    String name
    List<String> values = new ArrayList<String>()
}