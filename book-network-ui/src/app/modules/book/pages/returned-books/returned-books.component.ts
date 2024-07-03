import { Component, OnInit } from '@angular/core';
import { BorrowedBookResponse, PageResponseBorrowedBookResponse } from '../../../../services/models';
import { BookService } from '../../../../services/services';

@Component({
    selector: 'app-returned-books',
    templateUrl: './returned-books.component.html',
    styleUrl: './returned-books.component.scss'
})
export class ReturnedBooksComponent implements OnInit {

    returnedBooks: PageResponseBorrowedBookResponse = {};
    page: number = 0;
    size: number = 5;
    message: string = '';
    level: string = 'success';

    constructor(
        private bookService: BookService
    ) {}

    ngOnInit(): void {
        this.findAllReturnedBooks();
    }

    findAllReturnedBooks() {
        this.bookService.findAllReturnedBooks({
            page: this.page,
            size: this.size
        }).subscribe({
            next: (res) => {
                this.returnedBooks = res;
            }
        });
    }

    approveReturnBook(book: BorrowedBookResponse) {
        if(book.returned) {
            this.bookService.approveReturnBorrowedBook({
                'book-id': book.id as number
            }).subscribe({
                next: () => {
                    this.level = 'success';
                    this.message = 'Book return approved';
                    this.findAllReturnedBooks();
                },
                error: (err) => {
                    this.level = 'danger';
                    this.message = err.error.error;
                }
            });
        } else {
            this.level = 'danger';
            this.message = 'This book is not yet returned';
        }
    }

    goToFirstPage() {
        this.page = 0;
        this.findAllReturnedBooks();
    }

    goToLastPage() {
        this.page = (this.returnedBooks.totalPage as number - 1);
        this.findAllReturnedBooks();
    }

    goToPreviousPage() {
        --this.page;
        this.findAllReturnedBooks();
    }

    goToNextPage() {
        ++this.page;
        this.findAllReturnedBooks();
    }

    goToPage(pageIndex: number) {
        this.page = pageIndex;
        this.findAllReturnedBooks();
    }

    isLastPage(): boolean {
        let result = true;
        if (this.returnedBooks.totalPage) {
            result = (this.page == (this.returnedBooks.totalPage - 1));
        }
        return result;
    }
}
