package ch.fhnw.thga.gradleplugins;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class SharedTaskLogicTest 
{
    @Test
    void given_valid_frege_module_name_then_can_extract_class_name()
    {
    }
}