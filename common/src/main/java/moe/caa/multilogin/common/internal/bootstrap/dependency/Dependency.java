package moe.caa.multilogin.common.internal.bootstrap.dependency;

record Dependency(
        String groupId,
        String artifactId,
        String version
) {

    public static Dependency ofString(String dependencyString) {
        String[] parts = dependencyString.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid dependency string format. Expected format: groupId:artifactId:version");
        }
        return new Dependency(parts[0], parts[1], parts[2]);
    }

    public String getJarFilename() {
        return String.format("%s-%s.jar", artifactId, version);
    }

    public String getJarPath() {
        return String.format("%s/%s/%s/%s", groupId.replace('.', '/'), artifactId, version, getJarFilename());
    }

    public String getRelocatedJarPath() {
        return String.format("%s/%s/%s/relocated-%s", groupId.replace('.', '/'), artifactId, version, getJarFilename());
    }


    public String generateJarDownloadURL(String repository) {
        return repository + "/" + getJarPath();
    }

    public String generateJarDownloadSha1URL(String repository) {
        return repository + "/" + getJarPath() + ".sha1";
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
