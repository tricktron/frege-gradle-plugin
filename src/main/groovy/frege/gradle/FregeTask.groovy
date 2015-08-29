package frege.gradle

import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.compile.CompilationFailedException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import static org.apache.commons.io.FilenameUtils.removeExtension


class FregeTask extends AbstractCompile {


    // todo set names
    private static final String FREGE_COMPILER_CLASS_NAME = 'frege.compiler.Main'
   	public static final String FREGE_CLASSPATH_FIELD = 'fregeClasspath'
    FileCollection fregeClasspath




    static String DEFAULT_CLASSES_SUBDIR = "classes/main"       // TODO: should this come from a convention?
    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?

    @Optional @Input
    String xss = "4m"

    @Optional @Input
    boolean hints = false

    @Optional @Input
    boolean verbose = false

    @Optional @Input
    boolean inline = true

    @Optional @Input
    boolean make = true

    @Optional @Input
    boolean skipCompile = false

    @Optional @Input
    String extraArgs = ""

    @Optional @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Optional @Input
    String module = ""

    @Optional @InputDirectory
    File sourceDir = new File(project.projectDir, DEFAULT_SRC_DIR).exists() ?  new File(project.projectDir, DEFAULT_SRC_DIR) : null

    @Optional @OutputDirectory
    File outputDir = new File(project.buildDir, DEFAULT_CLASSES_SUBDIR)

    @TaskAction
    void compile() {
        ensureFregeConfigurationSpecified()

        if (! outputDir.exists() ) {
            logger.info "Creating output directory '${outputDir.absolutePath}'."
            outputDir.mkdirs()
        }

        // access extension configuration values as ${project.frege.key1}

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain(FREGE_COMPILER_CLASS_NAME)
        action.setClasspath(project.files(project.configurations.compile))

        List jvmargs = []
        if (xss)
            jvmargs << "-Xss$xss"
        action.setJvmArgs(jvmargs)

        def args = allArgs ? allArgs.split().toList() : assembleArguments()

        logger.info("Calling Frege compiler with args: '$args'")
        action.args(args)
        action.execute()
    }

    protected List assembleArguments() {
        List args = []
        if (hints)
            args << "-hints"
        if (inline)
            args << "-inline"
        if (make)
            args << "-make"
        if (verbose)
            args << "-v"
        if (skipCompile)
            args << "-j"

        if (sourceDir != null) {
            args << "-sp"
            args << sourceDir.absolutePath
        }

        args << "-d"
        args << outputDir

        if (!module && !extraArgs) {
            logger.info "no module and no extra args given: compiling all of the sourceDir"
            if (sourceDir != null) {
                args << sourceDir.absolutePath
            }

        } else if (module) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = args + extraArgs.split().toList()
        }
        args
    }

   	protected void ensureFregeConfigurationSpecified() {
   		if (getFregeClasspath().empty) {
   			throw new InvalidUserDataException('You must assign a Frege library to the "frege" configuration.')
   		}
   	}

}