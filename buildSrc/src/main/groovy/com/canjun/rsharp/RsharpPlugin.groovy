import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * Created by zfc at 2020-10-17 15:13:01
 *
 * The RSharpPlugin is a plugin that can refactor the module name  fast.
 */
class RsharpPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('rsharp',RsharpConfiguration)
        project.tasks.create('doReplace',RsharpTask)
//        project.tasks.create('doClean',RsharpCleanTask)
    }
}