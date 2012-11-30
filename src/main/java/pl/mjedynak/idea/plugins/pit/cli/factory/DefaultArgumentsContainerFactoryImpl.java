package pl.mjedynak.idea.plugins.pit.cli.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import pl.mjedynak.idea.plugins.pit.ProjectDeterminator;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer;
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainerImpl;
import pl.mjedynak.idea.plugins.pit.cli.model.PitCommandLineArgument;

public class DefaultArgumentsContainerFactoryImpl implements DefaultArgumentsContainerFactory {

    static final String DEFAULT_REPORT_DIR = "report";
    static final String MAVEN_REPORT_DIR = "target/report";

    private ProjectRootManager projectRootManager;

    private PsiManager psiManager;

    private ProjectDeterminator projectDeterminator;

    public DefaultArgumentsContainerFactoryImpl(ProjectRootManager projectRootManager, PsiManager psiManager, ProjectDeterminator projectDeterminator) {
        this.projectRootManager = projectRootManager;
        this.psiManager = psiManager;
        this.projectDeterminator = projectDeterminator;
    }

    @Override
    public PitCommandLineArgumentsContainer createDefaultPitCommandLineArgumentsContainer(Project project) {
        PitCommandLineArgumentsContainer container = new PitCommandLineArgumentsContainerImpl();
        addReportDir(project, container);
        addSourceDir(container);
        addTargetClasses(container);
        return container;
    }

    private void addReportDir(Project project, PitCommandLineArgumentsContainer container) {
        VirtualFile baseDir = project.getBaseDir();
        String reportDir;
        if (baseDir != null) {
            String suffix = DEFAULT_REPORT_DIR;
            if (projectDeterminator.isMavenProject(project)) {
                suffix = MAVEN_REPORT_DIR;
            }
            reportDir = baseDir.getPath() + "/" + suffix;
            container.put(PitCommandLineArgument.REPORT_DIR, reportDir);
        }
    }

    private void addSourceDir(PitCommandLineArgumentsContainer container) {
        VirtualFile[] sourceRoots = projectRootManager.getContentSourceRoots();
        if (isAtLeastOneSourceRoot(sourceRoots)) {
            String sourceRootPath = sourceRoots[0].getPath();
            container.put(PitCommandLineArgument.SOURCE_DIRS, sourceRootPath);
        }
    }

    private void addTargetClasses(PitCommandLineArgumentsContainer container) {
        VirtualFile[] sourceRoots = projectRootManager.getContentSourceRoots();
        if (isAtLeastOneSourceRoot(sourceRoots)) {
            PsiDirectory directory = psiManager.findDirectory(sourceRoots[0]);
            addTargetClassesIfDirectoryExists(container, directory);
        }
    }

    private void addTargetClassesIfDirectoryExists(PitCommandLineArgumentsContainer container, PsiDirectory directory) {
        if (directory != null) {
            PsiDirectory[] subdirectories = directory.getSubdirectories();
            if (isAtLeastOneSubdirectory(subdirectories)) {
                container.put(PitCommandLineArgument.TARGET_CLASSES, subdirectories[0].getName() + ".*");
            }
        }
    }

    private boolean isAtLeastOneSourceRoot(VirtualFile[] sourceRoots) {
        return sourceRoots != null && sourceRoots.length > 0;
    }

    private boolean isAtLeastOneSubdirectory(PsiDirectory[] subdirectories) {
        return subdirectories != null && subdirectories.length > 0;
    }
}
