package moe.caa.multilogin.core.semver;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import moe.caa.multilogin.api.internal.util.ValueUtil;

import java.util.Locale;

/**
 * 语义化版本号处理工具
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class SemVersion {
    private final int major;
    private final int minor;
    private final int patch;
    private final VersionSuffix suffixes;
    private final int suffixesBd;


    public static SemVersion of(String version) {
        if (ValueUtil.isEmpty(version)) return null;
        if (version.toLowerCase(Locale.ROOT).startsWith("build_")) return null;
        // 1.0.0-RC.4
        String[] split = version.split("-");
        String[] mmp = split[0].split("\\.");
        if (split.length == 1) {
            return new SemVersion(Integer.parseInt(mmp[0]), Integer.parseInt(mmp[1]), Integer.parseInt(mmp[2]),
                    VersionSuffix.NONE, -1);
        }

        if (split.length == 2) {
            split = split[1].split("\\.");
            return new SemVersion(Integer.parseInt(mmp[0]), Integer.parseInt(mmp[1]), Integer.parseInt(mmp[2]),
                    VersionSuffix.valueOf(split[0]), Integer.parseInt(split[1]));
        }
        return null;
    }

    @Override
    public String toString() {
        if (suffixes == VersionSuffix.NONE) return String.format("%d.%d.%d", major, minor, patch);
        return String.format("%d.%d.%d-%s.%d", major, minor, patch, suffixes.name(), suffixesBd);
    }

    public boolean needUpgrade(SemVersion version) {
        // 如果 suffixes 等级降级，就不需要更新。
        // 比如 1.0.0 和 1.1.0-RC.1，不需要更新
        if (version.suffixes.mj < suffixes.mj) return false;

        // 在前面的版本号相同的情况下
        if (version.major == major && version.minor == minor && version.patch == patch) {
            // suffixes 提升一个等级，就需要更新
            // 比如 1.0.0-BETA.1 和 1.0.0-RC.1，需要更新
            if (version.suffixes.mj > suffixes.mj) return true;
            // suffixesBd 提升一个等级，就需要更新
            // 比如 1.0.0-BETA.1 和 1.0.0-BETA.2，需要更新
            if (version.suffixesBd > suffixesBd) return true;
        }

        // 在前面的版本号比较大的情况下
        // 全部要更新
        return needUpgradeIgnoreSuffixes(version);
    }

    public boolean needUpgradeIgnoreSuffixes(SemVersion version) {
        return version.major >= major && version.minor >= minor && version.patch > patch;
    }

    enum VersionSuffix {
        NONE(3),
        RC(2),
        BETA(1),
        ALPHA(0);

        private final int mj;

        VersionSuffix(int mj) {
            this.mj = mj;
        }
    }
}
