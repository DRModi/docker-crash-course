# Docker for Java Developers

### Basic Commands with information


								Docker for Java Spring Developer
								~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 

> Docker Registry: hub.docker.com > Docker Repositories: drmodi/<<image name>>

> Docker Image: Static Version (think as like class)


#######################
	CONTAINERS
#######################


> Docker Container: Running version of image/application (think like as an object)
	docker run <<image name>>
		or 
	docker container run <<image name>>

> list of containers:
	docker container ls
		OR
	docker ps -a

> -d: detach mode, running the container

> docker logs: to see the container logs

> docker logs -f: tailing the logs
	docker container logs -f <<container id>>

> container pause
	docker container pause <<container name>>
	Note: it will not stop the container, it just pause the container in specific state.

> container unpause
		docker container unpause <<container name>>
		
> container stop: it will shutdown container gracefully.
	docker container stop <<container id>>
		OR
	docker stop <<container id>>
	
	Note: Stop=>SIGTERM (signal termination, which give around 10 sec or so to stop)=> Graceful shutdown.
	      Meaning: it will stop shutting down executer service, stopping JPA entityfactory, dropping
		  table sequences and the pool connection, etc..
		  
> Killing containers: it will not give anytime to container and kill it immediately
	docker container kill <<container id>>
		OR
	docker kill <<container id>>
	
	Note: kill=>SIGKILL=>Immediately terminate the process
	
> docker restart policy: default value is no.
	docker run -p 8081:5000 -d --restart=always/no <<image name>>
	
	Note: If container start with --restart=always, and it is in stop mode after stopping the container,
	   and not removed fromt the list whenever docker demon/engine restarted, this container will start
	   automatically with running the run command. If we have removed from the list using prune, then
	   there is nothing to start. it will be useful for database services,etc.., so in case docker demon
	   restart accidently, this service is also get restarted and available for use.
	   
> docker container restart
	docker container restart <<container name>>
	
> contaier inspect:
	docker inspect <<container id>>
	
> docker container prune:
	Note: this will remove all stopped container. Except running container, all the containers will be
		  removed.
		docker container prune
		
> Docker Event: list the occured events
	docker events
	
	Note: like stop/kill container, volumes unmount, netework disconnected, etc..

> Docker top
	docker top <<container id>>
	
	Note: list the process which are running in this container.
	
> Docker stats:
	docker stats
	
	Note: provide the containers which are running with how much CPU usage and the memory usage, etc...
	
> memory and cpu quota assign to the containers:
	docker run -p <<desireport>>:<<dockerAppPort>> -m <<Memory in MB>> --cpu-quota <<100K for 100%>> -d <<fullImageName>>
	
	example: Running with 1 gb RAM and 10% of the 4 core cpu.
		docker run -p 9043:5000 -m 1024m --cpu-quota 10000 -d mysql
		
> Container running with interactive shell - detach and interactive shell
	docker run -dit openjdk:8-jdk-alpine
	
	use shell command to content in /tmp folder: docker container exec <<container_name>> ls /tmp
		
#######################
	Docker Deamon
#######################

> System df
	docker system df
	
	Note: will list image, containes, volumes, etc.. meaning whatever docker deamon managing.
	
	
	
	
	
	
	



#######################
	IMAGES
#######################
	
	
> list of images
	docker images
	
> add new tag of the images - set tag to latest
	docker tag <<fullpath of the image>>:<<tag name>> <<fullpath of the image>>:latest
	Note: latest doesnt mean always latest tag, meaning it is practice but if someone doesnt follow then 
		  it can be older version as well.
		  
> Search for official image
	docker search mysql
	Note: there are more images, and there are official [OK] images, which are certified by docker team.

> Image History:
	docker image history <<image id>>
	Note: it will list down steps which are being used to create this image.

> Image Inspect:
	docker image inspect <<image id>>
	Note: Tags, container, configruation, entrypoint, environment variable etc..

