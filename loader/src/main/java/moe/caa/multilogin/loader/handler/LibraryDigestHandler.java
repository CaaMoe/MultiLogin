package moe.caa.multilogin.loader.handler;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.loader.library.Library;
import moe.caa.multilogin.loader.main.PluginLoader;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LibraryDigestHandler {
    public static Map<Library, String> DIGESTED_MAP = Collections.emptyMap();

    static {
        try (InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream(".digested");
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(resourceAsStream));
             LineNumberReader lnr = new LineNumberReader(isr)
        ) {
            Map<Library, String> tMap = new HashMap<>();
            lnr.lines().map(s -> s.split("="))
                    .forEach(ss -> tMap.put(Library.of(ss[0]), ss[1]));

            DIGESTED_MAP = Collections.unmodifiableMap(tMap);
        } catch (Throwable throwable) {
            LoggerProvider.logger.error("Unable to load internal file .digested", throwable);
        }
    }

    public ValidateResult validate(Library library, File libraryFile) throws Exception {
        String string = DIGESTED_MAP.get(library);
        if (string == null) return ValidateResult.NO_RECORD;

        String calculated = calculateDigest(libraryFile);


        if (string.equals(calculated)) {
            return ValidateResult.MATCHED;
        }
        return ValidateResult.NO_MATCHED;
    }

    private String calculateDigest(File file) throws Exception {

        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }

    public enum ValidateResult {
        NO_RECORD,
        NO_MATCHED,
        MATCHED
    }
}
