package com.unir.catalogue.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.unir.catalogue.controller.model.BookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unir.catalogue.data.model.Book;
import com.unir.catalogue.controller.model.CreateBookRequest;
import com.unir.catalogue.service.BooksService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books Controller", description = "Microservicio encargado de exponer operaciones CRUD sobre libros alojados en una base de datos en memoria.")
public class BooksController {

    private final BooksService service;

    @GetMapping("/books")
    @Operation(
            operationId = "Obtener libros",
            description = "Operación de lectura",
            summary = "Se devuelve una lista de todos los libros almacenados en la base de datos.")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    public ResponseEntity<List<Book>> getBooks(
            @RequestHeader Map<String, String> headers,
            @Parameter(name = "title", description = "Título del libro. No tiene por qué ser exacto", example = "El Quijote", required = false)
            @RequestParam(required = false) String title,
            @Parameter(name = "author", description = "Autor del libro. Debe ser exacto", example = "Cervantes", required = false)
            @RequestParam(required = false) String author,
            @Parameter(name = "visible", description = "Estado del libro. true o false", example = "true", required = false)
            @RequestParam(required = false) Boolean visible,
            @Parameter(name = "isbn", description = "ISBN del libro. Debe ser exacto", example = "9780307389732", required = false)
            @RequestParam(required = false) String isbn) {

        log.info("headers: {}", headers);
        List<Book> books = service.getBooks(title, author, visible, isbn);

        return ResponseEntity.ok(books != null ? books : Collections.emptyList());
    }

    @GetMapping("/books/{bookId}")
    @Operation(
            operationId = "Obtener un libro",
            description = "Operación de lectura",
            summary = "Se devuelve un libro a partir de su identificador.")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(
            responseCode = "404",
            description = "No se ha encontrado el libro con el identificador indicado.")
    public ResponseEntity<Book> getBook(@PathVariable String bookId) {
        log.info("Request received for book {}", bookId);
        Book book = service.getBook(bookId);

        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/books/{bookId}")
    @Operation(
            operationId = "Eliminar un libro",
            description = "Operación de escritura",
            summary = "Se elimina un libro a partir de su identificador.")
    @ApiResponse(
            responseCode = "200",
            description = "Libro eliminado correctamente.")
    @ApiResponse(
            responseCode = "404",
            description = "No se ha encontrado el libro con el identificador indicado.")
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {
        return Boolean.TRUE.equals(service.removeBook(bookId)) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/books")
    @Operation(
            operationId = "Insertar un libro",
            description = "Operación de escritura",
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
        return createdBook != null ? ResponseEntity.status(HttpStatus.CREATED).body(createdBook) : ResponseEntity.badRequest().build();
    }

    @PatchMapping("/books/{bookId}")
    @Operation(
            operationId = "Modificar parcialmente un libro",
            description = "RFC 7386. Operación de escritura",
            summary = "RFC 7386. Se modifica parcialmente un libro.",
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
            description = "Operación de escritura",
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
