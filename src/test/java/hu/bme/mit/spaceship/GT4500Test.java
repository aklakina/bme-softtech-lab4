package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GT4500Test {

  private GT4500 ship;

  @BeforeEach
  public void init(){
    this.ship = new GT4500();
  }

  @Test
  public void fireTorpedo_Single_Success(){
    // Arrange

    // Act
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    // Assert
    assertEquals(true, result);
  }

  @Test
  public void fireTorpedo_All_Success(){
    // Arrange

    // Act
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    // Assert
    assertEquals(true, result);
  }

  /**
   * Test the ship using the command line interface.
   *
   * Input commands are provided as a parameter.
   * Expected outputs may be provided in another file.  In case the
   * output file does not exist, it is simply ignored and the test is
   * inconclusive.
   */
  @ParameterizedTest
  @MethodSource("provideTestFiles")
  void runCommandsFromFile_Success(File inputFile, File outputFile) throws IOException {
      // Arrange
      InputStream in = new FileInputStream(inputFile);
      OutputStream actualOut = new ByteArrayOutputStream();

      // Act
      CommandLineInterface.run(in, actualOut);

      // Assert
      if (! outputFile.exists()) {
        // No output file was provided; test is inconclusive but we
        // still get coverage metrics for the execution
        inconclusive();
      } else {
        try (InputStream expectedOut = new FileInputStream(outputFile)) {
          String expected = normalizeString(new String(expectedOut.readAllBytes(), StandardCharsets.UTF_8));
          String actual = normalizeString(actualOut.toString());
          assertEquals(expected, actual);
        }
      }
  }

  private static Stream<Arguments> provideTestFiles() throws IOException {
    final String rootFolderForTestInputsAndOutputs = "src/main/resources";
    File resourceFolder = new File(rootFolderForTestInputsAndOutputs);
    Map<File, File> files = provideFilesWithPrefix("input", "output", resourceFolder);
    return files.entrySet().stream()
        .map(it -> Arguments.of(
            it.getKey(), it.getValue()));
  }

  private static Map<File, File> provideFilesWithPrefix(String prefixOne, String prefixTwo, File root) throws IOException {
    Map<File, File> filePairs = new LinkedHashMap<File, File>();
    if (root.isDirectory()) {
      File[] files = root.listFiles();
      for (File file : files) {
        if (file.isDirectory()) { // Recursion for directory
          filePairs.putAll(
              provideFilesWithPrefix(prefixOne, prefixTwo, file));
        }
        else { // File - we have to check it
          String name = file.getName().trim();
          if (name.startsWith(prefixOne)) { // Match - key
            filePairs.put(file, null);
          }
          else if (name.startsWith(prefixTwo)) { // Match - value
            String twoPostName = name.substring(prefixTwo.length());
            // Searching for the key file
            for (File key : filePairs.keySet()) {
              String onePostName = key.getName().substring(prefixOne.length());
              if (onePostName.equals(twoPostName)) {
                filePairs.replace(key, file);
              }
            }
          }
        }
      }
    }
    return filePairs;
  }
  
  /**
   * Utility method to force a test result to be 'inconclusive'.
   */
  private static void inconclusive() {
    Assumptions.assumeTrue(false, "Inconclusive");
  }

  /**
   * Normalize a string by stripping all leading and trailing whitespace
   * and replacing all whitespace with a single space.
   */
  private static String normalizeString(String s) {
    return s.strip().replaceAll("\\s+", " ");
  }
}