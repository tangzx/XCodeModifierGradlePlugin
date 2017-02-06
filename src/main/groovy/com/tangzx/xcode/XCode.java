package com.tangzx.xcode;

import ca.mestevens.ios.xcode.parser.exceptions.FileReferenceDoesNotExistException;
import ca.mestevens.ios.xcode.parser.exceptions.InvalidObjectFormatException;
import ca.mestevens.ios.xcode.parser.models.*;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tangzx on 15/4/10.
 * @qq 272669294
 */
public class XCode extends XCodeProject {

    public static void main(String[] args) {
        try {
            String path = "/Users/mofeng/Desktop/XX/xx/xx.xcodeproj/project.pbxproj";

            XCode xp = new XCode(path);

            xp.getGroup("/test/11", true);
            //xp.addFramework("usr/lib/libstdc++.dylib");
            //xp.addFramework("System/Library/Frameworks/Accelerate.framework");

            //Save
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(xp.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XCode(String path) throws InvalidObjectFormatException {
        super(path);
    }

    public PBXFileElement addFileReference(String path, String groupPath) {
        PBXFileElement file = this.createFileReference(path);
        PBXFileElement group = this.getGroup(groupPath, true);
        group.addChild(file.getReference());
        file.setSourceTree("\"SDKROOT\"");
        return file;
    }

    @Override
    public PBXFileElement createFileReference(String filePath, String sourceTree) {
        Path path = Paths.get(filePath);

        PBXFileElement fileReference = new PBXFileElement("PBXFileReference",
                "\"" + path.getFileName().toString() + "\"", sourceTree);
        fileReference.setPath("\"" + path.toString() + "\"");

        if (!this.fileReferences.contains(fileReference)) {
            this.fileReferences.add(fileReference);
        }
        return fileReference;
    }

    public PBXBuildFile addBuildFile(PBXFileElement file) {
        PBXBuildFile pbxBuildFile = new PBXBuildFile(file.getName(), file.getReference().getIdentifier());
        this.buildFiles.add(pbxBuildFile);
        return pbxBuildFile;
    }

    public PBXBuildFile addBuildFile(String path, String group) {
        PBXFileElement fileElement = this.addFileReference(path, group);
        return this.addBuildFile(fileElement);
    }

    public void addFileToPhases(CommentedIdentifier identifier, List<PBXBuildPhase> phases) {
        for (PBXBuildPhase phase : phases) {
            phase.getFiles().add(identifier);
        }
    }

    /**
     * 移除一个文件
     * @param path path of file
     */
    public void removeFileReference(String path) {
        while (true) {
            PBXFileElement fileRef = this.getFileReference(path);
            if (fileRef != null) {
                //Remove From Phases
                try {
                    PBXBuildFile buildFile = this.getBuildFile(path);

                    this.removeFileInPhases(buildFile.getReference(), this.frameworksBuildPhases);
                    this.removeFileInPhases(buildFile.getReference(), this.resourcesBuildPhases);
                    this.removeFileInPhases(buildFile.getReference(), this.sourcesBuildPhases);
                    this.removeFileInPhases(buildFile.getReference(), this.appleScriptBuildPhases);
                    this.removeFileInPhases(buildFile.getReference(), this.copyFilesBuildPhases);
                    this.removeFileInPhases(buildFile.getReference(), this.headersBuildPhases);
                    this.removeFileInPhases(buildFile.getReference(), this.shellScriptBuildPhases);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Remove Files Ref
                this.fileReferences.remove(fileRef);

                //Remove From Groups
                for (PBXFileElement group : this.groups) {
                    group.getChildren().remove(fileRef.getReference());
                }
            } else break;
        }
    }

    private void removeFileInPhases(CommentedIdentifier element, List<PBXBuildPhase> phases) {
        for (PBXBuildPhase phase : phases) {
            List<CommentedIdentifier> files = phase.getFiles();
            files.remove(element);
        }
    }

    /**
     * 添加一个Embed Framework
     * @param path path of framework
     */
    public void addEmbedFramework(String path) throws FileReferenceDoesNotExistException {
        String buildFileName = Paths.get(path).getFileName().toString();
        PBXFileElement fileReference = this.getFileReference(path);

        PBXBuildFile file = new PBXBuildFile(buildFileName, fileReference.getReference().getIdentifier());
        this.buildFiles.add(file);

        for (PBXTarget target : this.nativeTargets) {
            if (target.getProductType().equals("\"com.apple.product-type.application\"")) {
                PBXBuildPhase phase = this.getEmbedFrameworksBuildPhase(target.getName());
                phase.getFiles().add(file.getReference());
            }
        }
    }

    /**
     * 添加一个Framework
     * @param path path of framework
     */
    public PBXBuildFile addFramework(String path, String groupPath) throws FileReferenceDoesNotExistException {
        PBXFileElement pbxFileElement = this.addFileReference(path, groupPath);
        PBXBuildFile pbxBuildFile = this.addBuildFile(pbxFileElement);

        for (PBXBuildPhase phase : this.frameworksBuildPhases) {
            phase.getFiles().add(pbxBuildFile.getReference());
        }

        this.addBuildSetting("FRAMEWORK_SEARCH_PATHS", Paths.get(path).getParent().toString());
        return pbxBuildFile;
    }

    /**
     * 清除这个设置
     * @param name key
     */
    public void removeBuildSetting(String name) {
        for (XCBuildConfiguration cfg : this.buildConfigurations) {
            cfg.getBuildSettings().remove(name);
        }
    }

    public void addBuildSetting(String name, String value) {
        for (XCBuildConfiguration cfg : this.buildConfigurations) {
            List<String> list = cfg.getBuildSettingAsList(name);
            if (list == null)
                list = new ArrayList<>();
            if (list.contains(value))
                continue;

            list.add(value);
            cfg.setBuildSetting(name, list);
        }
    }

    public void addBuildSetting(String name, List<String> values) {
        for (String v : values) {
            this.addBuildSetting(name, v);
        }
    }

    public PBXBuildFile getBuildFile(String path) throws FileReferenceDoesNotExistException {
        PBXFileElement fileRef = this.getFileReference(path);
        if (fileRef != null) {
            List<PBXBuildFile> files = this.getBuildFileWithFileRef(fileRef.getReference().getIdentifier());
            if (files.size() > 0)
                return files.get(0);
        }

        return null;
    }

    public PBXFileElement getFileReference(String path) {
        for (PBXFileElement file : this.fileReferences) {
            String filePath = file.getPath();
            if (filePath == null)
                continue;
            if (filePath.startsWith("\""))
                filePath = filePath.substring(1, filePath.length() - 1);

            if (filePath.equals(path)) {
                return file;
            }
        }

        return null;
    }

    public PBXFileElement getFileReferenceById(String id) {
        for (PBXFileElement file : this.fileReferences) {
            if (id.equals(file.getReference().getIdentifier())) {
                return file;
            }
        }
        return null;
    }

    public PBXFileElement getGroup(String path, boolean autoCreate) {
        PBXFileElement targetGroup = this.getGroupWithIdentifier(this.project.getMainGroup().getIdentifier());
        if (path == null)
            return targetGroup;

        String[] paths = path.split("/");
        if (paths.length < 2)
            return targetGroup;

        for (int i = 1; i < paths.length; i++) {
            String groupName = "\"" + paths[i] + "\"";

            PBXFileElement childGroup = null;
            assert targetGroup != null;
            List<CommentedIdentifier> children = targetGroup.getChildren();
            for (CommentedIdentifier child : children) {
                PBXFileElement childFile = this.getGroupWithIdentifier(child.getIdentifier());
                if (childFile != null && groupName.equals(childFile.getName())) {
                    childGroup = childFile;
                    break;
                }
            }

            if (childGroup == null && autoCreate) {
                childGroup = this.createGroup(groupName, targetGroup.getReference().getIdentifier());
            }

            targetGroup = childGroup;
        }

        return targetGroup;
    }

    public PBXTarget getNativeTargetWithName(String name) {
        for (PBXTarget target : this.getNativeTargets()) {
            if (target.getName().equals(name)) {
                return target;
            }
        }
        return null;
    }

    public void addFrameworksBuildPhaseFor(String targetName, PBXBuildPhase phase) {
        PBXTarget target = this.getNativeTargetWithName(targetName);
        if (target != null) {
            this.addFrameworksBuildPhase(target.getReference().getIdentifier(), phase);
        }
    }

    public PBXBuildPhase getEmbedFrameworksBuildPhase(String targetName) {
        PBXTarget target = this.getNativeTargetWithName(targetName);
        PBXBuildPhase phase = null;
        if (target != null) {
            for (PBXBuildPhase bp : this.getCopyFilesBuildPhases()) {
                if (bp.getName() != null && bp.getName().equals("\"Embed Frameworks\"")) {
                    phase = bp;
                    break;
                }
            }

            if (phase == null) {
                phase = new PBXBuildPhase("PBXCopyFilesBuildPhase", "\"Embed Frameworks\"", null, "\"\"", 10);
                this.addCopyFilesBuildPhase(target.getReference().getIdentifier(), phase);
            }
        }

        return phase;
    }

    @Override
    public String toString() {
        if (this.project.getProductRefGroup() == null)
            this.project.setProductRefGroup(this.getGroup("/Products", true).getReference());
        return super.toString();
    }
}