> Image remove:
	docker rmi <<image name>> 
			OR
	docker remove image remove
	

		









#####################################
	Create Image using manual step
######################################

>> Hello World Simple REST API Image
	(1)Build Jar: cd to below
		/target/hello-world-rest-api.jar
	(2)Setup the pre-requisites for running the JAR
		- openjdk:8-jdk-alpine

		> First run the container with above image
			docker run -dit --name my-jdk8-container openjdk:8-jdk-alpine
			
		> Run the commad docker ps, get the container name and do exec command to check /tmp folder which should be empty
			docker container exec my-jdk8-container ls /tmp
		
	(3) Copy the JAR
			docker container cp target/hello-world-rest-api.jar my-jdk8-container:/tmp
		
		run below commad to make sure, copy is done.
			docker container exec my-jdk8-container ls /tmp
		
	(4) Save the container as an image using commit command, with tag: Startup Command
			docker container commit my-jdk8-container drmodi/hello-world-rest-api:manual_01
			
			Note: Noticed that, image is created and running the container of off the manual_01 will start
			and exit immediately, since it doesnt have any entrypoint/startup command.
			
			Use below command with startup/entrypoint
			docker container commit --change='CMD ["java", "-jar", "/tmp/hello-world-rest-api.jar"]' my-jdk8-container drmodi/hello-world-rest-api:manual_02
		
	(5) Run the JAR
	
		docker run -p 8080:8080 drmodi/hello-world-rest-api:manual_02
		
		
		
		

#####################################
Docker Image - Publish to registry.
######################################

** Rename existing docker image to another name:
	docker image tag <<existing image name>> <<new name>>
	docker image tag drmodi/hello-world-rest-api:0.0.5-SNAPSHOT drmodi/hello-world-rest-api-ke:0.0.1-RELEASE

(1) Login -> docker login
(2) Push -> docker push <<repository>>/<<imageName>>:<<tagName>>
		 -> docker push drmodi/myImage:myTag
		
Another Way is: with in the spotify plugin. so whenever image being built, image will be pushed out.
-> Inside the plugin add another goal along with build and name "push"
	<goal>push</goal>
	
-> Set/configure "maven settings.xml" with server as docker.io and following config
	<servers>
		<server>
			<id>docker-repo.example.com:8080</id>
			<username>username</username>
			<password>password</password>
		</server>
	</servers>

- Best practice is manually have control for pushing the change.
		
		
		
		
		
#########################################################
			Dockerfile 
#########################################################

Note: All the plugin discussed below are work with Java only and its language/tool specific. Meaning similar way dockerfile can be created for python project, or some other lagauge most places basic dockerfile being used which is generic to docker and not to anytool.

Make it automatic Manual Image Creation work using docker file.

Basic Dockerfile Creation:
***********************************************

(1) Create Dockerfile with no extension. name it as Dockerfile
(2) past below commads Base image -> internal exposed port -> Add codebase -> ENTRYPOINT
	
	FROM openjdk:8-jdk-alpine
	EXPOSE 8080
	ADD target/hello-world-rest-api.jar tmp/hello-world-rest-api.jar
	ENTRYPOINT ["sh", "-c", "java -jar /tmp/hello-world-rest-api.jar"]
	
(3) run build command - to create an image
	docker build . -t drmodi/hello-world-rest-api:dockerfile_1
(4) Watch the history of the image to see how it is being built
	docker history drmodi/hello-world-rest-api:dockerfile_1
(5) run the container base off the created image
	docker run -p 8081:8080 drmodi/hello-world-rest-api:dockerfile_1
	
	

Dockerfile - Maven plugin - Spotify Plugin
***********************************************
- This will help while building the project, it will build the dockerfile with latest code - jar
- So This plugin provides the integration of the project build with dockerfile build
- Dockerfile which is used in project will build the image and Spotify plugin will integrate
the docker image creation and projecct build.

