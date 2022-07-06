package moe.caa.multilogin.core.main;

import com.google.gson.JsonParser;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.ohc.LoggingInterceptor;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import moe.caa.multilogin.core.semver.SemVersion;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CheckUpdater {
    private final MultiCore core;

    public CheckUpdater(MultiCore core) {
        this.core = core;
    }

    public List<SemVersion> getLatestVersionNow() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new RetryInterceptor(3, 10000))
                .writeTimeout(Duration.ofMillis(2000))
                .readTimeout(Duration.ofMillis(2000))
                .connectTimeout(Duration.ofMillis(2000))
                .build();
        Request build = new Request.Builder().get().url("https://api.github.com/repos/CaaMoe/MultiLogin/contents/latest").build();
        Call call = client.newCall(build);
        try (Response execute = call.execute();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            String content = JsonParser.parseString(Objects.requireNonNull(execute.body()).string())
                    .getAsJsonObject().getAsJsonPrimitive("content").getAsString();
            for (String s : content.split("\n")) {
                baos.writeBytes(Base64.getDecoder().decode(s));
            }
            baos.flush();
            return Arrays.stream(baos.toString(StandardCharsets.UTF_8).split("\n"))
                    .map(SemVersion::of).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    public void start() {
        core.getPlugin().getRunServer().getScheduler().runTaskAsyncTimer(() -> {
            try {
                List<SemVersion> latestVersionNow = getLatestVersionNow();
                if (latestVersionNow.isEmpty()) return;
                if (core.getSemVersion() == null) {
                    LoggerProvider.getLogger().info(String.format("The latest version is %s, please update.",
                            ValueUtil.join(", ", " and ", latestVersionNow.stream()
                                    .map(Object::toString).collect(Collectors.toList()))
                    ));
                } else {
                    SemVersion sv = core.getSemVersion();
                    for (SemVersion version : latestVersionNow) {
                        if (sv.needUpgrade(version)) {
                            sv = version;
                        }
                    }
                    if (!sv.equals(core.getSemVersion())) {
                        LoggerProvider.getLogger().info(
                                String.format("The latest recommended version is %s, Please update.", sv
                                ));
                    }
                }
            } catch (IOException e) {
                LoggerProvider.getLogger().error("Check update failure.", e);
            }
        }, 0, 1000 * 60 * 60 * 24); // 一天一次更新检查
    }
}
