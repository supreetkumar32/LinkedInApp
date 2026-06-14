package com.project.linkedIn.connection_service.controller;

import com.project.linkedIn.connection_service.entity.Person;
import com.project.linkedIn.connection_service.service.ConnectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
public class ConnectionsController {

    private final ConnectionsService connectionsService;

    @GetMapping("/first-degree")
    public ResponseEntity<List<Person>> getFirstConnections(@RequestHeader("X-User-id") Long userId) {
        return ResponseEntity.ok(connectionsService.getFirstDegreeConnections(userId));
    }
}
