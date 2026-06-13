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

