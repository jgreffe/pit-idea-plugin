package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument
import spock.lang.Specification

import pl.mjedynak.idea.plugins.pit.ProjectDeterminator

class DefaultArgumentsContainerFactoryImplTest extends Specification {

    Project project = Mock()
    ProjectRootManager projectRootManager = Mock()
    PsiManager psiManager = Mock()
    ProjectDeterminator projectDeterminator = Mock()
    DefaultArgumentsContainerFactoryImpl defaultArgumentsContainerFactory = new DefaultArgumentsContainerFactoryImpl(projectRootManager, psiManager, projectDeterminator)
    

    def "should create container with default report dir"() {
        String baseDirPath = "app"
        VirtualFile baseDir = Mock()
        project.getBaseDir() >> baseDir
        baseDir.getPath() >> baseDirPath

        when:
        PitCommandLineArgumentsContainer container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)

        then:
        container.get(PitCommandLineArgument.REPORT_DIR) == baseDirPath + '/' + DefaultArgumentsContainerFactoryImpl.DEFAULT_REPORT_DIR
    }

    def "should create container with maven default report dir for maven project"() {
        String baseDirPath = "app"
        VirtualFile baseDir = Mock()
        project.getBaseDir() >> baseDir
        baseDir.getPath() >> baseDirPath
        projectDeterminator.isMavenProject(project) >> true

        when:
        PitCommandLineArgumentsContainer container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)

        then:
        container.get(PitCommandLineArgument.REPORT_DIR) == baseDirPath + '/' + DefaultArgumentsContainerFactoryImpl.MAVEN_REPORT_DIR
    }

    def "should create container with default source dir"() {
        VirtualFile sourceRoot = Mock()
        VirtualFile[] sourceRoots = [sourceRoot]
        projectRootManager.getContentSourceRoots() >> sourceRoots
        String path = "src/main/java"
        sourceRoot.getPath() >> path

        when:
        PitCommandLineArgumentsContainer container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)

        then:
        container.get(PitCommandLineArgument.SOURCE_DIRS) == path
    }

    def "should create container with default target classes"() {
        VirtualFile sourceRoot = Mock()
        PsiDirectory directory = Mock()
        PsiDirectory subdirectory = Mock()
        String packageName = "com"
        VirtualFile[] sourceRoots = [sourceRoot]
        PsiDirectory[] subdirectories = [subdirectory]
        projectRootManager.getContentSourceRoots() >> sourceRoots
        sourceRoot.getPath() >> "anyPath"
        psiManager.findDirectory(sourceRoot) >> directory
        directory.getSubdirectories() >> subdirectories
        subdirectory.getName() >> packageName

        when:
        PitCommandLineArgumentsContainer container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)

        then:
        container.get(PitCommandLineArgument.TARGET_CLASSES) == packageName + ".*"
    }

}
