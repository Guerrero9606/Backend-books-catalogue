package com.unir.catalogue.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unir.catalogue.controller.model.BookDto;
import com.unir.catalogue.controller.model.CreateBookRequest;
import com.unir.catalogue.data.elasticsearch.SearchResponse;
import com.unir.catalogue.data.model.Book;
import com.unir.catalogue.data.elasticsearch.BookElasticRepository;
import com.unir.catalogue.service.BooksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books Controller", description = "Microservicio encargado de exponer operaciones CRUD sobre libros")
public class BooksController {

    private final BooksService service;
    // Inyectamos la clase personalizada para operaciones de lectura en Elasticsearch
    private final BookElasticRepository bookElasticRepository;

    @GetMapping("/books")
    @Operation(
            operationId = "Obtener libros",
            description = "Consulta el índice de Elasticsearch para obtener los libros. Si 'facets' es true, se devuelven también las facetas.",
            summary = "Devuelve una lista de libros y, opcionalmente, facetas."
    )
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {Book.class, SearchResponse.class}))
    )
    public ResponseEntity<?> getBooks(
            @RequestHeader Map<String, String> headers,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false, defaultValue = "false") Boolean facets) {

        if (facets) {
            SearchResponse response = bookElasticRepository.searchWithFacets(title, author, visible, isbn, price);
            return ResponseEntity.ok(response);
        } else {
            List<Book> books;
            if (title != null || author != null || visible != null || isbn != null || price != null) {
                books = bookElasticRepository.search(title, author, visible, isbn, price);
            } else {
                books = bookElasticRepository.getBooks();
            }
            return ResponseEntity.ok(books != null ? books : List.of());
        }
    }

    @GetMapping("/books/{bookId}")
    @Operation(
            operationId = "Obtener un libro",
            description = "Operación de lectura. Se consulta el índice de Elasticsearch para obtener el libro por su identificador.",
            summary = "Se devuelve un libro a partir de su identificador desde Elasticsearch.")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(
            responseCode = "404",
            description = "No se ha encontrado el libro con el identificador indicado en Elasticsearch.")
    public ResponseEntity<Book> getBook(@PathVariable String bookId) {
        log.info("Request received for book {}", bookId);
        try {
            Long id = Long.valueOf(bookId);
            Optional<Book> book = bookElasticRepository.findById(id);
            return book.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (NumberFormatException e) {
            log.error("El ID proporcionado no es válido: {}", bookId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Operaciones de escritura: se apuntan a PostgreSQL (a través del service), y la sincronización con Elasticsearch se maneja internamente.

    @PostMapping("/books")
    @Operation(
            operationId = "Insertar un libro",
            description = "Operación de escritura: se crea un libro en PostgreSQL y se sincroniza en Elasticsearch.",
            summary = "Se crea un libro a partir de sus datos.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del libro a crear.",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateBookRequest.class))))
    @ApiResponse(
            responseCode = "201",
            description = "Libro creado exitosamente.")
    @ApiResponse(
            responseCode = "400",
            description = "Datos incorrectos introducidos.")
    public ResponseEntity<Book> addBook(@RequestBody CreateBookRequest request) {
        Book createdBook = service.createBook(request);
        return createdBook != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(createdBook)
                : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/books/{bookId}")
    @Operation(
            operationId = "Eliminar un libro",
            description = "Operación de escritura: se elimina un libro en PostgreSQL y se sincroniza la eliminación en Elasticsearch.",
            summary = "Se elimina un libro a partir de su identificador.")
    @ApiResponse(
            responseCode = "200",
            description = "Libro eliminado correctamente.")
    @ApiResponse(
            responseCode = "404",
            description = "No se ha encontrado el libro con el identificador indicado.")
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {
        return Boolean.TRUE.equals(service.removeBook(bookId))
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/books/{bookId}")
    @Operation(
            operationId = "Modificar parcialmente un libro",
            description = "RFC 7386. Operación de escritura: se modifica parcialmente un libro en PostgreSQL y se sincroniza la actualización en Elasticsearch.",
            summary = "Se modifica parcialmente un libro.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del libro a modificar.",
                    required = true,
                    content = @Content(mediaType = "application/merge-patch+json", schema = @Schema(implementation = String.class))))
    @ApiResponse(
            responseCode = "200",
            description = "Libro modificado exitosamente.")
    @ApiResponse(
            responseCode = "400",
            description = "Libro inválido o datos incorrectos introducidos.")
    public ResponseEntity<Book> patchBook(@PathVariable String bookId, @RequestBody String patchBody) {
        Book patched = service.updateBook(bookId, patchBody);
        return patched != null ? ResponseEntity.ok(patched) : ResponseEntity.badRequest().build();
    }

    @PutMapping("/books/{bookId}")
    @Operation(
            operationId = "Modificar totalmente un libro",
            description = "Operación de escritura: se modifica totalmente un libro en PostgreSQL y se sincroniza la actualización en Elasticsearch.",
            summary = "Se modifica totalmente un libro.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del libro a actualizar.",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))))
    @ApiResponse(
            responseCode = "200",
            description = "Libro actualizado exitosamente.")
    @ApiResponse(
            responseCode = "404",
            description = "Libro no encontrado.")
    public ResponseEntity<Book> updateBook(@PathVariable String bookId, @RequestBody BookDto body) {
        Book updated = service.updateBook(bookId, body);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
}
