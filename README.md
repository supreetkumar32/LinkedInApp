LinkedInApp

for the post request
hit the post url from postman http://localhost:9010/posts
in body raw 
    {
    "content":"yo bro"
    }

it will give the result as follows:
    {
    "content": "yo bro",
    "createdAt": "2026-06-04T19:18:53.79461",
    "id": 1,
    "userId": 1
    }
userId is 1 as i have hardcoded the value

2. hit the get post from postman http://localhost:9010/posts/1
we get the result:
    {
       "content": "yo bro",
       "createdAt": "2026-06-04T19:18:53.79461",
       "id": 1,
       "userId": 1
    }

3. like post api
   hit the post url http://localhost:9010/likes/1
   see the data in the database in the post_likes 
4. get all posts of particular user
   http://localhost:9010/posts/users/1/allPosts
   [
   {
       "content": "yo bro",
       "createdAt": "2026-06-04T19:18:53.79461",
       "id": 1,
       "userId": 1
   },
   {
       "content": "yo bro supreet",
       "createdAt": "2026-06-04T19:50:43.839732",
       "id": 2,
       "userId": 1
   }
   ]
5. unlike post http://localhost:9010/likes/1

 it will remove the like from the database
added the dependencies for jwt
implementing the signup and login
below dependency for is used to provide the BCrypt password hashing algorithm in Java.
 <dependency>
 <groupId>org.mindrot</groupId>
 <artifactId>jbcrypt</artifactId>
 <version>0.4</version>
 </dependency>

6. POST http://localhost:9020/auth/signup
 {
   "name":"Supreet",
   "email":"Supreet@gmail.com",
   "password":"Supreet@123"
   }
got output as:
   {
   "id": 1,
   "name": "Supreet",
   "email": "Supreet@gmail.com"
   }

7. POST http://localhost:9020/auth/login
     {
      "email":"Supreet@gmail.com",
      "password":"Supreet@123"
      }
will get the token in the output

configured the discovery server
Put the @EnableEurekaServer
hit the localhost:8761

for making the clients, put the following in the pom
<spring-cloud.version>2025.0.0</spring-cloud.version>

<dependencyManagement>
<dependencies>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-dependencies</artifactId>
<version>${spring-cloud.version}</version>
<type>pom</type>
<scope>import</scope>
</dependency>
</dependencies>
</dependencyManagement>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

registered the user service as a client
registered the post service and api gateway as a client
we can see user service,post service and api gateway http://localhost:8761/

