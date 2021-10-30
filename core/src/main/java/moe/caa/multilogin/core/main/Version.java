package moe.caa.multilogin.core.main;

import lombok.AllArgsConstructor;

import java.util.Locale;
import java.util.Objects;

/**
 * 版本号解析器
 */
@AllArgsConstructor
public class Version {
    private final int majorVersion;
    private final int minorVersion;
    private final Type greekAlphabet;
    private final int phaseVersion;

    public static Version fromString(String input) {
        int majorVersion;
        int minorVersion;
        Type greekAlphabet;
        int phaseVersion;
        String[] args = input.split("\\.");
        if (args.length < 2) throw new IllegalArgumentException(input);
        majorVersion = Integer.parseInt(args[0]);
        if (!args[1].contains("-")) {
            minorVersion = Integer.parseInt(args[1]);
            return new Version(majorVersion, minorVersion, Type.RELEASE, -1);
        }
        phaseVersion = Integer.parseInt(args[2]);
        args = args[1].split("-");
        minorVersion = Integer.parseInt(args[0]);
        greekAlphabet = Type.valueOf(args[1].toUpperCase(Locale.ROOT));
        return new Version(majorVersion, minorVersion, greekAlphabet, phaseVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return majorVersion == version.majorVersion && minorVersion == version.minorVersion && phaseVersion == version.phaseVersion && greekAlphabet == version.greekAlphabet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(majorVersion, minorVersion, greekAlphabet, phaseVersion);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(majorVersion).append('.').append(minorVersion);
        if (greekAlphabet != Type.RELEASE) {
            sb.append('-').append(greekAlphabet.name()).append('.').append(phaseVersion);
        }
        return sb.toString();
    }

    public boolean shouldUpdate(Version version) {
        if (version.majorVersion > majorVersion) return true;
        if (version.majorVersion < majorVersion) return false;
        if (version.minorVersion > minorVersion) return true;
        if (version.minorVersion < minorVersion) return false;
        if (version.greekAlphabet.level > greekAlphabet.level) return true;
        if (version.greekAlphabet.level < greekAlphabet.level) return false;
        return version.phaseVersion > phaseVersion;
    }

    private enum Type {
        ALPHA(1),
        BETA(2),
        RC(3),
        RELEASE(4);

        private final int level;

        Type(int level) {
            this.level = level;
        }
    }
}
