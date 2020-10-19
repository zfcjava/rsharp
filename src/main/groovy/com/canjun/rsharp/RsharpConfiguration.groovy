
/**
 * Created by zfc at 2020-10-17 15:16:01
 *
 * The RSharpConfigExtension is a bean class that stores the data from the project.
 * The replace can happen：
 *      1. packageName
 *      2. typeKey
 */
class RsharpConfiguration {

    /**
     * 当前的包名
     */
    String oldPackageName
    /**
     * 新的包名
     */
    String newPackageName

    /**
     * 校验文件
     */
    List checkFiles

    /**
     * 校验路径
     */
    List checkDirs
}