## Velocity 模块缺失依赖补全步骤

1. 前往放 [velocity](https://github.com/PaperMC/Velocity) 源码的地方把他克隆到本地或 codespaces 里.
2. 执行 `./gradlew build` 来编译 velocity.
3. 复制 proxy 下的 velocity-proxy-*-SNAPSHOT.jar 文件扔进来.

例子：

```
git clone --depth 1 https://github.com/PaperMC/Velocity Velocity
cd Velocity
gradle build
cp proxy/build/libs/velocity-proxy-*-SNAPSHOT.jar ../
```

或者你也可以, 直接到 [这个地方](https://papermc.io/software/velocity) 下载最新的 velocity 本体扔进来.