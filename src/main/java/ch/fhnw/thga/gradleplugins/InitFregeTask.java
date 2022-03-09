package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.NEW_LINE;
import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.EMPTY_LINE;

import java.io.IOException;
import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

public abstract class InitFregeTask extends DefaultTask
{
    private static final String HELLO_FREGE_CODE_WITHOUT_MODULE = String.join(NEW_LINE,
        "import Test.QuickCheck",
        EMPTY_LINE,
        "--- compute digit sum",
        "digitSum :: Integer -> Integer",
        "digitSum 0 = 0",
        "digitSum n = (n `rem` 10) + digitSum (n `div` 10)",
        EMPTY_LINE,
        "--- compute the reduced digit sum",
        "reducedDigitSum :: Integer -> Integer",
        "reducedDigitSum n = if n < 10 then n else reducedDigitSum $ digitSum n",
        EMPTY_LINE,
        "main = do",
        "    let answer = digitSum 6666666",
        "    println $",
        "        \"The answer to life, the universe and everything is \"",
        "        ++ show answer",
        "        ++ \".\"",
        EMPTY_LINE,
        "{--",
        "    The property 'p_reduced_digit_sum_of_multiple_of_9_is_always_9' checks",
        "    the famous claim that every multiple of 9 number has also the reduced",
        "    digit sum of 9, e.g. 9, 27, 36, ...", 
        "-}",
        "p_reduced_digit_sum_of_multiple_of_9_is_always_9 = ",
        "    property $ \\(n :: Integer) -> (n > 0) ==> (reducedDigitSum $ 9 * n) == 9"
    );

    @Input
    @Option(option = "moduleName",
           description = "The module name of the default frege file"
    )
    public abstract Property<String> getFregeModuleName();

    @Internal
    final Provider<String> getHelloFregeCode()
    {
        return getFregeModuleName()
            .map(moduleName -> String.format("module %s where", moduleName))
            .map(firstLine  -> String.join(
                NEW_LINE, 
                firstLine, HELLO_FREGE_CODE_WITHOUT_MODULE
            ));
    }

    @Internal
    final Provider<String> getFregeFilePath()
    {
        return getFregeModuleName()
            .map(moduleName -> moduleName.replace(".", "/"))
            .map(filePath   -> Paths.get(filePath).normalize())
            .map(filePath   -> String.format("%s.fr", filePath.toString()));
    }
    
    @OutputDirectory
    public abstract DirectoryProperty getFregeMainSourceDir();

    @OutputFile
    final Provider<RegularFile> getHelloFregeFile()
    {
        return getFregeMainSourceDir().file(getFregeFilePath());
    }

    @TaskAction
    public void writeHelloFregeFile() throws IOException
    {
        SharedTaskLogic.writeToFile(
            getHelloFregeFile().get().getAsFile(),
            getHelloFregeCode().get()
        );
    }
}
