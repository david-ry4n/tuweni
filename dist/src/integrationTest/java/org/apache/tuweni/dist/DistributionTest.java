// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.dist;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;

/** Tests no duplicate entries are present in the distribution zip file. */
class DistributionTest {

  private static Path distributionsFolder() {
    File currentFile = null;
    try {
      currentFile =
          new File(
              DistributionTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    // dist/build/classes/java/org/apache/tuweni/dist
    Path parentFolder = currentFile.toPath();
    while (!"dist".equals(parentFolder.getFileName().toString())
        && parentFolder.getParent() != null) {
      parentFolder = parentFolder.getParent();
    }
    return parentFolder.resolve("build").resolve("distributions").toAbsolutePath();
  }

  @SuppressWarnings("JdkObsolete")
  @Test
  void testZipFileContents() throws IOException {
    Map<String, Set<String>> dupes = new HashMap<>();
    boolean foundOneZipFile = false;
    Path distFolder = distributionsFolder();
    File distFolderFile = distFolder.toFile();
    assertTrue(distFolderFile.exists(), distFolder.toAbsolutePath() + " doesn't exist");
    for (File archive : distFolderFile.listFiles()) {
      if (archive.getName().endsWith(".zip")) {
        foundOneZipFile = true;
        Set<String> duplicates = new HashSet<>();
        Set<String> files = new HashSet<>();
        try (ZipFile zip = new ZipFile(archive)) {
          Enumeration<? extends ZipEntry> entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!files.add(entry.getName())) {
              duplicates.add(entry.getName());
              dupes.putIfAbsent(archive.getName(), duplicates);
            }
          }
        }
      }
    }
    assertTrue(foundOneZipFile);
    for (Map.Entry<String, Set<String>> entry : dupes.entrySet()) {
      System.out.println("Archive :" + entry.getKey());
      for (String file : entry.getValue()) {
        System.out.println("\t-" + file);
      }
    }
    assertTrue(dupes.isEmpty());
  }
}
