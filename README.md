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
