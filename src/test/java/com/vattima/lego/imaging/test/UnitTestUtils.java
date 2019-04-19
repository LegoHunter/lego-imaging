package com.vattima.lego.imaging.test;

import com.vattima.lego.imaging.LegoImagingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitTestUtils {
    private UnitTestUtils() {
    }

    public static void deleteSubDirectoriesInPath(Path path) throws IOException {
        Files.walk(path)
             .filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
             .filter(p -> !p.equals(path))
             .forEach(UnitTestUtils::deleteDirectory);
    }

    public static void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(p -> {
                     final boolean deleted = p.delete();
                     System.out.println("Delete [" + p + "] result --> [" + deleted + "]");
                 });
        } catch (IOException e) {
            throw new LegoImagingException(e);
        }
    }

    public static Path renamed(Path oldPathToFile) {
        System.out.println("Calling renamed");
        Pattern pattern = Pattern.compile("(.*DSC_[0-9]+?)-changed\\.(.*)");
        Matcher matcher = pattern.matcher(oldPathToFile.getFileName()
                                                       .toString());
        if (matcher.matches()) {
            Path newPathToFile = oldPathToFile.getParent()
                                              .resolve(matcher.group(1) + "." + matcher.group(2));
            try {
                Files.move(oldPathToFile, newPathToFile);
                System.out.println("Renamed file from [" + oldPathToFile + "] to [" + newPathToFile + "]");
                return newPathToFile;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Could not rename file [" + oldPathToFile + "]");
        }
    }
}
