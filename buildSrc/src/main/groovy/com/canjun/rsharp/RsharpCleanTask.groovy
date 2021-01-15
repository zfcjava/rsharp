package com.canjun.rsharp

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class RsharpCleanTask extends DefaultTask{

    RsharpCleanTask(){
        setGroup('rsharp')
    }

    @TaskAction
    def doAction() {
        String oldPackageName = project.extensions.rsharp.oldPackageName
        String newPackageName = project.extensions.rsharp.newPackageName

        if(oldPackageName == newPackageName){
            throw GradleException("the oldPackageName must not be the same as the newPackageName")
        }
        doPackageClean(oldPackageName,newPackageName)
    }

    /**
     * 实现包名替换
     *      1.将指定包名下的文件，拷贝到目标目标位置
     *      2.针对文件内容的中包名替换
     *      3.修改其他资源
     * @param oldPackageName  com.fortune.mastermelon
     * @param newPackageName com.canjun.mastermelon
     *
     */
    def doPackageClean(String oldPackageName, String newPackageName){
        def sourceDirPath = oldPackageName.replace('.','/')
        def targetDirPath = newPackageName.replace('.','/')
        //中间路径
        def javaMiddlePath = 'src/main/java'
        def androidManifestPath = 'src/main/AndroidManifest.xml'

        //目标文件夹的确认
        def sourceDir = project.file("${javaMiddlePath}/${sourceDirPath}")
        if(!sourceDir.exists()){
            throw GradleException("please make sure the source dir exists")
        }

        String targetStr = project.file("${javaMiddlePath}/${targetDirPath}")

        while (!sourceDir.path.contains(targetStr)){
            println "sourceStr = " + targetStr
            def f = project.file(targetStr)
            if (f != null) {
                targetStr = f.getParent()
            }else{
                break
            }
            deleteFile(f)
        }


        //修改Manifest清单文件（如果是就对路径，则有必要修改）
        //创建一个新的备份文件
        def androidManifestBackup = project.file(androidManifestPath+".backup")
        if(androidManifestBackup.exists()){
            androidManifestBackup.delete()
        }
        androidManifestBackup.createNewFile()
        project.file(androidManifestPath).withReader { reader ->
            def lines = reader.lines()
            androidManifestBackup.withWriter {writer ->
                lines.each { line ->
                    writer.write(line + "\r\n")
                }
            }
        }
        //重建构建AndroidManifest
        def androidManifest = project.file(androidManifestPath)
        if(androidManifest.exists()){
            androidManifest.delete()
        }
        androidManifest.createNewFile()
        androidManifestBackup.withReader { reader ->
            def lines = reader.lines()
            androidManifest.withWriter {writer ->
                lines.each { line ->
                    if(line.contains(oldPackageName)){
                        line = line.replaceAll(oldPackageName,newPackageName)
                    }
                    writer.write(line + "\r\n")
                }
            }
        }

    }


    /**
     * 先根遍历序递归删除文件夹
     *
     * @param dirFile 要被删除的文件或者目录
     * @return 删除成功返回true, 否则返回false
     */
    def deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }

        return dirFile.delete();
    }


}