## Plugins

### Dockerfile Maven

- From Spotify
- https://github.com/spotify/dockerfile-maven

```
<plugin>
	<groupId>com.spotify</groupId>
	<artifactId>dockerfile-maven-plugin</artifactId>
	<version>1.4.10</version>
	<executions>
		<execution>
			<id>default</id>
			<goals>
				<goal>build</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<repository>in28min/${project.name}</repository>
		<tag>${project.version}</tag>
		<skipDockerInfo>true</skipDockerInfo>
	</configuration>
</plugin>
```

Run Command to build the project, which will build the image
-----------------------------------
mvn package -DskipTests

Run the container:
--------------------
docker run -p 8085:8080 drmodi/hello-world-rest-api:0.0.1-SNAPSHOT



Dockerfile - Make it generic to all project
***********************************************

FROM openjdk:8-jdk-alpine
EXPOSE 8080
# ADD target/hello-world-rest-api.jar hello-world-rest-api.jar
ADD target/*.jar app.jar
# ENTRYPOINT ["sh", "-c", "java -jar /hello-world-rest-api.jar"]
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]



Dockerfile - Make it more efficient 
***********************************************
- Include maven plugin - unpack
- It will split Jar into dependent libraries and code
- Faster image generation and save space at registry location/any docker hub.

<plugin>	
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-dependency-plugin</artifactId>
	<executions>
		<execution>
			<id>unpack</id>
			<phase>package</phase>
			<goals>
				<goal>unpack</goal>
			</goals>
			<configuration>
				<artifactItems>
					<artifactItem>
						<groupId>${project.groupId}</groupId>
						<artifactId>${project.artifactId}</artifactId>
						<version>${project.version}</version>
					</artifactItem>
				</artifactItems>
			</configuration>
		</execution>
	</executions>
</plugin>

### Improve Caching of Images using Layers
 
#### CURRENT SITUATION					

			--------------- 
			    FAT JAR
			--------------- 
			      JDK
			--------------- 

####  DESIRED SITUATION
			--------------- 
			    CLASSES   
			---------------
			 DEPENDENCIES 
			---------------
			     JDK      
			---------------


No Dockerfile Needed - Maven plugin - JIB Plugin - it is doing quite both the unpack and spotify
**********************************************************************************************

Jib is a maven plugin for building docker and OCI (open container initiative) images for Java applications.

It will create direct images, no dockerfile needed.


### JIB
- https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin#quickstart
- https://github.com/GoogleContainerTools/jib/blob/master/docs/faq.md
#### "useCurrentTimestamp - true" discussion
- https://github.com/GooleContainerTools/jib/blob/master/docs/faq.md#why-is-my-image-created-48-years-ago 
- https://github.com/GoogleContainerTools/jib/issues/413 

<plugin>
	<groupId>com.google.cloud.tools</groupId>
	<artifactId>jib-maven-plugin</artifactId>
	<version>1.6.1</version>
	<configuration>
		<container>
			<creationTime>USE_CURRENT_TIMESTAMP</creationTime>
		</container>
	</configuration>
	<executions>
		<execution>
			<phase>package</phase>
			<goals>
				<goal>dockerBuild</goal>
			</goals>
		</execution>
	</executions>
</plugin>

Note: Using JIB you not need to create docker file, just add the plugin and with maven package command, it will create docker image to use.

If we have provided the creation time as configuration, then it will be using EPOCH timing which start time in unix 00:00:00, January 1st, 1970 UTC. So reproducibility is achievable, meaning if same code beubg used then same image can be created after sometime. because it is use default base image for java (in this case is "distroless java", we can change to open jdk if we want to). So base (java is same) and if we have same code then it would generate same image. Hope it make sense.. :)



JIB: Automatically able to figure out the entrypoint.
------------------------------------------------------
[INFO] Container entrypoint set to [java, -cp, /app/resources:/app/classes:/app/libs/*, com.in28minutes.rest.webservices.restfulwebservices.RestfulWebServicesApplication]
[INFO]
[INFO] Built image to Docker daemon as 01-hello-world-rest-api:0.0.1-SNAPSHOT



Another Plugin - Fabric8IO, which was famous during 2015
*********************************************************
- in this plugin, dockerfile can be used or provide similar configuration during the plugin specified in the pom.xml



Command Difference:
********************

(1) ADD vs COPY:
	> COPY: It allows to copy file, directory in container image, however with copy specifying URL is not possible.
	> ADD: It allows to copy, local file, directory and also specified url as well, so that using that url it can download
			from the URL and then add into the container.
			
	Note: To copy the file, directories, binaries (jar/war) from local to container image, prefer COPY over add.

(2) CMD vs ENTRYPOINT:
	> CMD: Default Parameters. When you pass parameters from command line, your default parameters are overriden.
		   Meaning in below example, while running the container, if extra parameter being passed then that will override the
		   below default parameter.
	
		   Example: CMD ["catalina.sh","run"]
		   
	> ENTRYPOINT: Making a container as an executable. Meaning specified arguments in ENTRYPOINT can not be overriden.
		
		Example: ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]
		
		Just for knowledge, using "argument/s" --entrypoint, can override the whole entrypoint which is configured. But 
		by default only adding arguments in commands won't be override the specified arguments in ENTRYPOINT.
		
	Note: Best practice is using ENTRYPOINT whenever it is possible.
	
	





********************************
	Deployment Configurations:
********************************

(1) JAR deployment: 
	FROM openjdk:8-jdk-alpine
	EXPOSE 8080
	# ADD target/hello-world-rest-api.jar hello-world-rest-api.jar
	ADD target/*.jar app.jar
	# ENTRYPOINT ["sh", "-c", "java -jar /hello-world-rest-api.jar"]
	ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]
	
(2) WAR Deployment:
	
	From tomcat:8.0.51-jre8-alpine
	RUN rm -rf /usr/local/tomcat/webapps/*
	COPY ./target/*.war /usr/local/tomcat/webapps/ROOT.war
	CMD ["catalina.sh","run"]


*************
MySql: v5.7
*************

docker run -d -e MYSQL_ROOT_PASSWORD=adminPassword -e MYSQL_DATABASE=todos -e MYSQL_USER=todos-user -e MYSQL_PASSWORD=todosPassword --name mysql --network=web-application-mysql-network  mysql:5.7 

Same thing refer to docker-compose in following notes.


*************************
	Docker Network
*************************

- Bridge => Default network where all docker contaier are tied to
- Host => will use host network, now no need to expose the port, will use local host for network. It is not supporting docker
		  on windows and mac docker host.
- None => Container are tied to no network. usually not the option to be used.
- User-Defined / Custom => 

	(1) Create own custom bridge network:
		docker network create web-application-mysql-network
	
	(2) Run my sql using the custom bridge network:
	docker run -d -e MYSQL_ROOT_PASSWORD=adminPassword -p 3306:3306 -e MYSQL_DATABASE=todos -e MYSQL_USER=todos-user -e MYSQL_PASSWORD=todosPassword --name mysql --network=web-application-mysql-network  mysql:5.7
	
	(3) Run Application
		docker run -p 8081:8080 -e RDS_HOSTNAME=mysql --network=web-application-mysql-network drmodi/todo-web-application-mysql:0.0.1-SNAPSHOT
	
	
***********************************
	Volume Mapping
************************************

Mapping to local on host: /var/lib/mysql

docker run -p 3306:3306 -d -e MYSQL_ROOT_PASSWORD=adminPassword -e MYSQL_DATABASE=todos -e MYSQL_USER=todos-user -e MYSQL_PASSWORD=todosPassword --name mysql --network=web-application-mysql-network --volume mysql-database-volume:/var/lib/mysql  mysql:5.7


Same thing refer to docker-compose in following notes.


***********************************
	Docker Ignore
************************************
- So part of the frontend application,
we running the node server (npm install) which downloads everything in node_modules.
- Works similar way the gitIgnore.



*************************
	Two Stage - Build
*************************
- So far in all build, we were copying jar and dependecies which were created as part of our local build and then we create docker image. Lets say if one developer using different maven/java version then there is chance mismatch of the image. So mainly used if you want reproducible images then it is very useful.

- Biggest advantage is that it can be run anywhere, as long as docker engine is available, nothing being done on local machine. So not dependent on the local machine except the application code.

- Note: for some project it is overkill

Build
-----

##### Stage 1 - Lets build the "deployable package"

FROM maven:3.6.1-jdk-8-alpine as backend-build
WORKDIR /fullstack/backend

### Step 1 - Copy pom.xml and download project dependencies

# Dividing copy into two steps to ensure that we download dependencies 
# only when pom.xml changes
COPY pom.xml .
# dependency:go-offline - Goal that resolves all project dependencies, 
# including plugins and reports and their dependencies. -B -> Batch mode
RUN mvn dependency:go-offline -B



### Step 2 - Copy source and build "deployable package"
COPY src src
RUN mvn install -DskipTests

# Unzip
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

##### Stage 2 - Let's build a minimal image with the "deployable package"
FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/fullstack/backend/target/dependency
COPY --from=backend-build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=backend-build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=backend-build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.in28minutes.rest.webservices.restfulwebservices.RestfulWebServicesApplication"]




***********************************
	Docker Compose
************************************

- Multiple isolated environments on a single host
- Preserve volume data when container are created
- Only recreate containers that have changed
- Variables and moving a composition between environments

> check docker compose version
	docker-compose -version

> Staring all the container from docker compose
	docker-compose up -d 
	
> Stop, cleanup/removing container and networks
	docker-compose down

> To remove all stopped container which are created by using docker-compose
	docker-compose rm

> Checking network created by docker-compose
	<<FolderName where docker compose present>>_<<networkname defined in docker compose file>>
	
	ex:04-spring-boot-react-full-stack-h2_fullstack-application-network
	
> Checking the performed events by docker-compose
	docker-compose event
	
> Find what images are being used in docker compose command.
	docker-compose images
	
> To find containers running using this compose command
	docker-compose ps

> Finding configuration which is being used in docker-compose command:
	docker-compose config

> Finding what process are running and where they are running:
	docker-compose top
	
> Pause containers
	docker-compose pause
	docker-compose unpause
	
> Stop and Kill
	docker-compose stop (gracefull shutdown)
	docker-compose kill  (immediate terminate)
	
> docker-compose build instead of docker-compose up

	Can be created using dockerfile reference or image reference. Here we have been using the image directly.
  	but it can be used by refering docker file under build tag.
  
  
    #image: drmodi/todo-front-end:0.0.1-SNAPSHOT
    build:
      context: frontend/todo-app
      dockerfile: Dockerfile
  

> docker compose file:

	version: '3.7'
	# ERROR - Removed subprocess.CalledProcessError: 
	# Command '['/usr/local/bin/docker-credential-desktop', 'get']' 
	# returned non-zero exit status 1

	# SOLUTION - Remove "credsStore":"desktop" from ~/.docker/config.json 
	# Original Content of ~/.docker/config.json
	# {"auths":{},"credsStore":"", "credsStore":"desktop","stackOrchestrator":"swarm"}
	# Update it to this
	# {"auths":{},"credsStore":"","stackOrchestrator":"swarm"}
	# OR
	# {"auths":{},"stackOrchestrator":"swarm"}
	services:
	  todo-frontend:
	    image: drmodi/todo-front-end:0.0.1-SNAPSHOT
	    #build:
	      #context: frontend/todo-app
	      #dockerfile: Dockerfile
	    ports:
	      - "4200:80"
	    restart: always
	    depends_on: # Start the depends_on first
	      - todo-api 
	    networks:
	      - fullstack-application-network

	  todo-api:
	    image: drmodi/rest-api-full-stack:0.0.1-SNAPSHOT
	    ports:
	      - "8080:8080"
	    restart: always
	    networks:
	      - fullstack-application-network
  
	# Networks to be created to facilitate communication between containers
	networks:
	  fullstack-application-network:
	  

> Docker compose for web application with MySql

	version: '3.7'
	# Removed subprocess.CalledProcessError: Command '['/usr/local/bin/docker-credential-desktop', 'get']' returned non-zero exit status 1
	# I had this:
	# cat ~/.docker/config.json
	# {"auths":{},"credsStore":"", "credsStore":"desktop","stackOrchestrator":"swarm"}
	# I updated to this:
	# {"auths":{},"credsStore":"","stackOrchestrator":"swarm"}
	services:
	  todo-web-application:
	    image: drmodi/todo-web-application-mysql:0.0.1-SNAPSHOT
	    #build:
	      #context: .
	      #dockerfile: Dockerfile
	    ports:
	      - "8080:8080"
	    restart: always
	    depends_on: # Start the depends_on first
	      - mysql 
	    environment:
	      RDS_HOSTNAME: mysql
	      RDS_PORT: 3306
	      RDS_DB_NAME: todos
	      RDS_USERNAME: todos-user
	      RDS_PASSWORD: dummytodos
	    networks:
	      - todo-web-application-network

	  mysql:
	    image: mysql:5.7
	    ports:
	      - "3306:3306"
	    restart: always
	    environment:
	      MYSQL_ROOT_PASSWORD: root
	      MYSQL_ROOT_PASSWORD: dummypassword 
	      MYSQL_USER: todos-user
	      MYSQL_PASSWORD: dummytodos
	      MYSQL_DATABASE: todos
	    volumes:
	      - mysql-database-data-volume:/var/lib/mysql
	    networks:
	      - todo-web-application-network  
  
	# Volumes
	volumes:
	  mysql-database-data-volume:

	networks:
	  todo-web-application-network:
	




******************************
	Docker with Microservices
******************************


> Easier Development
	(1) Adopt new technology faster
		- Zero worry about deployment procedures (meaning no worries if one service built in java, one in 
		node js, etc..)
	(2) Fewer Environment Issues
		- No mor - "It works in my local"
		
> Easier operations: for infrastructure team
	- Consitent deployment automation across different environments and different technologies
	

> Dockerfile: 
  JAVA_OPTS="" (it can be used to defined memory or something that kind to pass on java runtime)
  -Djava.security.egd=file:/dev/./urandom (it is used for old spring boot version, help to function container 
   properly)

	FROM openjdk:8-jdk-alpine
	VOLUME /tmp
	EXPOSE 8100
	ADD target/*.jar app.jar
	ENV JAVA_OPTS=""
	ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
	
	
***********************************
More on Microservice:

Eureka Naming Server:
--------------------
- Service Registry: All service instances when they come up they will register themselves with Eureka Naming Server

- Service Discovery: If service1 calls service2 then service1 needs to know the live instances of service2, that process kown as service discovery and that will be done by Eureka Naming Server. Lets say 3 instance of service 2 are running and if service1 want to call service2, it will first reach out to eureka naming server and get the list of active/live instances of service2.

- So it helps in Dynamic Scale up and down.
	> Naming Server (Eureka)
	> Feign (Easier REST Clients) -> Furthure uses ribbon to do client side load balancing

- Naming server project setup:
	- Define the spring-cloud-starter-netflix-eureka-server dependency in pom.xml
	- add annotation to the Application class (with main method) - @EnableEurekaServer
	- Inside application.properties:
		- port#8761 - add this port#, since it is kind of default port for service registry 
		- Make sure add property for this project to not register themselves in registry.
			eureka.client.register-with-eureka=false
			eureka.client.fetch-registry=false

- Increase Scale - currency exchange service scaled using docker-compose scale command.
	docker-compose scale currency-exchange-service=2
	
	Error: due to port for scalling the instances
	Creating 05-microservices_currency-exchange-service_2 ... error

	ERROR: for 05-microservices_currency-exchange-service_2  Cannot start service currency-exchange-service: driver failed programming external connectivity on endpoint 05-microservices_currency-exchange-service_2 (7e3ff830e03a2c0d4f36837476e0d2266ac3b0f69cebb4345ddbec708137778b): Bind for 0.0.0.0:8000 failed: port is already allocated
	ERROR: Cannot start service currency-exchange-service: driver failed programming external connectivity on endpoint 05-microservices_currency-exchange-service_2 (7e3ff830e03a2c0d4f36837476e0d2266ac3b0f69cebb4345ddbec708137778b): Bind for 0.0.0.0:8000 failed: port is already allocated
	
	
	Note: in-order to increase the instances or scale the instances, remove the port# assignment. bcoz it will conflict with direct port#8000 on localhost since existing service is running with the same port.
	

Zuul API Gateway:
-----------------
- Microservices Architecture always have chain of microservices and required some additional features such as below. Think of if it requires to perform on each microservice then it would be lot of duplication of the features. Thats where API Gateway come into the picture.

	API Gateway:
	------------
		 > Authentication, authorization and security
		 > Rate limits: All about like this client can call 10K request a day or something like that.
		 > Fault Tolerance:
		 > Service Aggregation: 

- Zuul Api gateway project setup:
	- Define the spring-cloud-starter-netflix-zuul dependency in pom.xml
	- Also define the eureka-naming-server dependency so that API gateway register itself to naming server.
	- Enable Zuul proxy anotation to Application class
	- Enable Discovery client so it register itself to eureka server
	- To enable all the features, create ZuulLoginFilter class which extend zuul filer
		- here logging being done for test, but can have all complex logic for authentication/authorization and security etc.
	- In application.property
		- Configure the port#8765 which is kind of standard port for APIGateway
		- also eureka-client-defaultzone, to register at given naming server
		- Setting connection timeouts for services: feign and ribbon client timeouts.
	- Update client proxy service with Zuul-api-gateway naming. in this case currency-conversion-service being updated.

Distribution Tracing Zipkin - RabbitMQ
-----------------------------------------

- Centeralization logs/tracing
- RabbitMQ mq asynchrounously - Spring-rabbit mq dependency
	> in application property of other projects, like services and zuul not the naming server:
		spring.rabbitmq.host=rabbitmq
	> Image will be downloaded from the docker hub - rabbitmq:3.5.3-management
	> run on default port 5672
	> Add port for management console of mq - 15672
	> Add rabbitMq uri as environment variable to the zipkin server, services and zuul api application, so everyone can connect to it.
		Sudo: protocol://userid:password@rabbitmq:5672
		amqp://guest:guest@rabbitmq:5672
	


- Id generated by spring-cloud-starter-sleuth (update identifier in header checking sleuth header)
	> in application property:
		Defined - spring.sleuth.sampler.probability=1.0 (meaning all the 100% requests are )


- Zipkin Distributed Tracing Server
	- No project for zipkin, meaning no custom image for zipking, will use from dockerhub
		> image alway latest so : openzipkin/zipkin
		> port#9411 - typically default port
		> It depends on - rabbitMq
		> Add container name to zipkin
		> add the same container name to all services, zuul
		> other server are also depends on zipkin-server
		> Environment Variable:
			- STORAGE_TYPE: mem // meaning it is using inmemory database instead of RDBS
			- RABBIT_URI: amqp://guest:guest@rabbitmq:5672

	




	
	
	
	
	
	


