import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class RsharpTask extends DefaultTask{

    RsharpTask(){
        setGroup('rsharp')
    }

    static srcDir = 'src/'
    static mainDir = 'src/main/'
    static defaultSourceDir = 'src/main/java/'
    static defaultResDir = 'src/main/res/'
    static defaultAndroidManifestFile = 'src/main/AndroidManifest.xml'
    static defaultAndroidBuildGradle = 'build.gradle'


    @TaskAction
    def doAction() {
        String oldPackageName = project.extensions.rsharp.oldPackageName
        //TODO 我们可以增加一个属性，来清空newPackageName对应的路径
        //TODO 我们可以增加一个是否需要备份的策略
        //TODO 额外配置build.gradle路径
        String newPackageName = project.extensions.rsharp.newPackageName
        List checkFiles = project.extensions.rsharp.checkFiles
        List checkDirs = project.extensions.rsharp.checkDirs

        if(oldPackageName == newPackageName){
            throw GradleException("the oldPackageName must not be the same as the newPackageName")
        }

        if (checkFiles != null) {
            checkFiles.each {filePath ->
                if(filePath.startsWith('.')){
                    throw GradleException("checkFiles path must not start with '.' ")
                }
                if(filePath.endsWith('/')){
                    throw GradleException("checkFiles path must not end up with '/' ")
                }
                if(filePath.startsWith(defaultSourceDir)
                        ||filePath.startsWith(defaultResDir)
                        ||filePath.startsWith(defaultAndroidManifestFile)
                        ||filePath.startsWith(defaultAndroidBuildGradle)
                ){
                    throw GradleException("checkFiles path cannot be ")
                }
            }
        }

        if (checkDirs != null) {
            checkDirs.each{ dirPath ->
                if(dirPath.startsWith('.')){
                    throw GradleException("checkDirs path must not start with '.' ")
                }
                if(!dirPath.endsWith('/')){
                    throw GradleException("checkDirs path must end up with '/' ")
                }
                if(dirPath.startsWith(defaultSourceDir)
                        ||dirPath.startsWith(defaultResDir)
                        ||dirPath == mainDir
                        ||dirPath == srcDir
                ){
                    throw GradleException('''checkDirs path cannot be : ''')
                }
            }
        }

        println checkDirs
        println checkFiles

        //处理包名
        replaceSource(oldPackageName,newPackageName)
        replaceResource(oldPackageName,newPackageName)
        replaceAndroidManifest(oldPackageName,newPackageName)
        replaceBuildGradle(oldPackageName,newPackageName)
        replaceFiles(checkFiles,oldPackageName,newPackageName)
        replaceDirs(checkDirs,oldPackageName,newPackageName)
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
    def replaceSource(String oldPackageName, String newPackageName){

        def sourceDirPath = oldPackageName.replace('.','/')
        def targetDirPath = newPackageName.replace('.','/')
        //中间路径
        def javaMiddlePath = 'src/main/java'


        //目标文件夹的确认
        def targetDir = project.file("${javaMiddlePath}/${targetDirPath}")
        if(!targetDir.exists()){
            targetDir.mkdirs()
        }

        def sourceAbsDirPath = "${javaMiddlePath}/${sourceDirPath}"
        //打印一下当前项目路径
        project.fileTree("${javaMiddlePath}/${sourceDirPath}").each { file ->
            //源文件的父级别路径
            String fileAbsolutePath = file.getParent().toString()
            //源文件父路径相对于根包名的路径
            String relativeFilePath = fileAbsolutePath.substring(fileAbsolutePath.indexOf(sourceAbsDirPath) + sourceAbsDirPath.size())
            //新包名源文件父路径相对于
            def targetParentDir = project.file("${targetDir.toString()}${relativeFilePath}")
            if(!targetParentDir.exists()){
                targetParentDir.mkdirs()
            }
            targetParentDir.mkdirs()
            def targetFile = new File(targetParentDir,file.name)
            if(targetFile.exists()){
                targetFile.delete()
            }
            targetFile.createNewFile()

            file.withReader { reader ->
                def lines = reader.lines()
                targetFile.withWriter { writer ->
                    lines.each { line ->
                        //包名的全局替换
                        if(line.contains(oldPackageName)){
                            line = line.replaceAll(oldPackageName,newPackageName)
                        }
                        writer.write(line + "\r\n")
                    }
                }
            }
        }

        //删除原有文件
        String sourceStr = project.file("${javaMiddlePath}/${sourceDirPath}")

        while (!targetDir.path.contains(sourceStr)){
            println "sourceStr = " + sourceStr
            def f = project.file(sourceStr)
            if (f != null) {
                sourceStr = f.getParent()
            }else{
                break
            }
            deleteFile(f)
        }
    }


    def replaceResource(String oldPackageName, String newPackageName){
        //处理资源文件（png不需要处理）
        project.fileTree(defaultResDir).each { file ->
            replaceContent(file,oldPackageName,newPackageName)
        }
    }

    def replaceAndroidManifest(String oldPackageName, String newPackageName){

        //修改Manifest清单文件（如果是就对路径，则有必要修改）
        //创建一个新的备份文件
        def androidManifestBackup = project.file(defaultAndroidManifestFile+".backup")
        if(androidManifestBackup.exists()){
            androidManifestBackup.delete()
        }
        androidManifestBackup.createNewFile()
        project.file(defaultAndroidManifestFile).withReader { reader ->
            def lines = reader.lines()
            androidManifestBackup.withWriter {writer ->
                lines.each { line ->
                    writer.write(line + "\r\n")
                }
            }
        }

        //重建构建AndroidManifest
        def androidManifest = project.file(defaultAndroidManifestFile)
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


    def replaceBuildGradle(String oldPackageName,String newPackageName){

        //修改Manifest清单文件（如果是就对路径，则有必要修改）
        //创建一个新的备份文件
        def androidManifestBackup = project.file(defaultAndroidBuildGradle+".backup")
        if(androidManifestBackup.exists()){
            androidManifestBackup.delete()
        }
        androidManifestBackup.createNewFile()
        project.file(defaultAndroidBuildGradle).withReader { reader ->
            def lines = reader.lines()
            androidManifestBackup.withWriter {writer ->
                lines.each { line ->
                    writer.write(line + "\r\n")
                }
            }
        }

        //重建构建AndroidManifest
        def androidManifest = project.file(defaultAndroidBuildGradle)
        if(androidManifest.exists()){
            androidManifest.delete()
        }

        androidManifest.createNewFile()
        androidManifestBackup.withReader { reader ->
            def lines = reader.lines()
            androidManifest.withWriter {writer ->
                lines.each { line ->
                    if(line.contains(oldPackageName)){
                        if(!line.contains("=")&&!line.contains("oldPackageName")) {
                            line = line.replaceAll(oldPackageName,newPackageName)
                        }
                    }
                    writer.write(line + "\r\n")
                }
            }
        }
    }


    def replaceFiles(List checkFiles,String oldPackageName,String newPackageName) {
        if (checkFiles == null) {
            return
        }
        checkFiles.each { filePath ->
            def file = project.file(filePath)
            replaceContent(file,oldPackageName,newPackageName)
        }

    }
    def replaceDirs(List checkDirs,String oldPackageName,String newPackageName){
        if (checkDirs == null) {
            return
        }
        checkDirs.each { dirPath ->
            project.fileTree(dirPath).each {file ->
                replaceContent(file,oldPackageName,newPackageName)
            }
        }
    }

    def replaceContent(File file,String oldPackageName,String newPackageName){
        if(file!=null
                &&file.isFile()
                && !file.name.toLowerCase().endsWith(".png")
                &&!file.name.toLowerCase().endsWith(".jpg")
                &&!file.name.toLowerCase().endsWith(".jpeg")){
            def content = file.text
            content = content.replaceAll(oldPackageName,newPackageName)
            file.withWriter { writer ->
                writer.write(content)
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