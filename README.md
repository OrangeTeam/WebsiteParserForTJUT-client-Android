# 天津理工大学学生信息查询系统-移动校园

本项目是面向天津理工大学学生的移动校园Android应用。用户可以随时随地用它查看课表、成绩、通知以及学籍等个人信息。

![屏幕截图][screenshot]

## 从源代码构建
本项目使用[Gradle][]自动化构建系统。

### 准备

在进行以下操作前，请安装并配置好[JDK][] 7+、[Android SDK][]和[Git][]。

您可以在local.properties配置文件中设置`sdk.dir`属性，来指定Android SDK的路径。

### 克隆代码库
`git clone https://github.com/OrangeTeam/Query-System-for-Tianjin-University-of-Technology.git`

### 编译，测试，生成APK安装包
`./gradlew build`

#### Tips
本项目依赖于[WebsiteParserForTJUT][]和[WebsiteParserForTJUT-proxy-GAE][]，在编译本项目之前需把这两个项目的工件发布到Maven本地仓库（可在这两个项目下运行`./gradlew install`）。

`build`任务依赖于`assembleRelease`任务，这个Gradle任务会在app/build/apk目录下生成用于发布的apk安装包。
默认情况下，生成的安装包没有经过签名，您需要自己为它签名。如果在项目根目录下，有根据sample_local.gradle创建的local.gradle文件，并且它设置了signingConfigs属性，那么`assembleRelease`任务会自动生成经过签名的安装包。
如果您还不熟悉Android应用签名或还没有release key，请参考官方文档[Signing Your Applications]。

### 其他
您可以通过`./gradlew tasks`查找到更多可用任务。

您可以在[Gradle官网][man:Gradle]和[Android Gradle插件用户指导][man:AndroidPlugin]找到更详细的文档。

如果您使用Android Studio IDE，您可以直接用它导入本项目。

[screenshot]: screenshot/1.png
[WebsiteParserForTJUT]: https://github.com/OrangeTeam/WebsiteParserForTJUT
[Gradle]: http://gradle.org
[JDK]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[Android SDK]: http://developer.android.com/sdk/installing/adding-packages.html
[Git]: http://git-scm.com
[WebsiteParserForTJUT]: https://github.com/OrangeTeam/WebsiteParserForTJUT
[WebsiteParserForTJUT-proxy-GAE]: https://github.com/OrangeTeam/WebsiteParserForTJUT-proxy-GAE
[Signing Your Applications]: http://developer.android.com/tools/publishing/app-signing.html
[man:Gradle]: http://www.gradle.org/documentation
[man:MavenLocal]: http://www.gradle.org/docs/current/userguide/dependency_management.html#sub:maven_local
[man:AndroidPlugin]: http://tools.android.com/tech-docs/new-build-system/user-guide "Gradle Plugin User Guide"
