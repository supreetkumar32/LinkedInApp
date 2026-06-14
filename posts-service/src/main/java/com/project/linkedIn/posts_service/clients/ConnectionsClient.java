package com.project.linkedIn.posts_service.clients;

import com.project.linkedIn.posts_service.dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "connection-service", path = "/connections")
public interface ConnectionsClient {

    @GetMapping("/core/{userId}/first-degree")
    List<PersonDto> getFirstConnections(@PathVariable Long userId);

}
