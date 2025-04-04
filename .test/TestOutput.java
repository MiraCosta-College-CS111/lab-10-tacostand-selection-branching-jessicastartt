
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOutput
{
    @ParameterizedTest(name="{0}")
    @CsvFileSource(resources = "output_tests.csv")
    public void testOutputMatch(String testCaseName, String input, String expectedOutput, String matchType)
    {
        // Capture stdout
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Send input
        InputStream originalIn = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        resetUtilityBelt(in);

        // Run the class and capture stdout
        Main.main(null);
        String actualOutput = outputStream.toString().trim();
        System.setOut(originalOut);
        System.setIn(originalIn);

        // Perform the corresponding assertion based on the match type
        switch (matchType) {
            case "exact":
                assertEquals(expectedOutput, actualOutput);
                break;
            case "match":
                assertTrue(actualOutput.contains(expectedOutput), "Match failed for " + testCaseName +
                        "\n" + actualOutput + " does not contain " + expectedOutput);
                break;
            case "regex":
                assertTrue(Pattern.matches(expectedOutput, actualOutput), "Regex match failed for " + testCaseName +
                        "\n" + actualOutput + " does is not matched by pattern " + expectedOutput);
                break;
            default:
                fail("Invalid match type for " + testCaseName);
        }
    }

    /**
     * Sets a new Scanner in UtilityBelt to the new System.in we are providing.
     * @param in
     */
    private void resetUtilityBelt(ByteArrayInputStream in) {
        try {
            Class<?> clazz = Class.forName("UtilityBelt");
            Field field = clazz.getDeclaredField("keyboard");
            field.setAccessible(true);
            field.set(clazz, new Scanner(in));
        } catch (Exception e) {
            // UtilityBelt may not be used or be part of the project.
            System.out.println("Info: UtilityBelt not reset.");
        }
    }
}

