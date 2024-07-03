import { Component } from '@angular/core';
import { RegistrationRequest } from '../../services/models';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../services/services';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  
  registerRequest:RegistrationRequest = {email: '', firstname: '', lastname: '', password: ''};
  errorMessages:Array<string> = [];

  constructor(
    private router:Router,
    private authService:AuthenticationService
  ) {
  }

  register() {
    this.errorMessages = [];
    this.authService.register({
      body: this.registerRequest
    }).subscribe({
      next: () => {
        this.router.navigate(['activate-account']);
      },
      error: (err) => {
      console.log(err);
        if(err.error.validationErrors) {
          this.errorMessages = err.error.validationErrors;
        } else {
          this.errorMessages.push(err.error.error);
        }
      }
    });
  }

  login() {
    this.router.navigate(['login']);
  }
}
