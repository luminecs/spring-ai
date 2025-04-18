package org.springframework.ai.integration.tests.tool.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookService {

	private static final ConcurrentHashMap<Integer, Book> books = new ConcurrentHashMap<>(Map
		.of(// @formatter:off
		1, new Book("His Dark Materials", "Philip Pullman"),
		2, new Book("The Lion, the Witch and the Wardrobe", "C.S. Lewis"),
		3, new Book("The Hobbit", "J.R.R. Tolkien"),
		4, new Book("The Lord of The Rings", "J.R.R. Tolkien"),
		5, new Book("The Silmarillion", "J.R.R. Tolkien"))); // @formatter:on

	public List<Book> getBooksByAuthor(Author author) {
		return books.values().stream().filter(book -> author.name().equals(book.author())).toList();
	}

	public List<Author> getAuthorsByBook(List<Book> booksToSearch) {
		return books.values()
			.stream()
			.filter(book -> booksToSearch.stream().anyMatch(b -> b.title().equals(book.title())))
			.map(book -> new Author(book.author()))
			.toList();
	}

}
