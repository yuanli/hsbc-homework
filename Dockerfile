# 使用官方的 OpenJDK 21 作为基础镜像
FROM openjdk:21-jdk-slim

# 将 JAR 文件复制到容器中
COPY target/hsbc-transaction-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用程序运行的端口
EXPOSE 8000

# 设置容器启动时执行的命令
ENTRYPOINT ["java",  "-jar", "app.jar"]