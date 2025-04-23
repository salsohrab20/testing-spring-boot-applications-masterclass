package de.rieckpil.courses.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {

  private static final String VALID_ISBN = "1234567891234";
  @Mock
  private BookRepository bookRepository;

  @Mock
  private OpenLibraryApiClient openLibraryApiClient;

  @InjectMocks
  private BookSynchronizationListener bookSynchronizationListener;

  @Captor
  private ArgumentCaptor<Book> bookArgumentCaptor;

  @Test
  void shouldRejectBookWhenIsbnIsMalformed() {
    BookSynchronization bookSynchronization = new BookSynchronization("42");
    bookSynchronizationListener.consumeBookUpdates(bookSynchronization);
    verifyNoInteractions(openLibraryApiClient, bookRepository);
  }

  @Test
  void shouldNotOverrideWhenBookAlreadyExists() {
    BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
    when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(new Book());

    bookSynchronizationListener.consumeBookUpdates(bookSynchronization);
    verifyNoInteractions(openLibraryApiClient);
    verify(bookRepository, never()).save(ArgumentMatchers.any());
  }

  @Test
  void shouldThrowExceptionWhenProcessingFails() {
    BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
    when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(null);
    when(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).thenThrow(new RuntimeException("Network Timeout"));

    assertThrows(RuntimeException.class, () -> bookSynchronizationListener.consumeBookUpdates(bookSynchronization));
  }

  @Test
  void shouldStoreBookWhenNewAndCorrectIsbn() {
    BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
    when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(null);
    when(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).thenReturn(requestedBook());

    bookSynchronizationListener.consumeBookUpdates(bookSynchronization);
    verify(bookRepository).save(bookArgumentCaptor.capture());

    assertEquals("Title", bookArgumentCaptor.getValue().getTitle());
    assertEquals(VALID_ISBN, bookArgumentCaptor.getValue().getIsbn());
    assertEquals("Author", bookArgumentCaptor.getValue().getAuthor());
  }


  private Book requestedBook(){
    Book book = new Book();
    book.setIsbn(VALID_ISBN);
    book.setTitle("Title");
    book.setAuthor("Author");
    return book;
  }

}
