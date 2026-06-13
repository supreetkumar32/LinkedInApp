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