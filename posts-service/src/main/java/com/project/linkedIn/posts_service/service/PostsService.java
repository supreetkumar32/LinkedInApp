package com.project.linkedIn.posts_service.service;
import com.project.linkedIn.posts_service.dto.PostCreateRequestDto;
import com.project.linkedIn.posts_service.dto.PostDto;
import com.project.linkedIn.posts_service.entity.Post;
import com.project.linkedIn.posts_service.repository.PostsRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class PostsService {

    private final PostsRepository postsRepository;
    private final ModelMapper modelMapper;

    public PostDto createPost(PostCreateRequestDto postDto, Long userId) {
        Post post = modelMapper.map(postDto, Post.class);
        post.setUserId(userId);

        Post savedPost = postsRepository.save(post);
        return modelMapper.map(savedPost, PostDto.class);
    }
}
