CREATE TABLE book_author_mapping (
    book_id UUID REFERENCES book(id) ON DELETE CASCADE,
    author_id UUID REFERENCES author(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

CREATE INDEX idx_book_author_book ON book_author_mapping(book_id);
CREATE INDEX idx_book_author_author ON book_author_mapping(author_id);
