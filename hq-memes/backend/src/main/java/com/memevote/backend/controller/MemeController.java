package com.memevote.backend.controller;

import com.memevote.backend.dto.request.MemeRequest;
import com.memevote.backend.dto.response.MemeResponse;
import com.memevote.backend.service.MemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/memes")
@Tag(name = "Memes", description = "Meme management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class MemeController {
    @Autowired
    private MemeService memeService;

    @Operation(summary = "Create a new meme", description = "Upload a meme image with metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meme created successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MemeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or file processing error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemeResponse> createMeme(
            @Parameter(description = "Meme metadata", required = true)
            @Valid @RequestPart("meme") MemeRequest memeRequest,
            @Parameter(description = "Meme image file", required = true)
            @RequestPart("file") MultipartFile file) {
        try {
            MemeResponse memeResponse = memeService.createMeme(memeRequest, file);
            return ResponseEntity.ok(memeResponse);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Create multiple memes", description = "Upload multiple meme images with optional categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Memes created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or file processing error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid")
    })
    @PostMapping(path = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<MemeResponse>> createMemes(
            @Parameter(description = "Meme image files", required = true)
            @RequestPart("files") MultipartFile[] files,
            @Parameter(description = "Optional categories to apply to all memes")
            @RequestPart(value = "categories", required = false) Set<String> categories) {
        try {
            List<MemeResponse> responses = memeService.createMemes(files, categories);
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get memes", description = "Retrieve a paginated list of memes with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved memes")
    })
    @GetMapping
    public ResponseEntity<Page<MemeResponse>> getMemes(
            @Parameter(description = "Filter by categories")
            @RequestParam(required = false) Set<String> categories,
            @Parameter(description = "Filter by username")
            @RequestParam(required = false) String username,
            @Parameter(description = "Filter by title")
            @RequestParam(required = false) String title,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        Page<MemeResponse> memes = memeService.getMemes(categories, username, title, pageable);
        return ResponseEntity.ok(memes);
    }

    @Operation(summary = "Get meme by ID", description = "Retrieve a specific meme by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the meme",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MemeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Meme not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MemeResponse> getMemeById(
            @Parameter(description = "ID of the meme to retrieve", required = true)
            @PathVariable Long id) {
        MemeResponse meme = memeService.getMemeById(id);
        return ResponseEntity.ok(meme);
    }
}
