package com.project.linkedIn.posts_service.service;
import com.project.linkedIn.posts_service.auth.UserContextHolder;
import com.project.linkedIn.posts_service.clients.ConnectionsClient;
import com.project.linkedIn.posts_service.dto.PersonDto;
import com.project.linkedIn.posts_service.dto.PostCreateRequestDto;
import com.project.linkedIn.posts_service.dto.PostDto;
import com.project.linkedIn.posts_service.entity.Post;
import com.project.linkedIn.posts_service.exception.ResourceNotFoundException;
import com.project.linkedIn.posts_service.repository.PostsRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PostsService {

    private final PostsRepository postsRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsClient connectionsClient;

    public PostDto createPost(PostCreateRequestDto postDto, Long userId) {
        Post post = modelMapper.map(postDto, Post.class);
        post.setUserId(userId);

        Post savedPost = postsRepository.save(post);
        return modelMapper.map(savedPost, PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.debug("Retrieving post with ID: {}", postId);

        Long userId= UserContextHolder.getCurrentUserId();
        //using the feign client
        List<PersonDto> firstConnections = connectionsClient.getFirstConnections();


        Post post = postsRepository.findById(postId).orElseThrow(() ->
                new ResourceNotFoundException("Post not found with id: "+postId));
        return modelMapper.map(post, PostDto.class);
    }

    public List<PostDto> getAllPostsOfUser(Long userId) {
        List<Post> posts = postsRepository.findByUserId(userId);

        return posts
                .stream()
                .map((element) -> modelMapper.map(element, PostDto.class))
                .collect(Collectors.toList());
    }
}
