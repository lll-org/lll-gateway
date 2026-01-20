# ---- Runtime image (recommended) ----
# 用 JRE 镜像跑 Spring Boot，体积更小
FROM eclipse-temurin:21-jre

# 可选：容器内统一时区（你也可以不写）
ENV TZ=Asia/Shanghai

# 建议：不要用 root 跑服务（更安全）
RUN useradd -m appuser
USER appuser

WORKDIR /app

# 你的 jar 在 GitHub Actions 里已经打包好了：target/*.jar
# 这里用通配符拷贝（只要 target 下只有一个可执行 jar）
COPY target/*.jar app.jar

# Spring Boot 默认 8080，如果你项目不是 8080，就改这里（只是声明，不影响实际监听）
EXPOSE 8080

# JVM 参数可以通过环境变量追加：JAVA_OPTS
# 例如：-Xms256m -Xmx512m 等
ENV JAVA_OPTS=""

# 启动命令
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
