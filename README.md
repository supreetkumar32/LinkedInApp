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




