import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { TaskListComponent } from './tasks/task-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'tasks', component: TaskListComponent },
];
