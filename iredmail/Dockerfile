#编译环境
FROM lianshufeng/maven:jdk17 as build
MAINTAINER lianshufeng <251708339@qq.com>


ARG FILE_NAME="iredapi-0.0.1-SNAPSHOT.jar"
ARG JAR_FILE="/opt/jar"

# 拷贝源码
RUN mkdir /tmp/source
Add ./ /tmp/source

#下载源码
RUN set -xe \
	&& source /etc/profile \
	&& cd /tmp/source \
	&& mvn package \
	&& mkdir -p $JAR_FILE \
	&& cp /tmp/source/target/$FILE_NAME $JAR_FILE/$FILE_NAME \
	
	#刷新环境变量
	&& source /etc/profile 


#运行环境
FROM lianshufeng/springboot:jdk17
ARG FILE_NAME="iredapi-0.0.1-SNAPSHOT.jar"
ARG JAR_FILE="/opt/jar"
COPY --from=build $JAR_FILE/$FILE_NAME $JAR_FILE/$FILE_NAME 
WORKDIR $JAR_FILE


#默认的启动命令
ENV ENTRYPOINT="nohup java -Dfile.encoding=UTF-8 -Xmx300m -Xms100m -Duser.timezone=GMT+8 -jar $JAR_FILE/$FILE_NAME"


#启动项
ENTRYPOINT  sh /opt/bootstrap.sh 



	