Now i have given the configuration as:
spring:
cloud:
gateway:
routes:
- id: user-service
uri: lb://USER-SERVICE
predicates:
- Path=/api/v1/users/**
filters:
- StripPrefix=2

        - id: posts-service
        uri: lb://POSTS-SERVICE
        predicates:
            - Path=/api/v1/posts/**
        filters:
            - StripPrefix=2

        - id: connections-service
          uri: lb://CONNECTIONS-SERVICE
          predicates:
            - Path=/api/v1/connections/**
          filters:
            - StripPrefix=2

So the endpoints got changed
Example:http://localhost:9010/posts/core will become  http://localhost:8080/api/v1/posts/core

all posts like are as follows:

createPost POST http://localhost:8080/api/v1/posts/core
getPost    GET  http://localhost:8080/api/v1/posts/core/1
getUserPosts GET http://localhost:8080/api/v1/posts/core/users/1/allPosts
likePost POST http://localhost:8080/api/v1/posts/likes/2
unlikePost DELETE http://localhost:8080/api/v1/posts/likes/2

------EVERYTHING WORKING FINE TILL HERE----------
in the neo4j database, i have manually created the notes and the relationship in the query section.
UNWIND [
{userId: 1, name: 'Henry'},
{userId: 2, name: 'Grace'},
{userId: 3, name: 'Ivy'},
{userId: 4, name: 'Jack'},
{userId: 5, name: 'Eva'},
{userId: 6, name: 'Frank'},
{userId: 7, name: 'Alice'},
{userId: 8, name: 'David'},
{userId: 9, name: 'Charlie'},
{userId: 10, name: 'Bob'}
] AS person
CREATE (:Person {userId: person.userId, name: person.name});

for creating the relationships queries are :
MATCH (a:Person {name: 'Henry'}),   (b:Person {name: 'Grace'})
CREATE (a)-[:CONNECTED_TO]->(b);

MATCH (a:Person {name: 'Alice'}),   (b:Person {name: 'David'})
CREATE (a)-[:CONNECTED_TO]->(b);

MATCH (a:Person {name: 'Ivy'}),     (b:Person {name: 'Jack'})
CREATE (a)-[:CONNECTED_TO]->(b);

MATCH (a:Person {name: 'Charlie'}), (b:Person {name: 'Bob'})
CREATE (a)-[:CONNECTED_TO]->(b);

MATCH (a:Person {name: 'Eva'}),     (b:Person {name: 'Frank'})
CREATE (a)-[:CONNECTED_TO]->(b);

MATCH (a:Person {name: 'Grace'}),   (b:Person {name: 'Alice'})
CREATE (a)-[:CONNECTED_TO]->(b);

For visualising the nodes and relationship query is :
MATCH (a)-[r]->(b)
RETURN a, r, b;

After establishing the connection with the neo4J database
8. GET http://localhost:9030/connections/core/4/first-degree
    output is as:
[
   {
       "id": 2,
       "userId": 3,
       "name": "Ivy"
   }
]
after integrating it with the API gateway,
GET http://localhost:8080/api/v1/connections/core/4/first-degree

Agenda:
Setup teh gateway to handle JWT
Pass on JWT to downstream services
Setup custom UserContextHolder
Pass data to UserContextHolder fromm Request Interceptor
Register the RequestInterceptor with webMVC
Setup OpenFeignInterceptor to pass on user data

All the requests which are coming to API Gateway are intercepted by our own custom filters that would be a gateway filter. Then we 
pass on those filters to different routes. In that particular filter, I will check that the request header contains userId  and token 
or not.
In the AuthenticationFilter (GatewayFilter function), i have extracted the token and then passes to other services.i can pass the token 
by passing the token inside header.
.request(r -> r.header("X-User-Id", userId))//adding the userId to header

Before Gateway

Client sends:
POST /api/v1/posts/create
Authorization: Bearer abc.xyz.123

After Gateway

Posts service receives:
POST /posts/create
Authorization: Bearer abc.xyz.123
X-User-Id: 5

Why add X-User-Id?

Without it, every microservice would have to:
Read JWT
Verify signature
Extract claims

With this approach:

Gateway validates once
Services trust the gateway
Services simply read:
@RequestHeader("X-User-Id")
String userId;

Example in Posts Service:

@PostMapping("/create")
public Post createPost(
@RequestHeader("X-User-Id") String userId) {

    System.out.println("Creating post for user: " + userId);
}

Till now i have created the AuthenticationFilter.

POST http://localhost:9020/users/auth/signup becomes http://localhost:8080/api/v1/users/auth/signup
POST http://localhost:9020/users/auth/login becomes http://localhost:8080/api/v1/users/auth/login

i have checked the functionality whether the userId passes from API gateway to post service by putting the below line in post controller
and running in the debug mode
String userId= httpServletRequest.getHeader("X-User-Id");
i logged in and got the token.Put that token in the getPost api authentication in postman and ran it.
Till now added the authenticationFilter in posts and connection service and in post service whether the userId is passed in the post service.

Creating the UserContextHolder.
From UserContextHolder,i can hold of the userId from any layer inside the code.
UserInterceptor is used to intercept all the incoming request and from those request will get the header and from that i get
X-User-Id header and then i will initiate userContextHolder.
In WebConfig, I am allowing the interceptor to intercept the request.

Now, by creating the UserInterceptor I can directly get the userId.
i dont need to do   
public ResponseEntity<PostDto> getPost(@PathVariable Long postId,HttpServletRequest httpServletRequest) {
String userId= httpServletRequest.getHeader("X-User-Id")
i can directly do..Long userId= UserContextHolder.getCurrentUserId()

Implemented the Feign client to get teh firstConnections in postService

one observation: a person should not be allowed to see the first degree connection of everyone.
So removing the userId from @GetMapping("/{userId}/first-degree")
FeinClientInterceptor is made for this purpose.
In this(FeinClientInterceptor) we have changed the header
if(userId != null) {
requestTemplate.header("X-User-Id", userId.toString());
}

now...GET http://localhost:8080/api/v1/connections/core/first-degree is the updated Url.

Now i can remove userId from   public ResponseEntity<List<Person>> getFirstConnections(@RequestHeader("X-User-id") Long userId) {
i ahve to add UserContextHolder,UserInterceptor,WebConfig in auth of connection service.

Now, i have to do
Create notification service
Setting up kafka message queue
Setup the consumer inside the notification service

Setup producer in post service
Create request mapping for send,accept and reject the connection request
Setup producer in Connection service

Create the skeleton for notification service and kafka.Remove the userId from the post creation as we 
can directly get the userId from the UserContext.

In this commit , i have created the code for creating the two topics (post-created-topic and post-liked-topic).
Also, did some hardcoded userId removal.
After writing the code , run the microservices in the below order:
DiscoveryServer -> Use -> Post -> Apigateway

After running it, go to the postman 
and log in..take the generated token
POST http://localhost:8080/api/v1/posts/core (having the generated token in authentication)
{
"content":"kafka event creation"
}

then we got the output as:
{
"id": 5,
"content": "kafka event creation",
"userId": 2,
"createdAt": "2026-06-14T18:24:47.124308"
}

now, checking in the terminal
C:\kafka\bin\windows>kafka-topics.bat --bootstrap-server localhost:9092 --list

we get the output as:
__consumer_offsets
post-created-topic
post-liked-topic
test

want to check what is inside the post-created-topic:
C:\kafka\bin\windows>kafka-console-consumer.bat ^
More?   --bootstrap-server localhost:9092 ^
More?   --topic post-created-topic ^
More?   --from-beginning

i get:(what i have put in the POST above)
{"creatorId":2,"content":"kafka event creation","postId":5}

now, checking for the like controller
in postman
POST http://localhost:8080/api/v1/posts/likes/2 (given the authentication token)

C:\kafka\bin\windows>kafka-console-consumer.bat ^
More?   --bootstrap-server localhost:9092 ^
More?   --topic post-liked-topic ^
More?   --from-beginning

got output as:
{"postId":2,"creatorId":1,"likedByUserId":2}

Till now, i have created the producers(post-created-topic and post-liked-topic) and checked via postman and
kafka console via commands.

Now, i will consume notifications.
After writing the consumer code,
do a login , take a token to the createPost
POST http://localhost:8080/api/v1/posts/core
{
"content":"kafka consumer checking"
}

GOT the output
{
    "id": 6,
    "content": "kafka consumer checking",
    "userId": 2,
    "createdAt": "2026-06-14T22:15:15.038262"
}
In the terminal of the NotificationService Application,I get
Sending notifications: handlePostCreated: PostCreatedEvent(creatorId=2, content=kafka consumer checking, postId=6)
also in the terminal of ConnectionServiceApplication, it is showing
Getting first degree connection of userId : 2

Now i will create request mapping for send,accept and reject the connection request.

Now I will setup the producer in the connection service.
AcceptConnectionRequestEvent 
SendConnectionRequestEvent

Now i will create the consumer (ConnectionsServiceConsumer)
@KafkaListener(topics = "send-connection-request-topic")
@KafkaListener(topics = "accept-connection-request-topic")

Now 
POST http://localhost:8080/api/v1/connections/core/request/3 with bearer token
it will show 200 OK
it will show REQUESTED_TO to userId 3 in the neo4j database from the userId whose token i have given
And in the terminal of the NotificationServiceApplication it will show the following which shows that the notification service is also working:
c.p.l.n.c.ConnectionsServiceConsumer     : handle connections: handleSendConnectionRequest: SendConnectionRequestEvent(senderId=2, receiverId=3)
2026-06-15T22:25:10.021+05:30  INFO 26092 --- [notification-service] [ntainer#1-0-C-1] c.p.l.n.service.SendNotification         : Notification saved for user: 3

Now i am using the Zipkin and micrometer for distributed tracing
I have Downloaded zipkin server from https://repo1.maven.org/maven2/io/zipkin/zipkin-server/3.6.1/zipkin-server-3.6.1-exec.jar
In teh springboot terminal, i have opened
PS C:\Users\supre\Downloads> java -jar zipkin-server-3.6.1-exec.jar
hit the url http://localhost:9411/zipkin/

Adding the below dependencies to pom.xml to every microservice
<dependencies>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

		<!--Zipkin and micrometer dependency-->
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-observation</artifactId>
		</dependency>
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing-bridge-brave</artifactId>
		</dependency>
		<dependency>
			<groupId>io.zipkin.reporter2</groupId>
			<artifactId>zipkin-reporter-brave</artifactId>
		</dependency>
		<dependency>
			<groupId>io.github.openfeign</groupId>
			<artifactId>feign-micrometer</artifactId>
		</dependency>

Add the below to application.yml or application.properties

management.endpoints.web.exposure.include=*
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint: http://localhost:9411/api/v2/spans

management:
    endpoints:
        web:
            exposure:
                include: "*"
    tracing:
        sampling:
            probability: 1.0
    zipkin:
        tracing:
            endpoint: http://localhost:9411/api/v2/spans

now run all the microservices,neo4j,kafka,zipkin,postman,postgresql and after hitting any restendpoint via postman
hit the endpoint http://localhost:9411/zipkin/ to see the logs
Till now zipkin is running well.

Now i will do the centralised login using the ELK Stack
ELK is a collection of three open-source applications
- Elasticsearch, Logstash, and Kibana from Elastic that accepts data from any source or format, on which you can then perform search, analysis, and visualize
  that data.

download the elk from
ElasticSearch: https://www.elastic.co/downloads/elasticsearch
Logstash: https://www.elastic.co/downloads/logstash
Kibana: https://www.elastic.co/downloads/kibana

put it in c drive and extracted it there

run the elastic search as follows:
C:\elasticsearch-9.4.2\elasticsearch-9.4.2\bin>elasticsearch.bat

Γä╣∩╕Å  Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
8oOsZwZxMk6puX0L0gsV

Γä╣∩╕Å  HTTP CA certificate SHA-256 fingerprint:
33803f43b1313f5192d04911dca3291055457b454e5ccbbce1f16e0ed1a61306

Γä╣∩╕Å  Configure Kibana to use this cluster:
ΓÇó Run Kibana and click the configuration link in the terminal when Kibana starts.
ΓÇó Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
eyJ2ZXIiOiI4LjE0LjAiLCJhZHIiOlsiMTkyLjE2OC4xLjE2OjkyMDAiXSwiZmdyIjoiMzM4MDNmNDNiMTMxM2Y1MTkyZDA0OTExZGNhMzI5MTA1NTQ1N2I0NTRlNWNjYmJjZTFmMTZlMGVkMWE2MTMwNiIsImtleSI6Im1IN2Q1SjRCdDFvdHM2M2UzX05OOjFMUFZ3SEFkMEQ3X0FmSndyVksxVUEifQ==

hit the url https://localhost:9200/

go to advanced
username: elastic
password:8oOsZwZxMk6puX0L0gsV

then run kibana
C:\kibana-9.4.2\bin>kibana.bat

after running, i got Go to http://localhost:5601/?code=738000 to get started in the last of cmd
I hit the url http://localhost:5601/?code=738000
copy the enrollment token (eyJ2ZXIiOiI4LjE0LjAiLCJhZHIiOlsiMTkyLjE2OC4xLjE2OjkyMDAiXSwiZmdyIjoiMzM4MDNmNDNiMTMxM2Y1MTkyZDA0OTExZGNhMzI5MTA1NTQ1N2I0NTRlNWNjYmJjZTFmMTZlMGVkMWE2MTMwNiIsImtleSI6Im1IN2Q1SjRCdDFvdHM2M2UzX05OOjFMUFZ3SEFkMEQ3X0FmSndyVksxVUEifQ==) and paste it in the UI i got after hitting teh url
after setting up the configuration, it will ask for username and password
username: elastic
password:8oOsZwZxMk6puX0L0gsV

now for logstash
create the logback-spring.xml in the resorces folder and put the following code .Do the same for all the services.
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <springProperty name="applicationName" source="spring.application.name" defaultValue="UNKNOWN"/>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{dd-MM-yyyy HH:mm:ss.SSS} [%thread] [%X{traceId}-%X{spanId}] %-5level ${applicationName}-%logger{36}.%M - %msg%n</pattern>
        </encoder>
    </appender>
    <appender name="ROLLING-FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- Trigger for rolling logs every day and limit size to 10 MB -->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- rollover daily -->
            <fileNamePattern>logs/${applicationName}/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <!-- keep 30 days' worth of history -->
            <maxHistory>30</maxHistory>
        </rollingPolicy>

        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>%d{dd-MM-yyyy HH:mm:ss.SSS} [%thread] [%X{traceId}-%X{spanId}] %-5level ${applicationName}-%logger{36}.%M - %msg%n</Pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="ROLLING-FILE" />
    </root>
</configuration>

after the running the microservices , we can see the logs folder got created











