1. 手动编译
   1. 编译 velocity
   2. 复制 proxy 和 api 下带 SNAPSHOT 的 jar 进来

    例子：
    
    ```
    git clone --depth 1 https://github.com/PaperMC/Velocity Velocity
    cd Velocity
    gradle build
    cp proxy/build/libs/velocity-proxy-*-SNAPSHOT.jar ../
    cp api/build/libs/velocity-api-*-SNAPSHOT.jar ../
    ```
2. Gradle自动编译
   1. 在根目录下运行 `./gradlew MultiLogin:velocity:getLatestVelocity` 或 `gradle MultiLogin:velocity:getLatestVelocity`