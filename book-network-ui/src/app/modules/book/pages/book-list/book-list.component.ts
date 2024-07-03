import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from '../../../../services/services';
import { BookResponse, PageResponseBookResponse } from '../../../../services/models';

@Component({
    selector: 'app-book-list',
    templateUrl: './book-list.component.html',
    styleUrl: './book-list.component.scss'
})
export class BookListComponent implements OnInit {

    bookResponse: PageResponseBookResponse = {};
    page: number = 0;
    size: number = 5;
    message: string = '';
    level: string = 'success';

    constructor(
        private router: Router,
        private bookService: BookService
    ) {
    }

    ngOnInit(): void {
        this.findAllBooks();
    }

    private findAllBooks(): void {
        this.bookService.findAllBooks({
            page: this.page,
            size: this.size
        }).subscribe({
            next: (res) => {
                this.bookResponse = res;
            }
        });
    }

    goToFirstPage() {
        this.page = 0;
        this.findAllBooks();
    }

    goToLastPage() {
        this.page = (this.bookResponse.totalPage as number - 1);
        this.findAllBooks();
    }

    goToPreviousPage() {
        --this.page;
        this.findAllBooks();
    }

    goToNextPage() {
        ++this.page;
        this.findAllBooks();
    }

    goToPage(pageIndex: number) {
        this.page = pageIndex;
        this.findAllBooks();
    }

    isLastPage(): boolean {
        let result = true;
        if (this.bookResponse.totalPage) {
            result = (this.page == (this.bookResponse.totalPage - 1));
        }
        return result;
    }

    borrowBook(book: BookResponse) {
        this.message = '';
        this.level = 'success';
        this.bookService.borrowBook({
            'book-id': book.id as number
        }).subscribe({
            next: () => {
                this.message = 'Book successfully added to your list';
            },
            error: (err) => {
                this.message = err.error.error;
                this.level = 'danger';
            }
        });
    }
}
