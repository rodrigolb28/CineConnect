package com.cinema.CineConnect.controller;

import com.cinema.CineConnect.model.DTO.MovieRecord;
import com.cinema.CineConnect.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/movie")
@CrossOrigin(origins = "http://localhost:5173") // Ajuste conforme seu front
public class MovieController {

    private final MovieService movieService;

    // Injeção via construtor (Melhor prática)
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<MovieRecord> createMovie(
            @RequestPart("movie") MovieRecord movie,
            @RequestPart("file") MultipartFile file) {
        try {
            MovieRecord savedMovie = movieService.saveMovieWithImage(movie, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public List<MovieRecord> getAllMovies() {
        return movieService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieRecord> getMovieById(@PathVariable Integer id) {
        // CORREÇÃO: O serviço já retorna Optional, não use "Optional()" como função
        Optional<MovieRecord> movie = movieService.findById(id);

        return movie.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}