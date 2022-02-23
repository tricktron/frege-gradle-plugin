package ch.fhnw.thga.gradleplugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.extractClassNameFromFregeModuleName;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

@TestInstance(Lifecycle.PER_CLASS)
public class SharedTaskLogicTest 
{
    @Example
    void given_valid_frege_module_name_then_can_extract_class_name()
    {
        assertEquals(
            extractClassNameFromFregeModuleName("ch.fhnw.thga.Completion"),
            "Completion"
        );
    }

    @Example
    void given_empty_frege_module_name_then_returns_empty_string()
    {
        assertEquals(
            extractClassNameFromFregeModuleName(""),
            ""
        );
    }

    @Example
    void module_name_without_a_package_equals_class_name()
    {
        assertEquals(
            extractClassNameFromFregeModuleName("Completion"),
            "Completion"
        );
    }

    @Property
    void class_name_is_the_suffix_of_the_full_module_name(
        @ForAll String aString)
    {
        String expectedFregeClassName = "Completion";
        assertEquals(
            extractClassNameFromFregeModuleName(String.join(".", expectedFregeClassName)),
            expectedFregeClassName);
    }
}